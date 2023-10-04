package com.example.xmatenotes.logic.presetable;

import android.content.Context;

//import com.example.xmatenotes.logic.network.BitableManager;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExcelManager extends ExcelHelper {

    private static final String TAG = "ExcelManager";

    private static final String SEARCH_START = "Search1";

    private static final ExcelManager excelManager = new ExcelManager();
//    private static final BitableManager bitableManager = BitableManager.getInstance();

    /**
     * 摘要信息表数据
     */
    private AbstractSheet abstractSheet;

    /**
     * 数据表集合
     */
    private Map<String, DataSheet> dataSheetMap = new HashMap<>();

    private ExcelManager() {
    }

    public static ExcelManager getInstance(){
        return excelManager;
    }

    /**
     * 初始化解析配置
     * @param excelName 目标表格相对路径 eg:A3学程样例·纸分区坐标.xlsx
     * @return
     */
    public ExcelManager init(Context context, String excelName){
        super.init(context);

        //打开目标表格
        openExcel("excel/"+excelName);
        LogUtil.e(TAG, "init(): 打开目标表格完毕");

        //解析摘要信息表
        if(switchSheet(AbstractSheet.ABSTRACT_SHEET_NAME)){
            this.abstractSheet = new AbstractSheet().parseAbstractSheet(this.curWorkbook);
            LogUtil.e(TAG, "init(): 解析摘要信息表完毕");
        }

        //解析数据表
        Map<String, String> dataMap = this.abstractSheet.getMap(AbstractSheet.DATA_SHEET_NAME);
        Set<Map.Entry<String, String>> set = dataMap.entrySet();
        Iterator<Map.Entry<String, String>> it = set.iterator();
        this.dataSheetMap.clear();
        while (it.hasNext()){
            Map.Entry<String, String> node = it.next();
            String dataSheetName = node.getValue();
            DataSheet dataSheet = parseDataSheet(dataSheetName);
            this.dataSheetMap.put(dataSheet.getName(), dataSheet);
        }

        LogUtil.e(TAG, "init(): 解析数据表完毕: "+dataSheetMap);

        //初始化BitableManager
//        Map<String, String> bitableMap = this.abstractSheet.getMap(AbstractSheet.BITABLE_PROPERTY);
//        if(bitableMap != null){
//            bitableManager.initial(bitableMap.get("AppId"), bitableMap.get("AppSecret"), bitableMap.get("APPtoken"));
//            LogUtil.e(TAG, "init(): 初始化BitableManager完毕");
//        }

        return this;
    }


    public AbstractSheet getAbstractSheet() {
        return abstractSheet;
    }

    /**
     * 解析目标数据表
     * @param dataSheetName 数据表名，确保为数据表
     * @return
     */
    public DataSheet parseDataSheet(String dataSheetName){
        DataSheet dataSheet = new DataSheet(dataSheetName);

        XSSFSheet sheet = openSheet(dataSheetName);//打开数据表，根据表名

        //解析数据表头
        SheetHeader sheetHeader = new SheetHeader().parseSheetHeaderRow(sheet);

        int firstRow = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW));
        int lastRow = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_ROW));
        int firstCol = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_COLUMN));
        int lastCol = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_COLUMN));
        int header = rowNameToIndex(sheetHeader.get(SheetHeader.SHEET_HEADER_ROW));
        String key = sheetHeader.get(SheetHeader.PRIMARY_KEY);
        dataSheet.setPrimaryField(key);

        int rowNum = firstRow;

        //遍历数据表，填充dataSheet

        while(rowNum <= lastRow){
            int colNum = firstCol;
            XSSFRow row  = sheet.getRow(rowNum); //当前所在的行数
            XSSFRow rowHead = sheet.getRow(header); //表头行所在的行
            Map<String,String> map = new HashMap();

            while(colNum<=lastCol){

                XSSFCell cell = row.getCell(colNum);

                CellRangeAddress cellRangeAddress = null;
                if(ExcelUtil.inMerger(sheet, cell)){
                    cellRangeAddress = ExcelUtil.getMergedCellAddress(sheet,cell);
                    cell = sheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());

                }
                map.put(getCellString(rowHead.getCell(colNum)),getCellString(cell));

                colNum = colNum +1;
            }
            String curKey = map.get(key);

            dataSheet.addMap(curKey,map);
            rowNum = rowNum +1;
        }

        return dataSheet;
    }

    public LocalData getLocalData(int x, int y, int pageId, String command, String roleName){
        LocalData localData = new LocalData(x, y, pageId, command, roleName);

        //搜索起始sheet
        switchSheet(this.abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME).get(SEARCH_START));
        getLocalDataInSearchSheet(curSheet, Integer.MIN_VALUE, Integer.MAX_VALUE, localData);

//        Map<String, String> searchSheetMap = this.abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME);
//        if(searchSheetMap != null){
//            Set<Map.Entry<String, String>> set = searchSheetMap.entrySet();
//            Iterator<Map.Entry<String, String>> it = set.iterator();
//            while (it.hasNext()){
//                Map.Entry<String, String> node = it.next();
//                String searchSheetName = node.getValue();
//                switchSheet(searchSheetName);
//                getLocalDataInSearchSheet(curSheet, localData);
//            }
//        }
//
//        Map<String, String> responseSheetMap = this.abstractSheet.getMap(AbstractSheet.RESPONSE_SHEET_NAME);
//        if(responseSheetMap != null){
//            Set<Map.Entry<String, String>> set = responseSheetMap.entrySet();
//            Iterator<Map.Entry<String, String>> it = set.iterator();
//            while (it.hasNext()){
//                Map.Entry<String, String> node = it.next();
//                String responseSheetName = node.getValue();
//                switchSheet(responseSheetName);
//                getLocalDataInResponseSheet(curSheet, localData);
//            }
//        }

        return localData;
    }

    /**
     * 在搜索sheet中填充localdata
     * @param searchSheet
     * @param localData
     * @return
     */
    public LocalData getLocalDataInSearchSheet(XSSFSheet searchSheet, int rowStart, int rowEnd, LocalData localData){

        SheetHeader sheetHeader = new SheetHeader().parseSheetHeaderRow(searchSheet);

        int minRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW));
        int minColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_COLUMN));
        int maxRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_ROW));
        int maxColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_COLUMN));
        int headerIndex = rowNameToIndex(sheetHeader.get(SheetHeader.SHEET_HEADER_ROW));

        String rowSearchStart = null;
        String rowSearchEnd = null;

        XSSFRow headerRow = this.curSheet.getRow(headerIndex);

        for(int rowIndex = Math.max(minRowIndex, rowStart); rowIndex <= Math.min(maxRowIndex, rowEnd); ){

            XSSFRow row = this.curSheet.getRow(rowIndex);
            XSSFCell rowCell;
            for(int colIndex = minColIndex; colIndex <= maxColIndex; ){
                rowCell = row.getCell(colIndex);
                if(!isEmptyCell(rowCell)){
                    String headerCellName = getCellString(headerRow.getCell(colIndex));
                    String rowCellValue = getCellString(rowCell);
                    Object rowCellTrueValue = null;
                    CellCite cellCite = new CellCite().parseCell(rowCellValue, localData);
                    switch (cellCite.type) {
                        case CellCite.VALUE:
                        case CellCite.CONSTANT_CITE:
                        case CellCite.DATA_SHEET_CITE:
                        case CellCite.FIELD_CITE:
                            rowCellTrueValue = cellCite.value;
                            break;
                        case CellCite.SHEET_CITE:
                            //跳转sheet
                            if(this.abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME).containsKey(cellCite.key)){
                                if(rowSearchStart != null && rowSearchEnd != null){
                                    switchSheet((String) cellCite.value);
                                    return getLocalDataInSearchSheet(curSheet, rowNameToIndex(rowSearchStart), rowNameToIndex(rowSearchEnd), localData);
                                }

                            } else if(this.abstractSheet.getMap(AbstractSheet.RESPONSE_SHEET_NAME).containsKey(cellCite.key)){
                                switchSheet((String) cellCite.value);
                                return getLocalDataInResponseSheet(curSheet, localData);
                            }
                            break;
                        default:
                    }
                    if(LocalData.PAGEID.equals(headerCellName)){
                        if((int)rowCellTrueValue != localData.getPageId()){
                            break;
                        }
                    }
                    if(LocalData.MIN_X.equals(headerCellName)){
                        if((int)rowCellTrueValue > localData.getX()){
                            break;
                        }
                    }
                    if(LocalData.MIN_Y.equals(headerCellName)){
                        if((int)rowCellTrueValue > localData.getY()){
                            break;
                        }
                    }
                    if(LocalData.MAX_X.equals(headerCellName)){
                        if((int)rowCellTrueValue < localData.getX()){
                            break;
                        }
                    }
                    if(LocalData.MAX_Y.equals(headerCellName)){
                        if((int)rowCellTrueValue < localData.getY()){
                            break;
                        }
                    }
                    if(LocalData.ROW_SEARCH_START.equals(headerCellName)){
                        rowSearchStart = (String) rowCellTrueValue;
                    }
                    if(LocalData.ROW_SEARCH_END.equals(headerCellName)){
                        rowSearchEnd = (String) rowCellTrueValue;
                    }
                    localData.addField(headerCellName, rowCellTrueValue);
                }
                colIndex++;
            }
            rowIndex++;
        }

        return localData;
    }

    /**
     * 在响应sheet中填充localdata
     * @param responseSheet
     * @param localData
     * @return
     */
    public LocalData getLocalDataInResponseSheet(XSSFSheet responseSheet, LocalData localData){

        return localData;
    }

    /**
     * 单元格引用类
     */
    public class CellCite {
        private static final String TAG = "CellCite";

        /**
         * 直接值
         */
        public static final int VALUE = 0;

        /**
         * 常量引用
         */
        public static final int CONSTANT_CITE= 1;

        /**
         * 数据表引用
         */
        public static final int DATA_SHEET_CITE= 2;

        /**
         * 字段引用
         */
        public static final int FIELD_CITE= 3;

        /**
         * sheet引用
         */
        public static final int SHEET_CITE= 4;

        /**
         * 引用类型
         */
        public int type;

        /**
         * 键名
         */
        public String key;

        /**
         * 真实值
         */
        public Object value;

        public CellCite parseCell(String cellString, LocalData localData){

            //解析类型
            if (cellString.contains("#")){
                if(cellString.contains("final")){ //如果是常量类型
                    this.type = CONSTANT_CITE;
                    this.key = cellString.replace("#final ","");
                    this.value = abstractSheet.getMap(AbstractSheet.CONSTANT).get(this.key);

                    LogUtil.e(TAG,this.value.toString());
                }
                else if (cellString.contains("data")){  //数据表类型的引用
                    this.type = DATA_SHEET_CITE;

                    cellString.replace("#data ",""); //去除标识

                    String[] parts = cellString.split("/");

                    int num = parts.length -1;

                    if (num == 2){
                        String dataname = parts[0];
                        String key1 = parts[1];
                        String key2 = parts[2];
                        this.key = key2;
                        this.value = dataSheetMap.get(dataname).getMap(key1).get(key2);
                    }
                    else if(num ==3){
                        String dataname = parts[0];
                        String key1 = parts[1];
                        String key2 = parts[2];
                        String key3 = parts[3];

                        LogUtil.e(TAG,"数据表的信息"+key1+key2+key3);
                        this.key = key3;
                        String temp = dataSheetMap.get(dataname).getMap(key1).get(key2);//验证是否符合条件的中间变量

                        List<String> dataList = new ArrayList<>();
                        Map<String, Map<String, String>> map = dataSheetMap.get(dataname).getData();
                        for(String k : map.keySet()){
                            if(dataSheetMap.get(dataname).getMap(k).get(key2).equals(temp)){
                                dataList.add(dataSheetMap.get(dataname).getMap(k).get(key3));
                            }
                        }
                        this.value = dataList;
                    }
                    else{
                        LogUtil.e(TAG,"数据表引用错误");
                    }
                }
                else if(cellString.contains("field")){  //字段类型的引用
                    this.type = FIELD_CITE;
                    this.key = cellString.replace("#field ","");
                    this.value = localData.getFieldValue(this.key);

                    LogUtil.e(TAG,"字段类型值为"+this.value.toString());
                }
                else if(cellString.contains("sheet")){ //sheet引用
                    this.type = SHEET_CITE;
                    this.key = cellString.replace("#sheet ","");
                    if(abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME).containsKey(this.key)){
                        this.value = abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME).get(this.key);
                    }
                    else if(abstractSheet.getMap(AbstractSheet.RESPONSE_SHEET_NAME).containsKey(this.key)){
                        this.value = abstractSheet.getMap(AbstractSheet.RESPONSE_SHEET_NAME).get(this.key);
                    }

                }
                else{
                    LogUtil.e(TAG,"不是正确的字段格式");
                }
            }
            else{
                this.type = VALUE;
                this.key = cellString;
                this.value = cellString;
            }

            //解析真实值
            return this;
        }

    }

    /**
     * 表头行
     */
    public class SheetHeader {
        private static final String TAG = "SheetHeader";

        /**
         * 表头行列号
         */
        private static final String SHEETHEADEERROWNUM = "1";

        private static final String SHEETHEADERCOLNAME = "A";
        private static final String EFFECTIVE_FIRST_ROW = "有效首行";
        private static final String EFFECTIVE_LAST_ROW = "有效尾行";
        private static final String EFFECTIVE_FIRST_COLUMN = "有效首列";
        private static final String EFFECTIVE_LAST_COLUMN = "有效尾列";
        private static final String SHEET_HEADER_ROW = "表头行";
        private static final String FILTER_FIRST_COLUMN = "筛选首列";
        private static final String FILTER_LAST_COLUMN = "筛选尾列";
        private static final String PRIMARY_KEY = "主键";

        private Map<String, String> sheetHeaderMap = new HashMap<>();

        private XSSFRow sheetHeaderRow;

        public SheetHeader() {
        }

        public SheetHeader add(String key, String value){
            this.sheetHeaderMap.put(key, value);
            return this;
        }

        /**
         * 解析目标sheet的表头行
         * @param sheet
         * @return
         */
        public SheetHeader parseSheetHeaderRow(XSSFSheet sheet){
            this.sheetHeaderRow = sheet.getRow(rowNameToIndex(SHEETHEADEERROWNUM));
            int firstCol = colNameToIndex(SHEETHEADERCOLNAME);
            LogUtil.e(TAG, "");

            //遍历表头行直至空单元格为止，生成sheetHeaderMap
            while(!isEmptyCell(sheetHeaderRow.getCell(firstCol))){
                add(getCellString(sheetHeaderRow.getCell(firstCol)),getCellString(sheetHeaderRow.getCell(firstCol+1)));
                firstCol = firstCol +2;
            }

            return this;
        }

        public String get(String key){
            if(this.sheetHeaderMap.containsKey(key)){
                return this.sheetHeaderMap.get(key);
            }
            LogUtil.e(TAG, "get(): 未找到目标key");
            return null;
        }
    }

    public class AbstractSheet {
        private static final String TAG = "AbstractSheet";

        private static final String ABSTRACT_SHEET_NAME = "摘要信息表";
        private static final String PAGE_PROPERTY = "版面属性";
        private static final String SEARCH_SHEET_NAME = "搜索sheet";
        private static final String DATA_SHEET_NAME = "数据sheet";
        private static final String RESPONSE_SHEET_NAME = "响应sheet";
        private static final String BITABLE_PROPERTY = "远程多维表格属性";
        private static final String CONSTANT = "常量";

        private XSSFSheet abstractSheet;

        /**
         * 摘要信息表的表头行
         */
        private SheetHeader sheetHeader;

        /**
         * 版面属性
         */
        private Map<String, String> pageProperty = new HashMap<>();

        /**
         * 搜索sheet
         */
        private Map<String, String> searchSheet = new HashMap<>();

        /**
         * 数据sheet
         */
        private Map<String, String> dataSheet = new HashMap<>();

        /**
         * 响应sheet
         */
        private Map<String, String> responseSheet = new HashMap<>();

        /**
         * 远程多维表格属性
         */
        private Map<String, String> bitableProperty = new HashMap<>();

        /**
         * 常量
         */
        private Map<String, String> constant = new HashMap<>();

        /**
         * 解析摘要信息表
         * @param workbook
         * @return
         */
        public AbstractSheet parseAbstractSheet(XSSFWorkbook workbook){

            this.abstractSheet = workbook.getSheet(ABSTRACT_SHEET_NAME);

            //解析表头行
            this.sheetHeader = new SheetHeader().parseSheetHeaderRow(this.abstractSheet);

            //获取搜索起点和搜索范围
            final int firstRowNum = rowNameToIndex(this.sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW));
            final int firstColNum = colNameToIndex(this.sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_COLUMN));
            int lastRowNum;

            //实时搜索坐标
            int rowNum;
            int colNum;

            //下一个搜索起点
            int nextRowNum;

            XSSFRow firstRow = this.abstractSheet.getRow(firstRowNum);

            XSSFCell cell = firstRow.getCell(firstColNum);
            lastRowNum = cell.getRowIndex();
            nextRowNum = lastRowNum+1;
            CellRangeAddress cellRangeAddress = null;
            if(ExcelUtil.inMerger(this.abstractSheet, cell)){
                cellRangeAddress = ExcelUtil.getMergedCellAddress(this.abstractSheet,cell);
                cell = this.abstractSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
                lastRowNum = cellRangeAddress.getLastRow();
                nextRowNum = lastRowNum+1;
            }

            while (!isEmptyCell(cell)){
                String fieldName = getCellString(cell);
                LogUtil.e(TAG, "parseAbstractSheet(): 搜索到字段: "+fieldName);
                Map<String, String> map = getMap(fieldName);
                if(map != null){
                    //获取字段键值对搜索起点
                    rowNum = cell.getRowIndex();
                    colNum = cell.getColumnIndex()+1;

                    //循环存储键值对，直到键为空或到达该字段最后一行，循环停止；若值为空，存储null
                    while(!isEmptyCell(this.abstractSheet.getRow(rowNum).getCell(colNum)) && rowNum <= lastRowNum){
                        if(!isEmptyCell(this.abstractSheet.getRow(rowNum).getCell(colNum+1))){
                            map.put(getCellString(this.abstractSheet.getRow(rowNum).getCell(colNum)),getCellString(this.abstractSheet.getRow(rowNum).getCell(colNum+1)));
                            rowNum = rowNum +1;
                        }
                        else{
                            LogUtil.e(TAG,"键所对应的值为空");
                            map.put(getCellString(this.abstractSheet.getRow(rowNum).getCell(colNum)),null);
                            rowNum = rowNum +1;
                        }
                    }
                }

                //确定下一字段搜索起点
                cell = this.abstractSheet.getRow(nextRowNum).getCell(firstColNum);
                lastRowNum = cell.getRowIndex();
                nextRowNum = lastRowNum+1;
                if(ExcelUtil.inMerger(this.abstractSheet, cell)){
                    cellRangeAddress = ExcelUtil.getMergedCellAddress(this.abstractSheet,cell);
                    cell = this.abstractSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
                    lastRowNum = cellRangeAddress.getLastRow();
                    nextRowNum = lastRowNum+1;
                }

            }

            return this;
        }

        /**
         * 根据字段名获取对应map
         * @param name
         * @return
         */
        private Map<String, String> getMap(String name){

            if(PAGE_PROPERTY.equals(name)){
                return pageProperty;
            } else if(SEARCH_SHEET_NAME.equals(name)){
                return searchSheet;
            }else if(DATA_SHEET_NAME.equals(name)){
                return dataSheet;
            }
            else if(RESPONSE_SHEET_NAME.equals(name)){
                return responseSheet;
            }
            else if(BITABLE_PROPERTY.equals(name)){
                return bitableProperty;
            }else if(CONSTANT.equals(name)){
                return constant;
            }
            else{
                LogUtil.e(TAG, "getMap(): 没有对应的map");
                return null;
            }
        }

        @Override
        public String toString() {
            return "AbstractSheet{" +
                    "pageProperty=" + pageProperty +
                    ", searchSheet=" + searchSheet +
                    ", dataSheet=" + dataSheet +
                    ", responseSheet=" + responseSheet +
                    ", bitableProperty=" + bitableProperty +
                    ", constant=" + constant +
                    '}';
        }
    }

}

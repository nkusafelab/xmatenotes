package com.example.xmatenotes.logic.manager;

import com.example.xmatenotes.util.ExcelUtil;
import com.example.xmatenotes.util.LogUtil;

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

public class ExcelManager extends ExcelHelper{

    private static final String TAG = "ExcelManager";

    private static final ExcelManager excelManager = new ExcelManager();

    /**
     * 摘要信息表数据
     */
    private AbstractSheet abstractSheet;

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
    public ExcelManager init(String excelName){

        //打开目标表格
        openExcel("excel/"+excelName);

        //解析摘要信息表
        if(switchSheet(AbstractSheet.ABSTRACT_SHEET_NAME)){
            this.abstractSheet = new AbstractSheet().parseAbstractSheet(this.curWorkbook);
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


        return this;
    }

    /**
     * 解析目标数据表
     * @param dataSheetName 数据表名，确保为数据表
     * @return
     */
    public DataSheet parseDataSheet(String dataSheetName){
        DataSheet dataSheet = new DataSheet(dataSheetName);

        //解析数据表头

        //遍历数据表，填充dataSheet

        return dataSheet;

    }

    /**
     * 表头行
     */
    public class SheetHeader {
        private static final String TAG = "SheetHeader";

        /**
         * 表头行列号
         */
        private static final int SHEETHEADEERROWNUM = 1;

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
            this.sheetHeaderRow = sheet.getRow(numToIndex(SHEETHEADEERROWNUM));
            int firstCol = colNameToIndex(SHEETHEADERCOLNAME);
            LogUtil.e(TAG, "");

            //遍历表头行直至空单元格为止，生成sheetHeaderMap
            while(!ExcelUtil.isEmptyCell(sheetHeaderRow.getCell(firstCol))){
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
            final int firstRowNum = numToIndex(Integer.parseInt(this.sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW)));
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

            while (!ExcelUtil.isEmptyCell(cell)){
                String fieldName = getCellString(cell);
                LogUtil.e(TAG, "parseAbstractSheet(): 搜索到字段: "+fieldName);
                Map<String, String> map = getMap(fieldName);
                if(map != null){
                    //获取字段键值对搜索起点
                    rowNum = cell.getRowIndex();
                    colNum = cell.getColumnIndex()+1;

                    //循环存储键值对，直到键为空或到达该字段最后一行，循环停止；若值为空，存储null
                    while(!ExcelUtil.isEmptyCell(this.abstractSheet.getRow(rowNum).getCell(colNum)) && rowNum <= lastRowNum){
                        if(!ExcelUtil.isEmptyCell(this.abstractSheet.getRow(rowNum).getCell(colNum+1))){
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

    }

}

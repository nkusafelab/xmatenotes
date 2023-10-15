package com.example.xmatenotes.logic.manager;


import android.content.Context;
import android.util.Log;

import com.example.xmatenotes.logic.dao.IExcelDao;
import com.example.xmatenotes.logic.model.handwriting.BaseDot;
import com.example.xmatenotes.logic.network.BitableManager;
import com.example.xmatenotes.util.LogUtil;
import com.example.xmatenotes.util.ExcelUtil;

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

public class ExcelManager extends ExcelHelper implements IExcelDao {

    private static final String TAG = "ExcelManager";

    private static final String SEARCH_START = "Search1";

    private static final ExcelManager excelManager = new ExcelManager();
    private static final BitableManager bitableManager = BitableManager.getInstance();

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
        if(!dataMap.isEmpty()){
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
        }

        //初始化BitableManager
        Map<String, String> bitableMap = this.abstractSheet.getMap(AbstractSheet.BITABLE_PROPERTY);
        if(bitableMap != null && !bitableMap.isEmpty()){
            bitableManager.initial(bitableMap.get("AppId"), bitableMap.get("AppSecret"), bitableMap.get("Apptoken"));
            LogUtil.e(TAG, "init(): 初始化BitableManager完毕");
        }

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

    public Map<String, DataSheet> getDataSheetMap() {
        return dataSheetMap;
    }

    /**
     *
     * @param x
     * @param y
     * @param pageId
     * @param command
     * @param roleName 身份信息&权限标识符
     * @return
     */
    public LocalData getLocalData(int x, int y, int pageId, String command, String roleName){
        LocalData localData = new LocalData(x, y, pageId, command, roleName);
        LogUtil.e(TAG, "getLocalData: localData: "+localData);

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
     * 在搜索sheet中填充localdata，筛选条件为坐标匹配
     * @param searchSheet
     * @param localData
     * @return null表示没有查找到匹配的区域或没有权限
     */
    public LocalData getLocalDataInSearchSheet(XSSFSheet searchSheet, int rowStart, int rowEnd, LocalData localData){

        LogUtil.e(TAG, "getLocalDataInSearchSheet(): 开始搜索sheet: "+searchSheet.getSheetName());
        SheetHeader sheetHeader = new SheetHeader().parseSheetHeaderRow(searchSheet);

        int minRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW));
        int minColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_COLUMN));
        int maxRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_ROW));
        int maxColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_COLUMN));
        int headerIndex = rowNameToIndex(sheetHeader.get(SheetHeader.SHEET_HEADER_ROW));

        String rowSearchStart = null;
        String rowSearchEnd = null;

        XSSFRow headerRow = this.curSheet.getRow(headerIndex);

        int realMinRowIndex = Math.max(minRowIndex, rowStart);//实时最小行index
        int realMaxRowIndex = Math.min(maxRowIndex, rowEnd);//实时最大行index

        int colIndex = minColIndex;//实时列index
        int resetColIndex = colIndex;//回退列inde

        for(int rowIndex = realMinRowIndex; rowIndex <= realMaxRowIndex; ){

            boolean isCompareCoordinates = false;//是否比对过坐标
            XSSFRow row = this.curSheet.getRow(rowIndex);
            XSSFCell rowCell;
            while (colIndex <= maxColIndex){
                Log.e(TAG, "getLocalDataInSearchSheet: row: "+row.getRowNum()+" col: "+colIndex);
                LogUtil.e(TAG, "getLocalDataInSearchSheet: colIndex:"+colIndex+" <= maxColIndex:"+maxColIndex+": "+(colIndex <= maxColIndex));
                rowCell = row.getCell(colIndex);
                LogUtil.e(TAG, "getLocalDataInSearchSheet: rowCell: "+rowCell);
                CellRangeAddress cellRangeAddress = null;
                if(rowCell != null && ExcelUtil.inMerger(this.curSheet, rowCell)){
                    LogUtil.e(TAG, "getLocalDataInSearchSheet: 合并单元格");
                    cellRangeAddress = ExcelUtil.getMergedCellAddress(this.curSheet,rowCell);
                    rowCell = this.curSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
                    if(!isCompareCoordinates && this.abstractSheet.getMap(AbstractSheet.AREA_CODE_IDENTIFICATION).containsKey(getCellString(headerRow.getCell(rowCell.getColumnIndex())))){
                        resetColIndex = colIndex;
                        int quickColIndex = colIndex;
                        int firstRowNum = cellRangeAddress.getFirstRow();
                        int lastRowNum = cellRangeAddress.getLastRow();
                        XSSFRow firstRow = this.curSheet.getRow(firstRowNum);
                        XSSFRow lastRow = this.curSheet.getRow(lastRowNum);
                        while (quickColIndex <= maxColIndex){
                            LogUtil.e(TAG, "getLocalDataInSearchSheet: quickColIndex: "+quickColIndex);
                            String headerCellName = getCellString(headerRow.getCell(quickColIndex));
                            if(LocalData.MIN_PAGEID.equals(headerCellName)){
                                int pageId = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(firstRow.getCell(quickColIndex)), localData).value));
                                int x = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(firstRow.getCell(quickColIndex+1)), localData).value));
                                int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(firstRow.getCell(quickColIndex+2)), localData).value));
                                BaseDot minDot = parseDot(pageId, x, y);
                                BaseDot localDot = parseDot(localData.getPageId(), localData.getX(), localData.getY());
                                Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+minDot+"): "+(localDot.compareTo(minDot)));
                                if(localDot.compareTo(minDot) > 0){
                                    quickColIndex += 3;
                                    continue;
                                } else {
                                    rowIndex = cellRangeAddress.getLastRow()+1;
                                    break;
                                }
                            }
                            if(LocalData.MAX_PAGEID.equals(headerCellName)){
                                int pageId = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(lastRow.getCell(quickColIndex)), localData).value));
                                int x = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(lastRow.getCell(quickColIndex+1)), localData).value));
                                int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(lastRow.getCell(quickColIndex+2)), localData).value));
//                                int pageId = getCellInt(lastRow.getCell(quickColIndex));
//                                int x = getCellInt(lastRow.getCell(quickColIndex+1));
//                                int y = getCellInt(lastRow.getCell(quickColIndex+2));
                                BaseDot minDot = parseDot(pageId, x, y);
                                BaseDot localDot = parseDot(localData.getPageId(), localData.getX(), localData.getY());
                                Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+minDot+"): "+(localDot.compareTo(minDot)));
                                if(localDot.compareTo(minDot) < 0){
                                    realMaxRowIndex = cellRangeAddress.getLastRow();
                                    localData.addField(getCellString(headerRow.getCell(colIndex)), getCellString(rowCell));
                                    colIndex++;
                                    break;
                                } else {
                                    rowIndex = cellRangeAddress.getLastRow()+1;
                                    break;
                                }
                            }
                            if(LocalData.MIN_X.equals(headerCellName)){
                                int x = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(firstRow.getCell(quickColIndex)), localData).value));
                                int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(firstRow.getCell(quickColIndex+1)), localData).value));

                                BaseDot minDot = new BaseDot(x, y);
                                BaseDot localDot = new BaseDot(localData.getX(), localData.getY());
                                Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+minDot+"): "+(localDot.compareTo(minDot)));
                                if(localDot.compareTo(minDot) > 0){
                                    quickColIndex += 2;
                                    continue;
                                } else {
                                    rowIndex = cellRangeAddress.getLastRow()+1;
                                    break;
                                }
                            }
                            if(LocalData.MAX_X.equals(headerCellName)) {
                                int x = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(lastRow.getCell(quickColIndex)), localData).value));
                                int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(lastRow.getCell(quickColIndex+1)), localData).value));

                                BaseDot maxDot = new BaseDot(x, y);
                                BaseDot localDot = new BaseDot(localData.getX(), localData.getY());
                                Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+maxDot+"): "+(localDot.compareTo(maxDot)));
                                if (localDot.compareTo(maxDot) < 0) {
                                    realMaxRowIndex = cellRangeAddress.getLastRow();
                                    localData.addField(getCellString(headerRow.getCell(colIndex)), getCellString(rowCell));
                                    colIndex++;
                                    break;
                                } else {
                                    rowIndex = cellRangeAddress.getLastRow()+1;
                                    break;
                                }
                            }


//                            if(LocalData.PAGEID.equals(headerCellName)){
//                                if(getCellInt(firstRow.getCell(quickColIndex)) > localData.getX()){
//                                    rowIndex = cellRangeAddress.getLastRow()+1;
//                                    break;
//                                }
//                            }
//
//                            if(LocalData.MIN_X.equals(headerCellName)){
//                                if(getCellInt(firstRow.getCell(quickColIndex)) > localData.getX()){
//                                    rowIndex = cellRangeAddress.getLastRow()+1;
//                                    break;
//                                }
//                            }
//                            if(LocalData.MIN_Y.equals(headerCellName)){
//                                if(getCellInt(firstRow.getCell(quickColIndex)) > localData.getY()){
//                                    rowIndex = cellRangeAddress.getLastRow()+1;
//                                    break;
//                                }
//                            }
//                            if(LocalData.MAX_X.equals(headerCellName)){
//                                if(getCellInt(lastRow.getCell(quickColIndex)) < localData.getX()){
//                                    rowIndex = cellRangeAddress.getLastRow()+1;
//                                    break;
//                                }
//                            }
//                            if(LocalData.MAX_Y.equals(headerCellName)){
//                                if(getCellInt(lastRow.getCell(quickColIndex)) < localData.getY()){
//                                    rowIndex = cellRangeAddress.getLastRow()+1;
//                                    break;
//                                } else {
//                                    realMaxRowIndex = cellRangeAddress.getLastRow();
//                                    localData.addField(getCellString(headerRow.getCell(colIndex)), getCellString(rowCell));
//                                    colIndex++;
//                                    break;
//                                }
//                            }
                            quickColIndex++;
                        }
                        break;
                    }

                }
                if(!isEmptyCell(rowCell)){
                    String headerCellName = getCellString(headerRow.getCell(colIndex));
                    String rowCellStringValue = getCellString(rowCell);
                    Object rowCellTrueValue = null;
                    CellCite cellCite = new CellCite().parseCell(headerCellName, rowCellStringValue, localData);
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
                                LogUtil.e(TAG,"链接表key"+cellCite.key);

                                if(rowSearchStart != null && rowSearchEnd != null){

                                    LogUtil.e(TAG,"调用切换之前"+curSheet.getSheetName());
                                    switchSheet((String) cellCite.value);

                                    LogUtil.e(TAG,"链接表value"+cellCite.value);

                                    LogUtil.e(TAG,curSheet.getSheetName());

                                    return getLocalDataInSearchSheet(curSheet, rowNameToIndex(rowSearchStart), rowNameToIndex(rowSearchEnd), localData);
                                }

                            } else if(this.abstractSheet.getMap(AbstractSheet.RESPONSE_SHEET_NAME).containsKey(cellCite.key)){
                                switchSheet((String) cellCite.value);
                                return getLocalDataInResponseSheet(curSheet, localData);
                            }
                            break;
                        default:
                    }

                    if(LocalData.MIN_PAGEID.equals(headerCellName)){
                        int pageId = Integer.parseInt(String.valueOf(rowCellTrueValue));
                        int x = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(row.getCell(rowCell.getColumnIndex()+1)), localData).value));
                        int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(row.getCell(rowCell.getColumnIndex()+2)), localData).value));
                        BaseDot minDot = parseDot(pageId, x, y);
                        BaseDot localDot = parseDot(localData.getPageId(), localData.getX(), localData.getY());
                        Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+minDot+"): "+(localDot.compareTo(minDot)));
                        if(localDot.compareTo(minDot) > 0){
                            localData.setLeft(x);
                            localData.setTop(y);
                            colIndex += 3;
                            continue;
                        } else {
                            colIndex = resetColIndex+1;
                            rowIndex++;
                            break;
                        }
                    }
                    if(LocalData.MAX_PAGEID.equals(headerCellName)){
                        int pageId = Integer.parseInt(String.valueOf(rowCellTrueValue));
                        int x = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(row.getCell(rowCell.getColumnIndex()+1)), localData).value));
                        int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(row.getCell(rowCell.getColumnIndex()+2)), localData).value));
                        BaseDot maxDot = parseDot(pageId, x, y);
                        BaseDot localDot = parseDot(localData.getPageId(), localData.getX(), localData.getY());
                        Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+maxDot+"): "+(localDot.compareTo(maxDot)));
                        if(localDot.compareTo(maxDot) < 0){
                            localData.setRight(x);
                            localData.setBottom(y);
                            colIndex += 3;
                            isCompareCoordinates = true;
                            continue;
                        } else {
                            colIndex  = resetColIndex+1;
                            rowIndex++;
                            break;
                        }
                    }
//                    if(LocalData.PAGEID.equals(headerCellName)){
//                        if((int)rowCellTrueValue != localData.getPageId()){
//                            break;
//                        }
//                    }
                    if(LocalData.MIN_X.equals(headerCellName)){
                        int x = Integer.parseInt(String.valueOf(rowCellTrueValue));
                        int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(row.getCell(rowCell.getColumnIndex()+1)), localData).value));
                        BaseDot minDot = new BaseDot(x, y);
                        BaseDot localDot = new BaseDot(localData.getX(), localData.getY());
                        Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+minDot+"): "+(localDot.compareTo(minDot)));
                        if(localDot.compareTo(minDot) > 0){
                            localData.setLeft(x);
                            localData.setTop(y);
                            colIndex += 2;
                            continue;
                        } else {
                            colIndex = resetColIndex+1;
                            rowIndex++;
                            break;
                        }
                    }
//                    if(LocalData.MIN_Y.equals(headerCellName)){
//                        if((int)rowCellTrueValue > localData.getY()){
//                            colIndex -= 1;
//                            rowIndex++;
//                            break;
//                        }
//                    }
                    if(LocalData.MAX_X.equals(headerCellName)){
                        int x = Integer.parseInt(String.valueOf(rowCellTrueValue));
                        int y = Integer.parseInt(String.valueOf(new CellCite().parseCell(headerCellName,getCellString(row.getCell(rowCell.getColumnIndex()+1)), localData).value));
                        BaseDot maxDot = new BaseDot(x, y);
                        BaseDot localDot = new BaseDot(localData.getX(), localData.getY());
                        Log.e(TAG, "getLocalDataInSearchSheet: "+localDot+".compareTo("+maxDot+"): "+(localDot.compareTo(maxDot)));
                        if(localDot.compareTo(maxDot) < 0){
                            localData.setRight(x);
                            localData.setBottom(y);
                            colIndex += 2;
                            isCompareCoordinates = true;
                            continue;
                        } else {
                            colIndex = resetColIndex+1;
                            rowIndex++;
                            break;
                        }
                    }
                    if(LocalData.PAGEID.equals(headerCellName)){
                        int pageId = Integer.parseInt(String.valueOf(rowCellTrueValue));
                        if(pageId == localData.getPageId()){
                            colIndex++;
                            continue;
                        } else {
                            rowIndex++;
                            break;
                        }
                    }
//                    if(LocalData.MAX_Y.equals(headerCellName)){
//                        if((int)rowCellTrueValue < localData.getY()){
//                            colIndex -= 3;
//                            rowIndex++;
//                            break;
//                        } else {
//                            isCompareCoordinates = true;
//                        }
//                    }
                    if(LocalData.ROW_SEARCH_START.equals(headerCellName)){
                        rowSearchStart = (String) rowCellTrueValue;
                    }
                    if(LocalData.ROW_SEARCH_END.equals(headerCellName)){
                        rowSearchEnd = (String) rowCellTrueValue;
                    }
                    if(LocalData.LIMIT.equals(headerCellName)){
                        //检查权限
                        if(!checkLimit(rowCellTrueValue.toString(), localData)){
                            LogUtil.e(TAG, "getLocalDataInSearchSheet(): 不具有目标区域操作权限！");
                            return null;
                        }
                    }
                    localData.addField(headerCellName, rowCellTrueValue);
                    LogUtil.e(TAG, "getLocalDataInSearchSheet(): 存入键值对: "+headerCellName+" : "+rowCellTrueValue);
                }
                colIndex++;
            }
            if(colIndex >= maxColIndex){
                //无后续响应
                return localData;
            }
//            rowIndex++;
        }

        LogUtil.e(TAG, "getLocalDataInSearchSheet(): 未找到匹配的目标区域！");
        return null;
    }

    /**
     * 将局部坐标转换为幅面统一坐标系坐标
     * @param pageId
     * @param x
     * @param y
     * @return
     */
    public BaseDot parseDot(int pageId, int x, int y){
        int unifiedX = 0;
        int unifiedY = 0;

        //转换统一坐标
        String minX  = pageId+"MINX";

        LogUtil.e(TAG,"拼凑出的常量的键为"+minX);

        String minY = pageId+"MINY";

        LogUtil.e(TAG,"拼凑出的常量的键为"+minY);

        unifiedX = x + Integer.parseInt(abstractSheet.getMap(AbstractSheet.CONSTANT).get(minX));

        unifiedY = y +  Integer.parseInt(abstractSheet.getMap(AbstractSheet.CONSTANT).get(minY));

        return new BaseDot(unifiedX, unifiedY);
    }

    /**
     * 根据权限信息检查是否具有目标区域操作权限
     * @param rowCellTrueValue
     * @param localData
     * @return
     */
    private boolean checkLimit(String rowCellTrueValue, LocalData localData) {

        if(localData.getRole() == null){
            return true;
        } else if(rowCellTrueValue.contains(localData.getRole())){
            return true;
        } else{
            return false;
        }
    }

    /**
     * 在响应sheet中填充localdata，筛选条件为行匹配
     * @param responseSheet
     * @param localData
     * @return
     */
    public LocalData getLocalDataInResponseSheet(XSSFSheet responseSheet, LocalData localData){

        LogUtil.e(TAG, "getLocalDataInResponseSheet(): 开始搜索sheet: "+responseSheet.getSheetName());

        SheetHeader sheetHeader = new SheetHeader().parseSheetHeaderRow(responseSheet);

        int minRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW));
        int minColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_COLUMN));
        int maxRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_ROW));
        int maxColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_COLUMN));
        int headerIndex = rowNameToIndex(sheetHeader.get(SheetHeader.SHEET_HEADER_ROW));
        int filterFirstCol = colNameToIndex(sheetHeader.get(SheetHeader.FILTER_FIRST_COLUMN));
        int filterLastCol = colNameToIndex(sheetHeader.get(SheetHeader.FILTER_LAST_COLUMN));


        XSSFRow headerRow = this.curSheet.getRow(headerIndex);

        //生成筛选匹配值列表
        List<String> filterList = new ArrayList<>();
        for(int colIndex = filterFirstCol; colIndex <= filterLastCol;colIndex++){

            LogUtil.e(TAG,getCellString(headerRow.getCell(colIndex)));

            filterList.add((String) localData.getFieldValue(getCellString(headerRow.getCell(colIndex))));
        }

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> mapList = new ArrayList<>();

        for(int rowIndex = minRowIndex; rowIndex <= maxRowIndex; rowIndex++){

            XSSFRow row = this.curSheet.getRow(rowIndex);
            int colIndex = filterFirstCol;
            XSSFCell rowCell;
            while (colIndex <= maxColIndex) {
                rowCell = row.getCell(colIndex);
                CellRangeAddress cellRangeAddress = null;
                if (ExcelUtil.inMerger(this.curSheet, rowCell)) {
                    cellRangeAddress = ExcelUtil.getMergedCellAddress(this.curSheet, rowCell);
                    rowCell = this.curSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
                }
                if(!isEmptyCell(rowCell)) {
                    String rowCellStringValue = getCellString(rowCell);
                    Object rowCellTrueValue = null;
                    CellCite cellCite = new CellCite().parseCell(getCellString(headerRow.getCell(colIndex)),rowCellStringValue, localData);
                    switch (cellCite.type) {
                        case CellCite.VALUE:
                        case CellCite.CONSTANT_CITE:
                        case CellCite.DATA_SHEET_CITE:
                        case CellCite.FIELD_CITE:
                            rowCellTrueValue = cellCite.value;
                            break;
                        case CellCite.SHEET_CITE:
                            //跳转sheet
                            if (this.abstractSheet.getMap(AbstractSheet.RESPONSE_SHEET_NAME).containsKey(cellCite.key)) {
                                switchSheet((String) cellCite.value);
                                return getLocalDataInResponseSheet(curSheet, localData);
                            }
                            break;
                        default:
                    }
                    if(colIndex <= filterLastCol){
                        //角色需要考虑contains而不是equals

                        LogUtil.e(TAG, "getLocalDataInResponseSheet: "+rowCellTrueValue+" == "+filterList.get(colIndex - filterFirstCol));
                        if(!rowCellTrueValue.equals(filterList.get(colIndex - filterFirstCol))){
                            break;
                        }
                    } else {
                        String key = getCellString(headerRow.getCell(colIndex));
                        Object value = rowCellTrueValue;
                        map.put(key, value);
                        localData.addField(key, value);
                        LogUtil.e(TAG, "getLocalDataInResponseSheet(): 存入键值对: "+key+" : "+value+" 行index： "+rowIndex);
                    }
                }
                colIndex++;
            }
            if(colIndex > maxColIndex){
                mapList.add(map);
                map = new HashMap<>();
                break;
            }

        }

        localData.addField(responseSheet.getSheetName(), mapList);

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


        public CellCite parseCell(String headerCellName, String cellString, LocalData localData){

            //解析类型
            if (cellString.charAt(0) == '#'){
                if(cellString.contains("final")){ //如果是常量类型
                    LogUtil.e("判断出是常量类型","");
                    this.type = CONSTANT_CITE;
                    this.key = cellString.replace("#final ","");
                    LogUtil.e(TAG,this.key);
                    LogUtil.e(TAG,abstractSheet.getMap(AbstractSheet.CONSTANT).toString());
                    this.value = abstractSheet.getMap(AbstractSheet.CONSTANT).get(this.key);

                    LogUtil.e(TAG,this.value.toString());

                }
                else if (cellString.contains("data")){  //数据表类型的引用
                    this.type = DATA_SHEET_CITE;

                    String newcellString = cellString.replace("#data ",""); //去除标识

                    String[] parts = newcellString.split("/");

                    int num = parts.length -1;

                    if (num == 2){
                        String dataname = parts[0];

                        LogUtil.e(TAG,"解析出的名字"+dataname);
                        String key1 = parts[1];
                        LogUtil.e(TAG,"解析出的主键1"+key1);
                        String key2 = parts[2];
                        LogUtil.e(TAG,"解析出的主键2"+key2);
                        this.key = key2;
                        Log.e(TAG, "parseCell: dataSheetMap.get(dataname): "+dataSheetMap.get(dataname));
                        this.value = dataSheetMap.get(dataname).getMap(key1).get(key2);
                        Log.e(TAG, "parseCell: this.value: "+this.value);
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
                else if(headerCellName.equals("筛选条件")){   //对于筛选条件的处理，直接在此拼接成可处理的字符串
                    this.type = FIELD_CITE;
                    if (cellString.contains(";")){  //如果是多个筛选条件，则进行AND拼接

                        String[] split = cellString.split(";");

                        LogUtil.e(TAG,"判断出是多个筛选条件进行写入");

                        String now1 = split[0].replace("#field ","");

                        String now2 = split[1].replace("#field ","");

                        Object obj1 = localData.getFieldValue(now1);

                        Object obj2 = localData.getFieldValue(now2);

                        String result1 = "";

                        String result2 = "";

                        if(obj1 instanceof String){
                            String real = obj1.toString();

                            result1 = "CurrentValue.[" + now1 + "]=\"" + real + "\"";

                            LogUtil.e(TAG,"拼接出的筛选条件的值为" + result1);
                        }
                        else if(obj1 != null){

                            int tag = 1;
                            for (String s : (List<String>) obj1) {
                                if(tag == 1){
                                    result1  = "OR(CurrentValue.[" + now1 +"]=\"" + s + "\")";
                                    tag = tag +1;
                                }
                                else{
                                    result1 = result1.substring(0,result1.length()-1);

                                    result1 = result1 + "，CurrentValue.[" + now1 + "]=\"" + s + "\")";
                                }
                            }
                        }
                        if(obj2 instanceof String){
                            String real = obj2.toString();
                            String result = "CurrentValue.[" + now2 + "]=\"" + real + "\"";
                            result2 = result;
                            LogUtil.e(TAG,"拼接出的筛选条件的值为" + result2);
                        }
                        else if(obj2 != null){

                            int tag = 1;
                            for (String s : (List<String>) obj2) {
                                if(tag == 1){
                                    result2  = "OR(CurrentValue.[" + now2 +"]=\"" + s + "\")";
                                    tag = tag +1;
                                }
                                else{
                                    result2 = result2.substring(0,result2.length()-1);

                                    result2 = result2 + "，CurrentValue.[" + now2 + "]=\"" + s + "\")";
                                }
                            }
                        }

                        String result = "";

                        result = "AND(" + result1 + "，" + result2 + ")";

                        LogUtil.e(TAG,"nihao"+result);

                        this.key = now2;

                        this.value = result;
                    }
                    else{
                        String now = cellString.replace("#field ","");//这样说明是只有一个筛选条件，可能进行or拼接或者不拼接

                        this.key = now;

                        Object obj = localData.getFieldValue(now);
                        if(obj instanceof String ){
                            LogUtil.e(TAG,"判断出是长压的单值区域");
                            String real = obj.toString();

                            String result = "CurrentValue.[" + now + "]=\"" + real + "\"";
                            this.value = result;
                            LogUtil.e(TAG,"拼接出的筛选条件的值为" + result);
                        }
                        else if(obj != null){  //不是字符串类型就按照列表来处理

                            LogUtil.e(TAG,"判断出是长压的多值区域");

                            int tag = 1;
                            String result = "";
                            for (String s : (List<String>) obj) {
                                if(tag == 1){

                                    result  = "OR(CurrentValue.[" + now +"]=\"" + s + "\")";
                                    tag = tag +1;
                                }
                                else{
                                    result = result.substring(0,result.length()-1);

                                    result = result + "，CurrentValue.[" + now + "]=\"" + s + "\")";
                                }
                            }

                            this.value = result;



                        }
                    }

                }
                //对于处了筛选条件字段下的这些引用，还是直接取值。

                else if(cellString.contains("field") && !cellString.contains(";") && !headerCellName.equals("筛选条件")){  //字段类型的引用
                    this.type = FIELD_CITE;
                    this.key = cellString.replace("#field ","");
                    this.value = localData.getFieldValue(this.key);

                    LogUtil.e(TAG,"字段类型值为"+this.value.toString());
                }
                else if(cellString.contains("sheet")){ //sheet引用

                    this.type = SHEET_CITE;
                    this.key = cellString.replace("#sheet ","");
                    LogUtil.e(TAG,this.key);
                    if(abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME).containsKey(this.key)){
                        this.value = abstractSheet.getMap(AbstractSheet.SEARCH_SHEET_NAME).get(this.key);
                        LogUtil.e(TAG,"确认是搜索表的map"+this.value.toString());
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
                if(cellString.contains(";")){
                    String[] split = cellString.split(";");
                    List<String> list = new ArrayList<>();
                    for(String s : split){
                        list.add(s);
                    }
                    this.value = list;
                }
                else{
                    this.value = cellString;
                }

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
        private static final String AREA_CODE_IDENTIFICATION = "区域编码标识";
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
         * 区域编码标识
         */
        private Map<String, String> areaCodeSheet = new HashMap<>();

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
            int minRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_ROW));
            int minColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_FIRST_COLUMN));
            int maxRowIndex = rowNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_ROW));
            int maxColIndex = colNameToIndex(sheetHeader.get(SheetHeader.EFFECTIVE_LAST_COLUMN));
            int lastRowIndex;

            //实时搜索坐标
            int realRowIndex;
            int realColIndex;
            int rowNum;
            int colNum;
            //下一个搜索起点
            int nextRowNum;
            XSSFRow realRow;
            XSSFCell realCell;
            for(realRowIndex = minRowIndex, realColIndex = minColIndex;realRowIndex <= maxRowIndex && realColIndex <= maxColIndex;){
                realRow = this.abstractSheet.getRow(realRowIndex);
                realCell = realRow.getCell(realColIndex);
                lastRowIndex = realRowIndex;
                CellRangeAddress cellRangeAddress = null;

                if (realCell != null && !isEmptyCell(realCell)){
                    if(ExcelUtil.inMerger(this.abstractSheet, realCell)){
                        cellRangeAddress = ExcelUtil.getMergedCellAddress(this.abstractSheet,realCell);
                        realCell = this.abstractSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
                        lastRowIndex = cellRangeAddress.getLastRow();
                    }

                    String fieldName = getCellString(realCell);
                    LogUtil.e(TAG, "parseAbstractSheet(): 搜索到字段: "+fieldName);
                    Map<String, String> map = getMap(fieldName);
                    if(map != null){
                        //获取字段键值对搜索起点
                        realRowIndex = realCell.getRowIndex();
                        realColIndex = realCell.getColumnIndex()+1;
                        //循环存储键值对，直到键为空或到达该字段最后一行，循环停止；若值为空，存储null
                        XSSFCell keyCell;
                        XSSFCell valueCell;
                        while(realRowIndex <= lastRowIndex){
                            keyCell = this.abstractSheet.getRow(realRowIndex).getCell(realColIndex);
                            if(keyCell != null && !isEmptyCell(keyCell)){
                                valueCell = this.abstractSheet.getRow(realRowIndex).getCell(realColIndex+1);
                                if(valueCell != null && !isEmptyCell(valueCell)){
                                    map.put(getCellString(keyCell),getCellString(valueCell));
                                    LogUtil.e(TAG, "parseAbstractSheet: 存入键值对: "+getCellString(keyCell)+" : "+getCellString(valueCell));
                                }
                            } else {
                                break;
                            }
                            realRowIndex++;
                        }
                        LogUtil.e(TAG,"parseAbstractSheet: 获得映射: "+map.toString());
                    }
                    realRowIndex = lastRowIndex +1;
                    realColIndex--;
                }
            }

//            XSSFRow firstRow = this.abstractSheet.getRow(realRowIndex);
//
//            XSSFCell cell = firstRow.getCell(realColIndex);
////            lastRowNum = cell.getRowIndex();
////            nextRowNum = lastRowNum+1;
//            CellRangeAddress cellRangeAddress = null;
//
//            while (!isEmptyCell(cell)){
//                lastRowIndex = cell.getRowIndex();
//                nextRowNum = lastRowIndex+1;
//                if(ExcelUtil.inMerger(this.abstractSheet, cell)){
//                    cellRangeAddress = ExcelUtil.getMergedCellAddress(this.abstractSheet,cell);
//                    cell = this.abstractSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
//                    lastRowIndex = cellRangeAddress.getLastRow();
//                    nextRowNum = lastRowIndex+1;
//                }
//
//                String fieldName = getCellString(cell);
//                LogUtil.e(TAG, "parseAbstractSheet(): 搜索到字段: "+fieldName);
//                Map<String, String> map = getMap(fieldName);
//                if(map != null){
//                    //获取字段键值对搜索起点
//                    rowNum = cell.getRowIndex();
//                    colNum = cell.getColumnIndex()+1;
//
//                    //循环存储键值对，直到键为空或到达该字段最后一行，循环停止；若值为空，存储null
//                    while(rowNum <=lastRowIndex && !isEmptyCell(this.abstractSheet.getRow(rowNum).getCell(colNum))){
//
//                        if(!isEmptyCell(this.abstractSheet.getRow(rowNum).getCell(colNum+1))){
//                            map.put(getCellString(this.abstractSheet.getRow(rowNum).getCell(colNum)),getCellString(this.abstractSheet.getRow(rowNum).getCell(colNum+1)));
//                            rowNum = rowNum +1;
//                        }
//                        else{
//                            LogUtil.e(TAG,"键所对应的值为空");
//                            map.put(getCellString(this.abstractSheet.getRow(rowNum).getCell(colNum)),null);
//                            rowNum = rowNum +1;
//                        }
//                        LogUtil.e(TAG,String.valueOf(rowNum));
//                    }
//                    LogUtil.e(TAG,map.toString());
//                }
//
//                //确定下一字段搜索起点
//                cell = this.abstractSheet.getRow(nextRowNum).getCell(firstColNum);
//
//            }

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
            }else if(AREA_CODE_IDENTIFICATION.equals(name)){
                return areaCodeSheet;
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

    public class DataSheet {
        private static final String TAG = "DataSheet";

        private String name;

        /**
         * 主键所在字段
         */
        private String primaryField;

        private Map<String, Map<String, String>> data = new HashMap<>();

        public DataSheet(String name) {
            this.name = name;
        }

        /**
         *
         * @param primaryKey 主键
         * @param map 一条数据记录
         * @return
         */
        public DataSheet addMap(String primaryKey, Map<String, String> map){
            this.data.put(primaryKey, map);
            return this;
        }

        public String getName() {
            return name;
        }

        public String getPrimaryField() {
            return primaryField;
        }

        public void setPrimaryField(String primaryField) {
            this.primaryField = primaryField;
        }

        /**
         * 通过主键获取目标数据记录
         * @param primaryKey 主键
         * @return
         */
        public Map<String, String> getMap(String primaryKey){
            if(this.data.containsKey(primaryKey)){
                return this.data.get(primaryKey);
            }
            LogUtil.e(TAG, "getMap(): 未找到目标主键！");
            return null;
        }

        public Map<String, Map<String, String>> getData() {
            return data;
        }

        @Override
        public String toString() {
            return "DataSheet{" +
                    "name='" + name + '\'' +
                    ", data=" + data +
                    '}';
        }
    }


}

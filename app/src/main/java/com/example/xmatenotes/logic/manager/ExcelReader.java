package com.example.xmatenotes.logic.manager;

import android.graphics.Rect;
import android.util.Log;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.xmatenotes.app.ax.A3;
import com.example.xmatenotes.app.XmateNotesApplication;
import com.example.xmatenotes.app.ax.Constants;
import com.example.xmatenotes.util.ExcelUtil;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

/**
 * Excel读取类
 * @see XSSFWorkbook
 * @see XSSFSheet
 * @see XSSFCell
 */
public class ExcelReader implements ReadPage{

    private static final String TAG = "ExcelReader";

    //单例
    private volatile static ExcelReader excelReader;

    //横坐标范围中值，用来区分左半页和右半页
    private final static int MIDDLEWIDTH = 136;
    //左半页搜索起始列名
    private final static String LEFTFIRSTCOL = "B";
    //右半页搜索起始列名
    private final static String RIGHTFIRSTCOL = "J";
    //在二级局域编码表中的搜索列数
    private final static int SEARCHCOLNUMBER = 8;

    //存储excel表格的路径
    private String excelPath;

    private InputStream excelStream = null;
    private XSSFWorkbook workbook = null;//当前打开的excel
    private XSSFSheet indexSheet = null;//索引表
    private XSSFSheet sheet = null;//当前打开的工作表
    private XSSFCell cell = null;//当前获取的单元格

    private Map<Integer,CellRangeAddress> sectionMap = new HashMap<>();

    private ExcelReader(){

    }

    public static ExcelReader getInstance() {
        if(excelReader == null){
            synchronized (ExcelReader.class){
                if(excelReader == null){
                    excelReader = new ExcelReader();
                }
            }
        }
        return excelReader;
    }

    /*************************基础方法**************************/

    //根据excel路径打开相应的excel
    public boolean openExcel(String excelPath){
        try {
            createWorkbook(excelPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //根据excel路径打开相应的excel，并根据索引表名打开索引表
    public boolean openExcel(String excelPath,String indexSheetName){
        try {
            createWorkbook(excelPath);
            openIndexSheet(indexSheetName);//打开索引表
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //切换当前工作表
    public boolean switchSheet(String sheetName){
        XSSFSheet bufferSheet = openSheet(sheetName);
        if(bufferSheet != null){
            sheet = bufferSheet;
            return true;
        }
        return false;
    }



    private void createWorkbook(String path) throws IOException {
        if(excelReader != null){
            excelPath = path;
            createWorkbook(XmateNotesApplication.context.getApplicationContext().getAssets().open(path));
        }
    }

    private void createWorkbook(InputStream inputStream) throws IOException {
        excelStream = inputStream;
        workbook = new XSSFWorkbook(excelStream);
    }



    /**
     * 打开工作表
     * @param sheetName 工作表名
     * @return 打开的工作表
     * 打开工作表前需要先打开excel
     */
    private XSSFSheet openSheet(String sheetName){
        if(workbook != null){
            return workbook.getSheet(sheetName);
        }
        return null;
    }

    /**
     * 打开工作表
     * @param sheetNumber 工作表号
     * @return 打开的工作表
     * 打开工作表前需要先打开excel
     */
    private XSSFSheet openSheet(int sheetNumber){
        if(workbook != null){
            return workbook.getSheetAt(sheetNumber);
        }
        return null;
    }

    //打开索引表
    private boolean openIndexSheet(String sheetName){
        if(workbook != null){
            indexSheet = workbook.getSheet(sheetName);
            return true;
        }
        return false;
    }

    /**
     * 获取目标sheet中的字符串类型单元格的内容
     * @param sheetName 目标sheet名
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return 返回为""表示目标单元格内容为空
     */
    public String getCellString(String sheetName, int row, String column){
        XSSFCell cell = getCell(sheetName, row, column);
        return getCellString(cell);

    }

    /**
     * 获取当前sheet中的字符串类型单元格的内容
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return 返回为""表示目标单元格内容为空
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    public String getCellString( int row, String column){
        return getCellString(row, colNameToNumber(column));
    }

    /**
     * 获取当前sheet中的字符串类型单元格的内容
     * @param row 行号 (1,2,3,...)
     * @param column 列号 (1,2,3,...)
     * @return 返回为""表示目标单元格内容为空
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    public String getCellString( int row, int column){
        XSSFCell cell = getCell(row, column);
        return getCellString(cell);
    }

    /**
     * 获取当前sheet中的字符串类型单元格的内容
     * @param cell 目标cell
     * @return 返回为""表示目标单元格内容为空
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    public String getCellString(XSSFCell cell){
        if(cell == null){
            return null;
        }
        if(cell.getCellType() != Cell.CELL_TYPE_STRING){
            Log.e(TAG,"目标单元格内容不是字符串");
            return null;
        }
        return ExcelUtil.getCellString(cell);
    }

    /**
     * 获取目标sheet中的数字类型单元格的内容
     * @param sheetName 目标sheet名
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return 返回为null表示目标单元格内容为空
     */
    public Integer getCellInt(String sheetName, int row, String column){
        XSSFCell cell = getCell(sheetName, row, column);
        return getCellInt(cell);
    }

    /**
     * 获取目标sheet中的数字类型单元格的内容
     * @param cell 目标cell
     * @return 返回为null表示目标单元格内容为空或不存在
     */
    public Integer getCellInt(XSSFCell cell){
        if(cell == null){
            return null;
        }
        if(cell.getCellType() == Cell.CELL_TYPE_BLANK){
            Log.e(TAG,"目标单元格内容为空");
            return null;
        }
        if(cell.getCellType() != Cell.CELL_TYPE_NUMERIC){
            Log.e(TAG,"目标单元格内容不是数字类型");
        }
        return ExcelUtil.getCellInt(cell);
    }

    /**
     * 获取当前sheet中的数字类型单元格的内容
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return 返回为null表示目标单元格内容为空或不存在
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    public Integer getCellInt(int row, String column){
        return getCellInt(row, colNameToNumber(column));
    }

    /**
     * 获取当前sheet中的数字类型单元格的内容
     * @param row 行号 (1,2,3,...)
     * @param column 列号 (1,2,3,...)
     * @return 返回为null表示目标单元格内容为空或不存在
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    public Integer getCellInt(int row, int column){
        XSSFCell cell = getCell(row, column);
        return getCellInt(cell);
    }

    /**
     * 获取目标sheet中的cell类型的单元格
     * @param sheetName 目标sheet名
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return
     */
    private XSSFCell getCell(String sheetName, int row, String column){
        XSSFSheet bufferSheet = openSheet(sheetName);
        return getCell(bufferSheet, row, column);
    }

    /**
     * 获取目标sheet中的cell类型的单元格
     * @param desSheet 目标sheet
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return
     */
    private XSSFCell getCell(XSSFSheet desSheet, int row, String column){
        return getCell(desSheet, row, colNameToNumber(column));
    }
    /**
     * 获取目标sheet中的cell类型的单元格
     * @param desSheet 目标sheet
     * @param row 行号 (1,2,3,...)
     * @param column 列号 (1,2,3,...)
     * @return
     */
    private XSSFCell getCell(XSSFSheet desSheet, int row, int column){
        if(column < 1){
            Log.e(TAG,"非法列名");
            return null;
        }if(row < 1){
            Log.e(TAG,"非法行名");
            return null;
        }
        int colInt = column -1;
        int rowInt = row -1;
        if(desSheet != null){
            if (desSheet.getRow(rowInt) == null){
                Log.e(TAG,"不存在目标行");
                return null;
            }
            if (desSheet.getRow(rowInt).getCell(colInt) == null){
                Log.e(TAG,"不存在目标列");
                return null;
            }
            XSSFCell cell = desSheet.getRow(rowInt).getCell(colInt);
            return getMergedCell(desSheet, cell);
        }else {
            Log.e(TAG,"目标工作表为空");
            return null;
        }
    }

    /**
     * 获取当前sheet中的cell类型的单元格
     * @param row 行号 (1,2,3,...)
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    private XSSFCell getCell(int row, String column){
        return getCell(row, colNameToNumber(column));
    }

    /**
     * 获取当前sheet中的cell类型的单元格
     * @param row 行号 (1,2,...)
     * @param column 列号 (1,2,...)
     * @return
     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
     */
    private XSSFCell getCell(int row, int column){
        return getCell(sheet, row, column);
    }

    /**
     * 将字母组合形式的列名转换为列号
     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
     * @return 列号 (1,2,3,...)
     * 若列名不合法，获取失败，返回-1
     */
    private int colNameToNumber(String column){
        int num = 0;
        if(column.length() >0){
            StringBuilder col = new StringBuilder(column.toUpperCase());
            for(int i=0;i<col.length();i++){
                char c = col.charAt(i);
                if(c >='A' && c<='Z'){
                    num *= 26;
                    num += (c - 'A'+1);
                }else{
                    Log.e(TAG,"非法列名");
                    return -1;
                }
            }
        }else{
            Log.e(TAG,"非法列名");
            return -1;
        }
        return num;
    }

    //如果目标单元格属于合并单元格，则返回合并区域左上角的单元格
    private XSSFCell getMergedCell(XSSFSheet desSheet, XSSFCell cell){
        if(ExcelUtil.inMerger(desSheet, cell)){
            CellRangeAddress cellRangeAddress = ExcelUtil.getMergedCellAddress(desSheet,cell);
            cell = desSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
        }
        return cell;
    }

    /**
     * 获取当前打开的工作表名
     * @return 若当前未打开工作表，返回空字符串
     */
    public String getCurrentSheetName(){
        if(sheet == null){
            Log.e(TAG,"getCurrentSheetName(): 未打开工作表");
            return "";
        }
        return sheet.getSheetName();
    }

    /**
     * 关闭当前打开的工作表
     * @throws IOException
     */
    public void close() throws IOException {
        sheet = null;
        indexSheet = null;
        if(workbook != null){
            workbook.close();
        }
        if(excelStream != null){
            excelStream.close();
        }
    }

    /*********************************功能方法*********************************/
    //根据具体需求设计的方法

    //在一级局域索引表中的搜索起始偏移量
    private final static int OFFSET_ROW = 2;
    private final static int OFFSET_COL = 2;
    //获取目标坐标所在局部区域
    public LocalRect getLocalRectByXY(int pageNumber, int x, int y){
        if(pageNumber<1 || pageNumber>13){
            Log.e(TAG,"不存在该页");
            return null;
        }
        if(x<0 || x> A3.ABSCISSA_RANGE){
            Log.e(TAG,"横坐标超出有效范围");
            return null;
        }
        if(y<0 || y> A3.ORDINATE_RANGE){
            Log.e(TAG,"纵坐标超出有效范围");
            return null;
        }
        int indexPageRow = pageNumber + OFFSET_ROW-1;
        int indexPageCol = OFFSET_COL-1;
        XSSFRow row = indexSheet.getRow(indexPageRow);
        while (true){
            Integer i = getCellInt(row.getCell(indexPageCol++));
            if(i == null){
                Log.e(TAG,"在一级局域索引表中搜索超出坐标范围");
                break;
            }
            int minY = i;
            int maxY = getCellInt(row.getCell(indexPageCol++));
            if(y>=minY && y<=maxY){
                int rowFirst = getCellInt(row.getCell(indexPageCol++));
                int rowLast = getCellInt(row.getCell(indexPageCol++));
                return searchLocalRect(x, y, rowFirst, rowLast, x<=MIDDLEWIDTH);
            }else {
                indexPageCol += 2;
            }
        }
        return null;
    }

    /**
     * 在二级局域编码表的指定范围中搜索目标局域
     * @param x
     * @param y
     * @param rowFirst 首行号 (1,2,...)
     * @param rowLast 尾行号 (1,2,...)
     * @param leftOrRight true表示左半页，false表示右半页
     * @return
     */
    private LocalRect searchLocalRect(int x, int y, int rowFirst, int rowLast, boolean leftOrRight){
        int firstCol;//起始列号
        if(leftOrRight == true){
            firstCol = colNameToNumber(LEFTFIRSTCOL);
        }else {
            firstCol = colNameToNumber(RIGHTFIRSTCOL);
        }
        firstCol--;
        for(int i = rowFirst;i<=rowLast;i++){
            XSSFRow row = sheet.getRow(i-1);
            LocalRect localRect = new LocalRect(leftOrRight);

            XSSFCell cell = row.getCell(firstCol);
            CellRangeAddress cellRangeAddress = null;
            if(ExcelUtil.inMerger(sheet, cell)){
                cellRangeAddress = ExcelUtil.getMergedCellAddress(sheet,cell);
                cell = sheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
            }
//            int firstCode = 0;
//            if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
//                firstCode = getCellInt(cell);
//            }
            //如果数字以文本形式存储，就无法读出该数字
            if(cell.getCellType() == Cell.CELL_TYPE_BLANK){
                Log.e(TAG,"遇到空白单元格，卡片搜索未找到目标卡片");
                return null;
            }
            int firstCode = getCellInt(cell);
            if(firstCode > 1){//是正文
                Rect r = new Rect();
                if(cellRangeAddress != null){
                    i++;
                    XSSFRow row2 = sheet.getRow(i-1);
                    r.left = getCellInt(row.getCell(firstCol+3));
                    r.top = getCellInt(row.getCell(firstCol+4));
                    r.right = getCellInt(row2.getCell(firstCol+5));
                    r.bottom = getCellInt(row2.getCell(firstCol+6));
                    if(!r.contains(x, y)){
                        i = cellRangeAddress.getLastRow()+1;
                        continue;
                    }
                }
            }

            localRect.firstLocalCode = firstCode;

            //从二级局域编码所在列开始往后搜索
            return deeperSearch(x, y, cellRangeAddress.getFirstRow(), cellRangeAddress.getLastRow(), firstCol+1, localRect);

        }
        Log.e(TAG,"搜索超出卡片搜索范围，未找到目标卡片");
        return null;
    }

    /**
     * 在二级局域编码表的进一步缩小后的范围中搜索目标局域
     * @param x
     * @param y
     * @param rowFirst 首行号(0,1,2,...)
     * @param rowLast 尾行号 (0,1,2,...)
     * @param firstCol 搜索起点列号(0,1,2,...)
     * @param lR 待完善的目标局域
     * @return
     */
    private LocalRect deeperSearch(int x, int y, int rowFirst, int rowLast, int firstCol, LocalRect lR){
        Rect r = null;
        for(int i = rowFirst;i<=rowLast;i++){
            XSSFRow row = sheet.getRow(i);

            Log.e(TAG, "lR.firstLocalCode >1: "+(lR.firstLocalCode >1)+"i == rowFirst+2: "+ (i == rowFirst+2));
            if(lR.firstLocalCode >1 && i == rowFirst+2){
                r = new Rect();
                XSSFRow row2 = sheet.getRow(rowLast);
                r.left = getCellInt(row.getCell(firstCol+2));
                r.top = getCellInt(row.getCell(firstCol+3));
                r.right = getCellInt(row2.getCell(firstCol+4));
                r.bottom = getCellInt(row2.getCell(firstCol+5));
                Log.e(TAG,"r.rect: "+r.toString());
            }
            for(int j=0;j<SEARCHCOLNUMBER-1;j++){
                XSSFCell cell = getMergedCell(sheet, row.getCell(firstCol+j));
                int cellInt = 0;
                String cellStr = "";
//                if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
//                    cellInt = getCellInt(cell);
//                }else if(cell.getCellType() == Cell.CELL_TYPE_STRING){
//                    cellStr = getCellString(cell);
//                }
                if(j == 1){
                    cellStr = getCellString(cell);
                }else if(j == 6){
                    //提示区可能为空
                    if(cell.getCellType() != Cell.CELL_TYPE_BLANK){
                        cellStr = getCellString(cell);
                    }
                }else {
                    cellInt = getCellInt(cell);
                }
                if(j == 0){
                    if(lR.firstLocalCode >1 && cellInt >2){
                        cellInt = 3;
                    }
                    lR.secondLocalCode = cellInt;
                }else if(j == 1){
                    lR.localName = cellStr;
                }else if(j == 2){
                    lR.rect.left = cellInt;
                }else if(j == 3){
                    lR.rect.top = cellInt;
                }else if(j == 4){
                    lR.rect.right = cellInt;
                }else if(j == 5){
                    lR.rect.bottom = cellInt;
                }else if(j == 6){
                    if("资源卡".equals(lR.localName)){
                        lR.addInf = cellStr;
                    }
                }
            }
            if(lR.rect.contains(x, y)){
                Log.e(TAG,"rowFirst: "+rowFirst+" i: "+i+" r != null: "+(r != null));
                if(i >= rowFirst+2 && r != null){
                    lR.rect = r;
                    Log.e(TAG,"r.rect: "+r.toString());
                }
                return lR;
            }
        }
        Log.e(TAG,"在目标卡片中未搜索到目标局域");
        return null;
    }

    //返回参数页码在表格中对应的单元格矩形范围
    private CellRangeAddress getPageSectionRange(int pageNumber){
        if(!sectionMap.containsKey(pageNumber)){
            int firstRow = 4;
            String firstCol = "R";
            XSSFCell firstCell = getCell(firstRow,firstCol);
            int cellInt = getCellInt(firstRow,firstCol);
            CellRangeAddress cellRangeAddress = ExcelUtil.getMergedCellAddress(sheet,firstCell);
            while(cellInt != pageNumber){
                if(!sectionMap.containsKey(cellInt)){
                    sectionMap.put(cellInt,cellRangeAddress);
                }
                firstRow = cellRangeAddress.getLastRow();
                firstRow++;
                cellInt = getCellInt(firstRow,firstCol);
                firstCell = getCell(firstRow,firstCol);
                cellRangeAddress = ExcelUtil.getMergedCellAddress(sheet,firstCell);
            }
            sectionMap.put(cellInt,cellRangeAddress);
        }
        return sectionMap.get(pageNumber);
    }

    @Override
    public boolean isShuXieQu(int pageNumber, int x, int y) {
        CellRangeAddress cellRange = getPageSectionRange(pageNumber);
        int firstRow = cellRange.getFirstRow()+1;
        int lastRow = cellRange.getLastRow()+1;
        int firstCol = colNameToNumber("G");
        int[] xy = new int[4];
        for(int i=firstRow;i<=lastRow;i++){
            if("书写区".equals(getCellString(i,firstCol))){
                for(int j=0;j<4;j++){
                    xy[j] = getCellInt(i,j+firstCol+1);
                }
                if((x>=xy[0] && x<=xy[2]) && (y>=xy[1] && y<=xy[3])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isWenZiQu(int pageNumber, int x, int y) {
        CellRangeAddress cellRange = getPageSectionRange(pageNumber);
        int firstRow = cellRange.getFirstRow()+1;
        int lastRow = cellRange.getLastRow()+1;
        int firstCol = colNameToNumber("G");
        int[] xy = new int[4];
        for(int i=firstRow;i<=lastRow;i++){
            if("文字区".equals(getCellString(i,firstCol))){
                for(int j=0;j<4;j++){
                    xy[j] = getCellInt(i,j+firstCol+1);
                }
                if((x>=xy[0] && x<=xy[2]) && (y>=xy[1] && y<=xy[3])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isZiYuanKa(int pageNumber, int x, int y) {
        CellRangeAddress cellRange = getPageSectionRange(pageNumber);
        int firstRow = cellRange.getFirstRow()+1;
        int lastRow = cellRange.getLastRow()+1;
        int firstCol = colNameToNumber("G");
        int[] xy = new int[4];
        for(int i=firstRow;i<=lastRow;i++){
            if("资源卡".equals(getCellString(i,firstCol))){
                for(int j=0;j<4;j++){
                    xy[j] = getCellInt(i,j+firstCol+1);
                }
                if((x>=xy[0] && x<=xy[2]) && (y>=xy[1] && y<=xy[3])) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isRenZhiFaWen(int pageNumber, int x, int y) {
        CellRangeAddress cellRange = getPageSectionRange(pageNumber);
        int firstRow = cellRange.getFirstRow()+1;
        int lastRow = cellRange.getLastRow()+1;
        int firstCol = colNameToNumber("B");
        int[] xy = new int[4];
        for(int i=firstRow;i<=lastRow;i++){

            for(int j=0;j<4;j++){
                xy[j] = getCellInt(i,j+firstCol);
            }
            if((x>=xy[0] && x<=xy[2]) && (y>=xy[1] && y<=xy[3])) {
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean isJiaoChaChuangZao(int pageNumber, int x, int y) {
        CellRangeAddress cellRange = getPageSectionRange(pageNumber);
        int firstRow = cellRange.getFirstRow()+1;
        int lastRow = cellRange.getLastRow()+1;
        int firstCol = colNameToNumber("M");
        int[] xy = new int[4];
        for(int i=firstRow;i<=lastRow;i++){

            for(int j=0;j<4;j++){
                xy[j] = getCellInt(i,j+firstCol);
            }
            if((x>=xy[0] && x<=xy[2]) && (y>=xy[1] && y<=xy[3])) {
                return true;
            }

        }
        return false;
    }
}

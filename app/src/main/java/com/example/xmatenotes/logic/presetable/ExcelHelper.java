package com.example.xmatenotes.logic.presetable;

import android.content.Context;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

public class ExcelHelper {

    private static final String TAG = "ExcelHelper";

    private static final ExcelHelper excelHelper = new ExcelHelper();

    protected Context context;

    protected XSSFSheet curSheet = null;//当前打开的工作表

    /**
     * 摘要信息表
     */
    private XSSFSheet abstractSheet = null;

    /**
     * 一级搜索索引表
     */
    private XSSFSheet indexSheet = null;//索引表

    protected XSSFWorkbook curWorkbook = null;//当前打开的excel
    //存储excel表格的路径
    private String excelPath;

    private InputStream excelStream = null;


    protected ExcelHelper(){

    }

    public static ExcelHelper getInstance(){
        return excelHelper;
    }

    public ExcelHelper init(Context context){
        this.context = context;
        return this;
    }

    /*************************基础方法**************************/

    /**
     * 根据excel路径打开相应的excel
     * 如: excelReader.openExcel("excel/A3学程样例·纸分区坐标.xlsx")
     * @param excelPath
     * @return
     */
    public boolean openExcel(String excelPath){
        try {
            createWorkbook(excelPath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 切换当前工作表
     * @param sheetName
     * @return
     */
    public boolean switchSheet(String sheetName){
        curSheet = openSheet(sheetName);
        if(curSheet != null){
            LogUtil.e(TAG, "切换sheet: "+sheetName);
            return true;
        }
        return false;
    }

    private void createWorkbook(String path) throws IOException {
        if(excelHelper != null){
            excelPath = path;
            createWorkbook(context.getAssets().open(path));
        }
    }

    private void createWorkbook(InputStream inputStream) throws IOException {
        excelStream = inputStream;
        curWorkbook = new XSSFWorkbook(excelStream);
    }

    /**
     * 打开工作表
     * @param sheetName 工作表名
     * @return 打开的工作表
     * 打开工作表前需要先打开excel
     */
    protected XSSFSheet openSheet(String sheetName){
        if(curWorkbook != null){
            return curWorkbook.getSheet(sheetName);
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
        if(curWorkbook != null){
            return curWorkbook.getSheetAt(sheetNumber);
        }
        return null;
    }

    /**
     *
     * @param cell
     * @return 若单元格不存在或内容为空，返回true
     */
    public boolean isEmptyCell(XSSFCell cell){
        if(cell == null){
            return true;
        }
        return ExcelUtil.isEmptyCell(cell);
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
        }
        if(row < 1){
            Log.e(TAG,"非法行名");
            return null;
        }
        int colInt = numToIndex(column);
        int rowInt = numToIndex(row);
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
        return getCell(curSheet, row, column);
    }

    /**
     * 将字母组合形式的列名转换为代码中使用的index
     * @param colName 列名 (A,B,C,...,AA,AB,AC,...)
     * @return index (0,1,2,...)
     */
    protected int colNameToIndex(String colName){
        return numToIndex(colNameToNumber(colName));
    }

    /**
     * 将行名转换为代码中使用的index
     * @param rowName 行名 (1,2,3,...)
     * @return index (0,1,2,...)
     */
    protected int rowNameToIndex(String rowName){
        return numToIndex(Integer.parseInt(rowName));
    }

    /**
     * 将字母组合形式的列名转换为列号
     * @param colName 列名 (A,B,C,...,AA,AB,AC,...)
     * @return 列号 (1,2,3,...)
     * 若列名不合法，获取失败，返回-1
     */
    private int colNameToNumber(String colName){
        int num = 0;
        if(colName.length() >0){
            StringBuilder col = new StringBuilder(colName.toUpperCase());
            for(int i=0;i<col.length();i++){
                char c = col.charAt(i);
                if(c >='A' && c<='Z'){
                    num *= 26;
                    num += (c - 'A'+1);
                }else{
                    Log.e(TAG,"colNameToNumber(): 非法列名");
                    return -1;
                }
            }
        }else{
            Log.e(TAG,"colNameToNumber(): 非法列名");
            return -1;
        }
        return num;
    }

    /**
     * 将行号或列号转换为代码中使用的index
     * @param num 行号或列号 (1,2,3,...)
     * @return index (0,1,2,...)
     */
    private int numToIndex(int num){
        return num-1;
    }

    /**
     * 如果目标单元格属于合并单元格，则返回合并区域左上角的单元格
     * @param desSheet
     * @param cell
     * @return
     */
    protected XSSFCell getMergedCell(XSSFSheet desSheet, XSSFCell cell){
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
        if(curSheet == null){
            Log.e(TAG,"getCurrentSheetName(): 未打开工作表");
            return null;
        }
        return curSheet.getSheetName();
    }

    /**
     * 关闭当前打开的工作表
     * @throws IOException
     */
    public void close() throws IOException {
        curSheet = null;
        indexSheet = null;
        abstractSheet = null;
        if(curWorkbook != null){
            curWorkbook.close();
        }
        if(excelStream != null){
            excelStream.close();
        }
    }

    /*************************基础方法**************************/

    /*******************************功能方法*****************************/


}

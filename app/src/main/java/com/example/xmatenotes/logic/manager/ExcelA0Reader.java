//package com.example.xmatenotes.logic.manager;
//
//import android.util.Log;
//
//import com.example.xmatenotes.app.XApp;
//import com.example.xmatenotes.Constants;
//import com.example.xmatenotes.logic.presetable.ExcelUtil;
//
//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.HashMap;
//import java.util.Map;
//
//public class ExcelA0Reader {
//    public static final String TAG = "ExcelA0Reader";
//
//    //创建一个该函数的单例
//    private volatile static ExcelA0Reader excelA0Reader;
//
//    public String excelPath;
//
//    public InputStream excelStream = null;
//
//    public XSSFWorkbook workbook = null ;//当前打开的excel
//
//    public XSSFSheet indexSheet = openSheet("0级索引表");//0级索引表
//
//    public XSSFSheet indexSheet1 = openSheet("一级索引表");
//
//    public XSSFSheet indexSheet2 = openSheet("二级索引表");
//
//    public XSSFSheet sheet1 = openSheet("G表");
//
//    public XSSFSheet sheet2 = openSheet("TA表");
//
//    public XSSFSheet sheet3 = openSheet("N表");
//
//    public XSSFSheet pageSheet = openSheet("page");
//
//    public XSSFSheet sheet = null;//当前打开的工作表
//
//    public XSSFCell cell = null;//当前获取的单元格
//
//    public int pageUL = 0; //左上区域的pageid
//
//    public int pageUR = 1;
//
//    public int pageDL = 16;
//
//    public int pageDR = 17;
//
//    public int ulMaxX = 512;
//
//    public int ulMaxY = 512;
//
//    public int urMaxX = 42;
//
//    public int urMaxY = 512;
//
//    public int dlMaxX = 512;
//
//    public int dlMaxY = 270;
//
//    public int drMaxX = 42;
//
//    public int drMaxY = 270;
//
//    LocalA0Rect localA0Rect = new LocalA0Rect();
//
//
//    public Map<Integer, CellRangeAddress> sectionMap = new HashMap<>();
//
//    public ExcelA0Reader() {
//
//    }
//
//    public static ExcelA0Reader getInstance() {
//        if (excelA0Reader == null) {
//            synchronized (ExcelReader.class) {
//                if (excelA0Reader == null) {
//                    excelA0Reader = new ExcelA0Reader();
//                }
//            }
//        }
//        return excelA0Reader;
//    }
//
//    /*************************基础方法**************************/
//
//    /**
//     * 根据路径来打开excel
//     *
//     * @param excelPath 路径
//     * @return 给出是否打开成功
//     */
//    public boolean openExcel(String excelPath) {
//        try {
//            createWorkbook(excelPath);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//
//    /**
//     * 根据路径以及索引表名字来打开。
//     *
//     * @param excelPath      路径
//     * @param indexSheetName 索引表的名字
//     * @return 是否成功打开
//     */
//    public boolean openExcel(String excelPath, String indexSheetName) {
//        try {
//            createWorkbook(excelPath);
//            openIndexSheet(indexSheetName);//打开索引表
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * 根据工作表名字来切换工作表
//     *
//     * @param sheetName 工作表名字
//     * @return 是否打开
//     */
//    public boolean switchSheet(String sheetName) {
//        XSSFSheet bufferSheet = openSheet(sheetName);
//        if (bufferSheet != null) {
//            sheet = bufferSheet;
//            return true;
//        }
//        return false;
//    }
//
//    public void createWorkbook(String path) throws IOException {
//        if (excelA0Reader != null) {
//            excelPath = path;
//            createWorkbook(XApp.context.getApplicationContext().getAssets().open(path));
//        }
//    }
//
//    public void createWorkbook(InputStream inputStream) throws IOException {
//        excelStream = inputStream;
//        workbook = new XSSFWorkbook(excelStream);
//    }
//
//    /**
//     * @param sheetName 工作表的名字
//     * @return 打开的工作表
//     */
//    public XSSFSheet openSheet(String sheetName) {
//        if (workbook != null) {
//            return workbook.getSheet(sheetName);
//        }
//        return null;
//    }
//
//    /**
//     * 打开工作表
//     *
//     * @param sheetNumber 工作表号
//     * @return 打开的工作表
//     * 打开工作表前需要先打开excel
//     */
//    public XSSFSheet openSheet(int sheetNumber) {
//        if (workbook != null) {
//            return workbook.getSheetAt(sheetNumber);
//        }
//        return null;
//    }
//
//    /**
//     * @param sheetName 索引表的名字
//     * @return 打开索引表
//     */
//    public boolean openIndexSheet(String sheetName) {
//        if (workbook != null) {
//            indexSheet = workbook.getSheet(sheetName);
//            return true;
//        }
//        return false;
//    }
//    //下列是不同的形参的获得单元格内字符串的函数
//
//    /**
//     * 获取目标sheet中的字符串类型单元格的内容
//     *
//     * @param sheetName 目标sheet名
//     * @param row       行号 (1,2,3,...)
//     * @param column    列名 (A,B,C,...,AA,AB,AC,...)
//     * @return 返回为""表示目标单元格内容为空
//     */
//    public String getCellString(String sheetName, int row, String column) {
//        XSSFCell cell = getCell(sheetName, row, column);
//        return getCellString(cell);
//
//    }
//
//    /**
//     * 获取当前sheet中的字符串类型单元格的内容
//     *
//     * @param row    行号 (1,2,3,...)
//     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
//     * @return 返回为""表示目标单元格内容为空
//     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    public String getCellString(int row, String column) {
//        return getCellString(row, colNameToNumber(column));
//    }
//
//    /**
//     * 获取当前sheet中的字符串类型单元格的内容
//     *
//     * @param row    行号 (1,2,3,...)
//     * @param column 列号 (1,2,3,...)
//     * @return 返回为""表示目标单元格内容为空
//     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    public String getCellString(int row, int column) {
//        XSSFCell cell = getCell(row, column);
//        return getCellString(cell);
//    }
//
//    /**
//     * 获取当前sheet中的字符串类型单元格的内容
//     *
//     * @param cell 目标cell
//     * @return 返回为""表示目标单元格内容为空
//     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    public String getCellString(XSSFCell cell) {
//        if (cell == null) {
//            return null;
//        }
//        if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
//            Log.e(TAG, "目标单元格内容不是字符串");
//            return null;
//        }
//        return ExcelUtil.getCellString(cell);
//    }
//
//    //下面是不同形参的获得单元格内数字类型的内容的函数
//
//    /**
//     * 获取目标sheet中的数字类型单元格的内容
//     *
//     * @param sheetName 目标sheet名
//     * @param row       行号 (1,2,3,...)
//     * @param column    列名 (A,B,C,...,AA,AB,AC,...)
//     * @return 返回为null表示目标单元格内容为空
//     */
//    public Integer getCellInt(String sheetName, int row, String column) {
//        XSSFCell cell = getCell(sheetName, row, column);
//        return getCellInt(cell);
//    }
//
//    /**
//     * 获取目标sheet中的数字类型单元格的内容
//     *
//     * @param cell 目标cell
//     * @return 返回为null表示目标单元格内容为空或不存在
//     */
//    public Integer getCellInt(XSSFCell cell) {
//        if (cell == null) {
//            return null;
//        }
//        if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
//            Log.e(TAG, "目标单元格内容为空");
//            return null;
//        }
//        if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
//            Log.e(TAG, "目标单元格内容不是数字类型");
//        }
//        return ExcelUtil.getCellInt(cell);
//    }
//
//    /**
//     * 获取当前sheet中的数字类型单元格的内容
//     *
//     * @param row    行号 (1,2,3,...)
//     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
//     * @return 返回为null表示目标单元格内容为空或不存在
//     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    public Integer getCellInt(int row, String column) {
//        return getCellInt(row, colNameToNumber(column));
//    }
//
//    /**
//     * 获取当前sheet中的数字类型单元格的内容
//     *
//     * @param row    行号 (1,2,3,...)
//     * @param column 列号 (1,2,3,...)
//     * @return 返回为null表示目标单元格内容为空或不存在
//     * 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    public Integer getCellInt(int row, int column) {
//        XSSFCell cell = getCell(row, column);
//        return getCellInt(cell);
//    }
//
//    //下面是不同形参的获得目标sheet中的cell类型的单元格
//
//    /**
//     * 获取目标sheet中的cell类型的单元格
//     *
//     * @param sheetName 目标sheet名
//     * @param row       行号 (1,2,3,...)
//     * @param column    列名 (A,B,C,...,AA,AB,AC,...)
//     * @return
//     */
//    private XSSFCell getCell(String sheetName, int row, String column) {
//        XSSFSheet bufferSheet = openSheet(sheetName);
//        return getCell(bufferSheet, row, column);
//    }
//
//    /**
//     * 获取目标sheet中的cell类型的单元格
//     *
//     * @param desSheet 目标sheet
//     * @param row      行号 (1,2,3,...)
//     * @param column   列名 (A,B,C,...,AA,AB,AC,...)
//     * @return
//     */
//    private XSSFCell getCell(XSSFSheet desSheet, int row, String column) {
//        return getCell(desSheet, row, colNameToNumber(column));
//    }
//
//    /**
//     * 获取目标sheet中的cell类型的单元格
//     *
//     * @param desSheet 目标sheet
//     * @param row      行号 (1,2,3,...)
//     * @param column   列号 (1,2,3,...)
//     * @return
//     */
//    private XSSFCell getCell(XSSFSheet desSheet, int row, int column) {
//        if (column < 1) {
//            Log.e(TAG, "非法列名");
//            return null;
//        }
//        if (row < 1) {
//            Log.e(TAG, "非法行名");
//            return null;
//        }
//        int colInt = column - 1;
//        int rowInt = row - 1;
//        if (desSheet != null) {
//            if (desSheet.getRow(rowInt) == null) {
//                Log.e(TAG, "不存在目标行");
//                return null;
//            }
//            if (desSheet.getRow(rowInt).getCell(colInt) == null) {
//                Log.e(TAG, "不存在目标列");
//                return null;
//            }
//            XSSFCell cell = desSheet.getRow(rowInt).getCell(colInt);
//            return getMergedCell(desSheet, cell);
//        } else {
//            Log.e(TAG, "目标工作表为空");
//            return null;
//        }
//    }
//
//    /**
//     * 获取当前sheet中的cell类型的单元格
//     *
//     * @param row    行号 (1,2,3,...)
//     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
//     * @return 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    private XSSFCell getCell(int row, String column) {
//        return getCell(row, colNameToNumber(column));
//    }
//
//    /**
//     * 获取当前sheet中的cell类型的单元格
//     *
//     * @param row    行号 (1,2,...)
//     * @param column 列号 (1,2,...)
//     * @return 调用此方法前请确保已经通过swithSheet()方法切换到了目标sheet！
//     */
//    private XSSFCell getCell(int row, int column) {
//        return getCell(sheet, row, column);
//    }
//
//    /**
//     * 将字母组合形式的列名转换为列号
//     *
//     * @param column 列名 (A,B,C,...,AA,AB,AC,...)
//     * @return 列号 (1,2,3,...)
//     * 若列名不合法，获取失败，返回-1
//     */
//    private int colNameToNumber(String column) {
//        int num = 0;
//        if (column.length() > 0) {
//            StringBuilder col = new StringBuilder(column.toUpperCase());
//            for (int i = 0; i < col.length(); i++) {
//                char c = col.charAt(i);
//                if (c >= 'A' && c <= 'Z') {
//                    num *= 26;
//                    num += (c - 'A' + 1);
//                } else {
//                    Log.e(TAG, "非法列名");
//                    return -1;
//                }
//            }
//        } else {
//            Log.e(TAG, "非法列名");
//            return -1;
//        }
//        return num;
//    }
//
//    //如果目标单元格属于合并单元格，则返回合并区域左上角的单元格
//    private XSSFCell getMergedCell(XSSFSheet desSheet, XSSFCell cell) {
//        if (ExcelUtil.inMerger(desSheet, cell)) {
//            CellRangeAddress cellRangeAddress = ExcelUtil.getMergedCellAddress(desSheet, cell);
//            cell = desSheet.getRow(cellRangeAddress.getFirstRow()).getCell(cellRangeAddress.getFirstColumn());
//        }
//        return cell;
//    }
//
//    /**
//     * 获取当前打开的工作表名
//     *
//     * @return 若当前未打开工作表，返回空字符串
//     */
//    public String getCurrentSheetName() {
//        if (sheet == null) {
//            Log.e(TAG, "getCurrentSheetName(): 未打开工作表");
//            return "";
//        }
//        return sheet.getSheetName();
//    }
//
//    /**
//     * 关闭当前打开的工作表
//     *
//     * @throws IOException
//     */
//    public void close() throws IOException {
//        sheet = null;
//        indexSheet = null;
//        if (workbook != null) {
//            workbook.close();
//        }
//        if (excelStream != null) {
//            excelStream.close();
//        }
//    }
//
//    /*********************************功能方法*********************************/
//
//    public void getPage() {
//        XSSFRow row1 = pageSheet.getRow(1);
//
//        XSSFRow row2 = pageSheet.getRow(2);
//
//        XSSFRow row3 = pageSheet.getRow(3);
//
//        XSSFRow row4 = pageSheet.getRow(4);
//
//        XSSFRow row0 = pageSheet.getRow(0);
//
//
//        pageUL = getCellInt(row1.getCell(0));
//
//        String YY = String.valueOf(pageUL);//测试
//        Log.e(TAG,YY);//
//
//        pageUR = getCellInt(row2.getCell(0));
//
//        pageDL = getCellInt(row3.getCell(0));
//
//        pageDR = getCellInt(row4.getCell(0));
//
//        String XX = String.valueOf(pageDR);//测试
//        Log.e(TAG,XX);//
//
//        ulMaxX = getCellInt(row1.getCell(1));
//
//        ulMaxY = getCellInt(row1.getCell(2));
//
//        urMaxX = getCellInt(row2.getCell(1));
//
//        urMaxY = getCellInt(row2.getCell(2));
//
//        dlMaxX = getCellInt(row3.getCell(1));
//
//        dlMaxY = getCellInt(row3.getCell(2));
//
//        drMaxX = getCellInt(row4.getCell(1));
//
//        drMaxY = getCellInt(row4.getCell(2));
//
//    }
//
//    /**
//     * 根据0级索引表来区分小组来进入一级索引表第几行
//     * 同时也是完成的分组
//     *
//     * @param pageId 坐标的pageid
//     * @param x
//     * @param y
//     * @return
//     */
//    public LocalA0Rect getLocalRectByXY(int pageId, int x, int y) {
//        if (pageId != pageUL && pageId != pageUR && pageId != pageDL && pageId != pageDR) { //拼接完成只有这三个
//            Log.e(TAG, "不存在该页");
//            return null;
//        }
//        if (pageId == pageUL) {
//            if (x < 0 || x > ulMaxX) {
//                Log.e(TAG, "横坐标超出有效范围");
//                return null;
//            }
//            if (y < 0 || y > ulMaxY) {
//                Log.e(TAG, "纵坐标超出有效范围");
//                return null;
//            }
//        }
//
//        if (pageId == pageUR) {
//            if (x < 0 || x > urMaxX) {
//                Log.e(TAG, "横坐标超出有效范围");
//                return null;
//            }
//            if (y < 0 || y > urMaxY) {
//                Log.e(TAG, "纵坐标超出有效范围");
//                return null;
//            }
//        }
//        if (pageId == pageDL) {
//            if (x < 0 || x > dlMaxX) {
//                Log.e(TAG, "横坐标超出有效范围");
//                return null;
//            }
//            if (y < 0 || y > dlMaxY) {
//                Log.e(TAG, "纵坐标超出有效范围");
//                return null;
//            }
//        }
//        if (pageId == pageDR) {
//            if (x < 0 || x > drMaxX) {
//                Log.e(TAG, "横坐标超出有效范围");
//                return null;
//            }
//            if (y < 0 || y > drMaxY) {
//                Log.e(TAG, "纵坐标超出有效范围");
//                return null;
//            }
//        }
//
//        int indexPageRow = 1;
//
//        int indexPageCol = 0;
//
//        while (indexPageRow < 20) {
//            XSSFRow row = indexSheet.getRow(indexPageRow++); //从第二行开始寻找，excel表格是从0开始
//            indexPageCol = 0;  //第一组没找到，去另外一组
//            while (indexPageCol < 8) {
//
//                int pageId1 = getCellInt(row.getCell(indexPageCol++));
//
//                Log.e(TAG,"1"+String.valueOf(pageId1));//
//
//                int pageId2 = getCellInt(row.getCell(indexPageCol++));
//
//                Log.e(TAG,"2"+String.valueOf(pageId2));//
//
//                int x1 = getCellInt(row.getCell(indexPageCol++));
//
//                Log.e(TAG,"x1"+String.valueOf(x1));//
//
//                int y1 = getCellInt(row.getCell(indexPageCol++));
//
//                Log.e(TAG,"y1"+String.valueOf(y1));//
//
//                int x2 = getCellInt(row.getCell(indexPageCol++));
//
//                Log.e(TAG,"x2"+String.valueOf(x2));//
//
//                int y2 = getCellInt(row.getCell(indexPageCol++));//在到索引表那一列
//
//                Log.e(TAG,"y2"+String.valueOf(y2));
//
//                int localgroup = getCellInt(row.getCell(indexPageCol++));//得到组别
//
//                localA0Rect.Localgroup =String.valueOf(localgroup);//得到自己的组别写入本地
//
//                int plocalgroup = getCellInt(row.getCell(indexPageCol));  //得到结伴组
//
//                localA0Rect.Plocalgroup = String.valueOf(plocalgroup);//得到结伴组的组别写入本地
//
//                if (pageId1 != pageId2) {
//                    if (x2 < x1 && y2 > y1) {
//                        if (pageId == pageId1) {
//                            int x3 = Constants.A0_MAXX;
//                            int y3 = y2;
//                            if (x > x1 && x < x3 && y > y1 && y < y3) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));//现在在组别那一列
//                                String H = String.valueOf(rowFirst);
//                                Log.e(TAG,"进入下一级索引的行数"+H);
//                                Log.e(TAG,"找到点的位置了。");//测试
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//
//                        }
//                        if (pageId == pageId2) {
//                            int x4 = Constants.A0_MINX;
//                            int y4 = y1;
//                            if (x > x4 && x < x2 && y > y4 && y < y2) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//
//                        } else {
//                            indexPageCol++;//列为空了
//                        }
//                    }
//                    if (y2 < y1 && x2 > x1) {
//                        if (pageId == pageId1) {
//                            int x3 = x2;
//                            int y3 = Constants.A0_MAXY;
//                            if (x > x1 && x < x3 && y > y1 && y < y3) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//
//                        }
//
//                        if (pageId == pageId2) {
//                            int x4 = x1;
//                            int y4 = Constants.A0_MINY;
//                            if (x > x4 && x < x2 && y > y4 && y < y2) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//                        } else {
//                            indexPageCol++;
//                        }
//
//                    }
//                    if (x1 > x2 && y1 > y2) {
//                        if (pageId == pageId1) {
//                            int x3 = Constants.A0_MAXX;
//                            int y3 = Constants.A0_MAXY;
//                            if (x > x1 && x < x3 && y > y1 && y < y3) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//                        } else if (pageId == pageId2) {
//                            int x4 = Constants.A0_MINX;
//                            int y4 = Constants.A0_MINY;
//                            if (x > x4 && x < x2 && y > y4 && y < y2) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//                        } else {
//                            int x5 = Constants.A0_MINX;
//                            int y5 = y1;
//                            int x6 = x2;
//                            int y6 = Constants.A0_MAXY;
//                            int x7 = Constants.A0_MINX;
//                            int y7 = Constants.A0_MINY;
//                            int x8 = Constants.A0_MAXX;
//                            int y8 = y2;
//                            if (x > x5 && x < x6 && y > y5 && y < y6) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            } else if (x > x7 && x < x8 && y > y7 && y < y8) {
//                                int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                                return searchLocalRect1(x, y, rowFirst, pageId);
//                            }
//                            else{
//                                indexPageCol++;
//                            }
//
//                        }
//
//                    }
//                }
//
//                if (pageId1 == pageId2) {
//                    if (x < x2 && x > x1 && y < y2 && y > y1) {
//                        int rowFirst = getCellInt(row.getCell(indexPageCol+1));
//                        //测试用
//                        Log.e(TAG, "判断范围成功");
//                        return searchLocalRect1(x, y, rowFirst, pageId);
//                    } else {
//                        indexPageCol++;
//                    }
//                }
//            }
//
//        }
//
//        return null;
//    }
//
//    public LocalA0Rect searchLocalRect1(int x, int y, int rowFirst, int pageNumber) {
//        rowFirst = rowFirst -1;//excel从0开始
//        int indexPageCol = 1;
//        XSSFRow depart = indexSheet1.getRow(2);//判断调用什么方法
//        XSSFRow row = indexSheet1.getRow(rowFirst);  //根据0级别索引表得到的在一级索引表中要搜索的行数
//        while (getCellInt(row.getCell(indexPageCol)) != null) {
//
//            int pageId1 = getCellInt(row.getCell(indexPageCol++));
//
//            int pageId2 = getCellInt(row.getCell(indexPageCol++));
//
//            int x1 = getCellInt(row.getCell(indexPageCol++));
//
//            String hh = String.valueOf(x1);
//            Log.e(TAG ,"依次遍历"+hh);
//
//            int y1 = getCellInt(row.getCell(indexPageCol++));
//
//            int x2 = getCellInt(row.getCell(indexPageCol++));
//
//            int y2 = getCellInt(row.getCell(indexPageCol++));
//
//
//
//            if (pageId1 != pageId2) {
//                if (x2 < x1 && y2 > y1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = y2;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            if (rowFirst != 17){
//                                if ( "提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {  //大组信息
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol))); //对于A，B的提取
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ( "提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) { //小组信息
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if (  "N表".equals(getCellString(depart.getCell(indexPageCol)))) {  //姓名信息
//                                    Log.e(TAG,"我来到了N表");
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));  //写入信息
//                                    localA0Rect.wOrR = "xieru";  //写操作，直接执行
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));  //周信息
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = y1;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//                }
//                if (y2 < y1 && x2 > x1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = x2;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//                    }
//
//                    if (pageNumber == pageId2) {
//                        int x4 = x1;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//                if (x1 > x2 && y1 > y2) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//                    }
//                    if (pageNumber == pageUR) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = y1;
//                        int x6 = x2;
//                        int y6 = Constants.A0_MAXY;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//                    }
//                    if (pageNumber == pageDL) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = Constants.A0_MINY;
//                        int x6 = Constants.A0_MAXX;
//                        int y6 = y2;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            if (rowFirst != 17){
//                                if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "dazu";
//                                    return null;
//                                } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                                    localA0Rect.wOrR = "xiaozu";
//                                    return null;
//                                } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xingming";
//                                    return searchLocalRectN(x, y, rowNext,pageNumber);
//                                } else {
//                                    int rowNext = getCellInt(row.getCell(indexPageCol));
//                                    localA0Rect.wOrR = "xieru";
//                                    return searchLocalRect2(x, y, rowNext, pageNumber);
//                                }
//                            }
//                            else {  //对于G0要单独处理
//                                localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                                localA0Rect.wOrR = "zhou";
//                                return null;
//                            }
//                        }
//
//                    } else {
//                        indexPageCol++;
//
//                    }
//
//                }
//            }
//
//            if (pageId1 == pageId2) {
//                if (x < x2 && x > x1 && y < y2 && y > y1) {
//                    if (rowFirst != 17){
//                        if ("提取字符串信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                            localA0Rect.getGroupMessage(getCellString(row.getCell(indexPageCol)));
//                            localA0Rect.wOrR = "dazu";
//                            Log.e(TAG,"提取出的组别信息"+getCellString(row.getCell(indexPageCol)));
//                            return null;
//                        } else if ("提取整数信息".equals(getCellString(depart.getCell(indexPageCol)))) {
//                            localA0Rect.getGroupMessage(getCellInt(row.getCell(indexPageCol)));
//                            localA0Rect.wOrR = "xiaozu";
//
//                            //测试用
//                            Log.e(TAG, localA0Rect.groupMessage[0]);
//                            Log.e(TAG,localA0Rect.wOrR);
//                            return null;
//                        } else if ("N表".equals(getCellString(depart.getCell(indexPageCol)))) {
//                            Log.e(TAG,"成功进入N表");
//                            int rowNext = getCellInt(row.getCell(indexPageCol));
//                            localA0Rect.wOrR = "xingming";
//
//                            return searchLocalRectN(x, y, rowNext, pageNumber);
//                        } else {
//                            int rowNext = getCellInt(row.getCell(indexPageCol));
//                            localA0Rect.wOrR = "xieru";
//
//                            Log.e(TAG,"成功进如下一级索引表");
//                            String k = String.valueOf(rowNext);
//                            Log.e(TAG,"在下一级索引表的行数"+k);
//
//                            return searchLocalRect2(x, y, rowNext, pageNumber);
//                        }
//                    }
//                    else {  //对于G0要单独处理
//                        localA0Rect.getTimeMessage(getCellString(row.getCell(indexPageCol)));
//                        localA0Rect.wOrR = "zhou";
//                        //测试用
//                        Log.e(TAG,"对G0的特殊处理,压成一级");
//                        Log.e(TAG,getCellString(row.getCell(indexPageCol)));
//
//                        return null;
//                    }
//                } else {
//                    indexPageCol++;
//
//                }
//            }
//
//        }
//
//        return null;
//    }
//
//    public LocalA0Rect searchLocalRect2(int x, int y, int rowNext, int pageNumber) {
//
//        int indexPageCol = 1;
//        rowNext = rowNext - 1;
//        Log.e(TAG,"x是"+String.valueOf(x) + "y是" + String.valueOf(y));
//        int depart = 2;  //记录转去哪个表的行数
//        XSSFRow find = indexSheet2.getRow(depart); //得到这一行的对象
//        XSSFRow row = indexSheet2.getRow(rowNext);  //根据1级别索引表得到的在2级索引表中要搜索的行数
//        while (indexPageCol < 10000) {
//
//            int pageId1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(pageId1));//
//
//            int pageId2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(pageId2));//
//
//            int x1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x1));//
//
//            int y1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y1));//
//
//            int x2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x2));//
//
//            int y2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y2));//
//
//            int rowFirst = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(rowFirst));//
//
//            int rowLast = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(rowLast));//
//
//            String colFirst = getCellString(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(colFirst));//
//
//            String colLast = getCellString(row.getCell(indexPageCol));
//
//            Log.e(TAG,"这里是末尾"+String.valueOf(colLast));//
//
//            if (pageId1 != pageId2) {
//                Log.e(TAG,"不走了这里");
//                if (x2 < x1 && y2 > y1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = y2;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = y1;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//                }
//                if (y2 < y1 && x2 > x1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = x2;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//                    }
//
//                    if (pageNumber == pageId2) {
//                        int x4 = x1;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//                if (x1 > x2 && y1 > y2) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//                    }
//                    if (pageNumber == pageUR) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = y1;
//                        int x6 = x2;
//                        int y6 = Constants.A0_MAXY;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//                    }
//                    if (pageNumber == pageDL) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = Constants.A0_MINY;
//                        int x6 = Constants.A0_MAXX;
//                        int y6 = y2;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 0;
//                                return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                            }
//                            if ("TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                                localA0Rect.gOrTA = 1;
//                                return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                            }
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//            }
//
//            if (pageId1 == pageId2) {
//                Log.e(TAG,"yes page判断正确");
//                if (x < x2 && x > x1 && y < y2 && y > y1) {
//                    Log.e(TAG,"yes 走对了");
//
//                    Log.e(TAG,getCellString(find.getCell(indexPageCol)));
//
//                    if ("G".equals(getCellString(find.getCell(indexPageCol)))) {
//                        Log.e(TAG,"yes 走对了");
//                        Log.e(TAG,colFirst+colLast);
//                        localA0Rect.gOrTA = 0;
//                        return searchLocalRect3(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//
//                    }
//                    if ( "TA".equals(getCellString(find.getCell(indexPageCol)))) {
//                        Log.e(TAG,"no ,，没 走对");
//                        localA0Rect.gOrTA = 1;
//                        return searchLocalRect4(x, y, rowFirst, rowLast, colFirst, colLast, pageNumber);
//                    }
//                    else{
//                        Log.e(TAG,"判断失败");
//                        indexPageCol++;
//                    }
//                } else {
//                    Log.e(TAG,"no ,，没 走对");
//                    indexPageCol++;
//                }
//            }
//
//        }
//        return null;
//    }
//
//    public LocalA0Rect searchLocalRect3(int x, int y, int rowFirst, int rowLast, String colFirst, String colLast, int pageNumber) {
//        rowFirst = rowFirst -1;
//        rowLast = rowLast -1;
//        int colf = colNameToNumber(colFirst);
//        colf = colf -1;
//        for (int i = rowFirst; i <= rowLast; i++) {
//            int indexPageCol = colNameToNumber(colFirst);
//            indexPageCol = indexPageCol - 1;
//            XSSFRow row = sheet1.getRow(i);
//            XSSFRow row1 = sheet1.getRow(0); //读取的内容在第一行
//            int x1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,"x1="+String.valueOf(x1));//
//
//            int y1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y1));//
//
//            int pageId1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(pageId1));//
//
//            int x2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x2));//
//
//            int y2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y2));//
//
//            int pageId2 = getCellInt(row.getCell(indexPageCol));
//
//            Log.e(TAG,String.valueOf(pageId2));//
//
//            if (pageId1 != pageId2) {
//                if (x2 < x1 && y2 > y1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = y2;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//
//
//                            localA0Rect.pName = getCellString(row.getCell(1));
//
//
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = y1;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//                }
//                if (y2 < y1 && x2 > x1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = x2;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//                    }
//
//                    if (pageNumber == pageId2) {
//                        int x4 = x1;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//                if (x1 > x2 && y1 > y2) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageUR) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = y1;
//                        int x6 = x2;
//                        int y6 = Constants.A0_MAXY;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageDL) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = Constants.A0_MINY;
//                        int x6 = Constants.A0_MAXX;
//                        int y6 = y2;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            localA0Rect.time = getCellString(row1.getCell(colf));
//                            localA0Rect.pName = getCellString(row.getCell(1));
//                            break;
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//            }
//            if (pageId1 == pageId2) {
//                Log.e(TAG,"判断到了相等");
//                if (x < x2 && x > x1 && y < y2 && y > y1) {
//                    Log.e(TAG,"相等之后进来了");
//                    localA0Rect.time = getCellString(row1.getCell(colf));
//                    localA0Rect.pName = getCellString(row.getCell(1));
//                    //测试用
//                    Log.e(TAG, localA0Rect.time);
//                    Log.e(TAG,localA0Rect.pName);//
//                    break;
//                }
//            } else {
//                indexPageCol++;
//            }
//
//
//        }
//        return null;
//    }
//
//    public LocalA0Rect searchLocalRect4(int x, int y, int rowFirst, int rowLast, String colFirst, String colLast, int pageNumber) {
//
//        rowFirst = rowFirst -1;
//        rowLast = rowLast -1;
//        int colf = colNameToNumber(colFirst);
//        colf = colf -1;
//        XSSFRow row1 = sheet2.getRow(0);
//        for (int i = rowFirst; i <= rowLast; i++) {
//            int indexPageCol = colf;
//            XSSFRow row = sheet2.getRow(i);
//            int x1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x1));//
//
//            int y1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y1));//
//
//            int pageId1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(pageId1));//
//
//            int x2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x2));//
//
//            int y2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y2));//
//            int pageId2 = getCellInt(row.getCell(indexPageCol));
//
//            Log.e(TAG,String.valueOf(pageId2));//
//
//            if (pageId1 != pageId2) {
//                if (x2 < x1 && y2 > y1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = y2;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = y1;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//                }
//                if (y2 < y1 && x2 > x1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = x2;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//                    }
//
//                    if (pageNumber == pageId2) {
//                        int x4 = x1;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//                if (x1 > x2 && y1 > y2) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageUR) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = y1;
//                        int x6 = x2;
//                        int y6 = Constants.A0_MAXY;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageDL) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = Constants.A0_MINY;
//                        int x6 = Constants.A0_MAXX;
//                        int y6 = y2;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            localA0Rect.result = getCellString(row.getCell(2));
//                            localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                            localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                            LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                            //测试用
//                            Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                            break;
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//            } else if (pageId1 == pageId2) {
//                if (x < x2 && x > x1 && y < y2 && y > y1) {
//                    localA0Rect.result = getCellString(row.getCell(2));
//                    localA0Rect.field = getCellString(getMergedCell(sheet2, row.getCell(1))); //得到合并单元格的左上角，根据行号和列号来获得call
//                    localA0Rect.group = Integer.toString(getCellInt(getMergedCell(sheet2, row.getCell(0))));
//                    LocalA0Rect.time = getCellString(getMergedCell(sheet2,row1.getCell(indexPageCol)));
//                    //测试用
//                    Log.e(TAG, localA0Rect.result + localA0Rect.field + localA0Rect.group+localA0Rect.time);
//                    break;
//                }
//            } else {
//                indexPageCol++;
//            }
//
//        }
//        return null;
//    }
//
//    public LocalA0Rect searchLocalRectN(int x, int y, int rowNext, int pageNumber) {
//
//        Log.e(TAG,"现在是第几行，预计是一，实际是"+String.valueOf(rowNext));
//        rowNext = rowNext -1;
//        int indexPageCol = 1;
//        Log.e(TAG,"现在是第几行，预计是一，实际是"+String.valueOf(rowNext));
//        Log.e(TAG,String.valueOf(x) + "H"+String.valueOf(y));
//        XSSFRow row = sheet3.getRow(rowNext);
//
//        XSSFRow row11 = indexSheet1.getRow(2);
//        Log.e(TAG,getCellString(row11.getCell(1)));
//        while (indexPageCol < 10000) {
//
//            Integer pageId1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(pageId1));
//
//            int pageId2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(pageId2));
//
//            int x1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x1));
//
//            int y1 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y1));
//
//            int x2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(x2));
//
//            int y2 = getCellInt(row.getCell(indexPageCol++));
//
//            Log.e(TAG,String.valueOf(y2));
//
//            String name = getCellString(row.getCell(indexPageCol));
//
//            Log.e(TAG,name);
//
//            if (pageId1 != pageId2) {
//                if (x2 < x1 && y2 > y1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = y2;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = y1;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//
//                    } else {
//                        indexPageCol++;
//                    }
//                }
//                if (y2 < y1 && x2 > x1) {
//                    if (pageNumber == pageId1) {
//                        int x3 = x2;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//                    }
//
//                    if (pageNumber == pageId2) {
//                        int x4 = x1;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//                    } else {
//                        indexPageCol++;
//                    }
//
//                }
//                if (x1 > x2 && y1 > y2) {
//                    if (pageNumber == pageId1) {
//                        int x3 = Constants.A0_MAXX;
//                        int y3 = Constants.A0_MAXY;
//                        if (x > x1 && x < x3 && y > y1 && y < y3) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageId2) {
//                        int x4 = Constants.A0_MINX;
//                        int y4 = Constants.A0_MINY;
//                        if (x > x4 && x < x2 && y > y4 && y < y2) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageUR) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = y1;
//                        int x6 = x2;
//                        int y6 = Constants.A0_MAXY;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//                    }
//                    if (pageNumber == pageDL) {
//                        int x5 = Constants.A0_MINX;
//                        int y5 = Constants.A0_MINY;
//                        int x6 = Constants.A0_MAXX;
//                        int y6 = y2;
//                        if (x > x5 && x < x6 && y > y5 && y < y6) {
//                            localA0Rect.getNameMessage(name);
//                            break;
//                        }
//
//                    } else {
//                        indexPageCol++;
//
//                    }
//
//                }
//            }
//
//            if (pageId1 == pageId2) {
//                if (x < x2 && x > x1 && y < y2 && y > y1) {
//                    localA0Rect.getNameMessage(name);
//                    //测试用
//                    Log.e(TAG, localA0Rect.nameMessage[0]);
//                    break;
//                } else {
//                    indexPageCol++;
//
//                }
//
//            }
//        }
//
//        return null;
//    }
//
//}
//

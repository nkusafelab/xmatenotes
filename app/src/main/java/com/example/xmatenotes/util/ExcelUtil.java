package com.example.xmatenotes.util;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.SneakyThrows;

/**
 * Excel操作工具类
 */
@SuppressWarnings("all")
public class ExcelUtil {
    //=======================================  Workbook  =======================================

    //创建一个空的Workbook对象
    @SneakyThrows
    public static XSSFWorkbook createWorkbook() {
        return new XSSFWorkbook();
    }

    //以指定输入流为模版，创建一个Workbook对象
    @SneakyThrows
    public static XSSFWorkbook createWorkbookFromTemplate(InputStream is) throws IOException {
        return new XSSFWorkbook(is);
    }

    //以指定文件为模版，创建一个Workbook对象
    @SneakyThrows
    public static XSSFWorkbook createWorkbookFromTemplate(String file) throws IOException {
        InputStream is = new FileInputStream(file);
        return ExcelUtil.createWorkbookFromTemplate(is);
    }

    //将Workbook写出到指定输出流
    @SneakyThrows
    public static void write(XSSFWorkbook xbook, OutputStream os) throws IOException {
        xbook.write(os);
        os.flush();
        os.close();
    }

    //将Workbook写出到指定文件
    @SneakyThrows
    public static void write(XSSFWorkbook xbook, String file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        ExcelUtil.write(xbook, os);
    }

    //关闭Workbook
    @SneakyThrows
    public static void closeWorkbook(XSSFWorkbook xbook) throws IOException {
        xbook.close();
    }

    //=======================================  Sheet  =======================================

    //创建一个空的Sheet对象
    @SneakyThrows
    public static XSSFSheet createSheet(XSSFWorkbook workbook) {
        return workbook.createSheet();
    }

    //创建一个空的Sheet对象
    @SneakyThrows
    public static XSSFSheet createSheet(XSSFWorkbook workbook, String sheetName) {
        return workbook.createSheet(sheetName);
    }

    //获取指定位置的Sheet对象
    @SneakyThrows
    public static XSSFSheet getSheet(XSSFWorkbook workbook, int index) {
        return workbook.getSheetAt(index);
    }

    //获取指定名称的Sheet对象
    @SneakyThrows
    public static XSSFSheet getSheet(XSSFWorkbook workbook, String sheetName) {
        return workbook.getSheet(sheetName);
    }

    //复制一个Sheet到Workbook
    public static XSSFSheet cloneSheet(XSSFWorkbook xbook, String name, String newName) {
        XSSFSheet sheet = xbook.cloneSheet(xbook.getSheetIndex(name));
        if (newName != null && !newName.equals(""))
            xbook.setSheetName(xbook.getSheetIndex(sheet), newName);
        return sheet;
    }

    //移除指定位置的Sheet
    public static void removeSheet(XSSFWorkbook xbook, int index) {
        if (xbook.getNumberOfSheets() != 0)
            xbook.removeSheetAt(index);
    }

    //移除指定名称的Sheet
    public static void removeSheet(XSSFWorkbook xbook, String name) {
        if (xbook.getNumberOfSheets() != 0)
            xbook.removeSheetAt(xbook.getSheetIndex(name));
    }

    //=======================================  XSSFCell  =======================================

    //获取一个Cell对象
    public static XSSFCell getCell(XSSFSheet sheet, int row, int col) {
        if (sheet.getRow(row) == null)
            sheet.createRow(row);
        if (sheet.getRow(row).getCell(col) == null)
            sheet.getRow(row).createCell(col, Cell.CELL_TYPE_BLANK);
        return sheet.getRow(row).getCell(col);
    }

    //获取Cell格式类型
    //枚举值参考Cell.CELL_TYPE_XXX常量
    public static int getCellType(XSSFCell cell) {
        return cell.getCellType();
    }

    //获取文本单元格的值
    public static String getCellString(XSSFCell cell) {
        int cellType = cell.getCellType();
        if (cellType != Cell.CELL_TYPE_BLANK && cellType != Cell.CELL_TYPE_STRING && cellType != Cell.CELL_TYPE_NUMERIC)
//            throw BizException.of("unsupported cell type, a string type cell is needed");
            return "unsupported cell type, a string type cell is needed";
        cell.setCellType(Cell.CELL_TYPE_STRING);
        return cell.getStringCellValue().trim();
    }

    //获取文本单元格的值，并转为整数
    public static int getCellInt(XSSFCell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
            return (int) cell.getNumericCellValue();
        String value = getCellString(cell);
        return Integer.valueOf(value);
    }

    //获取文本单元格的值，并转为浮点数
    public static double getCellDouble(XSSFCell cell) {
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC)
            return cell.getNumericCellValue();
        String value = getCellString(cell);
        return Double.valueOf(value);
    }

    //获取文本单元格的值，并转为布尔型
    public static boolean getCellBool(XSSFCell cell, boolean default_value) {
        if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
            return cell.getBooleanCellValue();
        String value = getCellString(cell);
        if (value.equals("是") || value.equalsIgnoreCase("TRUE")) return true;
        if (value.equals("否") || value.equalsIgnoreCase("FALSE")) return false;
        return default_value;
    }

//    //获取文本单元格的值，并转为Date
//    public static Date getCellDate(XSSFCell cell) {
//        if (isDateCell(cell))
//            return DateUtil.getJavaDate(getCellDouble(cell));
//        String value = getCellString(cell);
//        Date date = Times.parseDate(value);
//        return date;
//    }

//    //获取文本单元格的值，并转为LocalDateTime
//    public static LocalDateTime getCellDateTime(XSSFCell cell) {
//        Date date = getCellDate(cell);
//        LocalDateTime dateTime = Times.getDateTime(date);
//        return dateTime;
//    }

    //获取文本单元格的值
    public static String getCellString(XSSFSheet sheet, int row, int col) {
        XSSFCell cell = getCell(sheet, row, col);
        return getCellString(cell);
    }

    //获取文本单元格的值，并转为整数
    public static int getCellInt(XSSFSheet sheet, int row, int col) {
        XSSFCell cell = getCell(sheet, row, col);
        return getCellInt(cell);
    }

    //获取文本单元格的值，并转为浮点数
    public static double getCellDouble(XSSFSheet sheet, int row, int col) {
        XSSFCell cell = getCell(sheet, row, col);
        return getCellDouble(cell);
    }

    //获取文本单元格的值，并转为布尔型
    public static boolean getCellBool(XSSFSheet sheet, int row, int col, boolean default_value) {
        XSSFCell cell = getCell(sheet, row, col);
        return getCellBool(cell, default_value);
    }

//    //获取单元格中的Date
//    public static Date getCellDate(XSSFSheet sheet, int row, int col) {
//        XSSFCell cell = getCell(sheet, row, col);
//        return getCellDate(cell);
//    }

//    //获取单元格中的Date
//    public static LocalDateTime getCellDateTime(XSSFSheet sheet, int row, int col) {
//        XSSFCell cell = getCell(sheet, row, col);
//        return getCellDateTime(cell);
//    }

    //设置文本单元格的值
    public static void setCellString(XSSFCell cell, Object text) {
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(new XSSFRichTextString(text.toString()));
    }

    //设置文本单元格的值
    public static void setCellString(XSSFSheet sheet, int row, int col, Object text) {
        XSSFCell cell = getCell(sheet, row, col);
        setCellString(cell, text == null ? "" : text.toString());
    }

    //判断单元格内容是否为空
    public static boolean isEmptyCell(XSSFCell cell) {
        return getCellString(cell).equals("");
    }

    //判断单元格内容是否为空
    public static boolean isEmptyCell(XSSFSheet sheet, int row, int col) {
        XSSFCell cell = getCell(sheet, row, col);
        return isEmptyCell(cell);
    }

//    //判断单元格是否是日期格式
//    public static boolean isDateCell(XSSFSheet sheet, int row, int col) {
//        XSSFCell cell = getCell(sheet, row, col);
//        return isDateCell(cell);
//    }

//    //判断单元格是否是日期格式
//    public static boolean isDateCell(XSSFCell cell) {
//        return DateUtil.isCellDateFormatted(cell);
//    }

    //=======================================  Merge  =======================================

    //获取合并单元格所在的区域
    public static CellRangeAddress getMergedCellAddress(XSSFSheet sheet, XSSFCell cell) {
        for (int mergeNums = sheet.getNumMergedRegions(), i = 0; i < mergeNums; ++i) {
            CellRangeAddress merger = sheet.getMergedRegion(i);
            boolean b1 = cell.getRowIndex() >= merger.getFirstRow() && cell.getRowIndex() <= merger.getLastRow();
            boolean b2 = cell.getColumnIndex() >= merger.getFirstColumn() && cell.getColumnIndex() <= merger.getLastColumn();
            if (b1 && b2) return merger;
        }
        return null;
    }

    //判断单元格是否位于合并区域
    public static boolean inMerger(XSSFSheet sheet, XSSFCell cell) {
        return getMergedCellAddress(sheet, cell) != null;
    }

    //判断单元格是否位于合并区域
    public static boolean inMerger(XSSFSheet sheet, int row, int col) {
        return inMerger(sheet, getCell(sheet, row, col));
    }

    //判断两个单元格是否位于同一合并区域
    public static boolean inSameMerger(XSSFSheet sheet, XSSFCell ca, XSSFCell cb) {
        int mergeNums = sheet.getNumMergedRegions();
        for (int i = 0; i < mergeNums; ++i) {
            CellRangeAddress merger = sheet.getMergedRegion(i);
            boolean b1 = ca.getRowIndex() >= merger.getFirstRow() && ca.getRowIndex() <= merger.getLastRow();
            boolean b2 = ca.getColumnIndex() >= merger.getFirstColumn() && ca.getColumnIndex() <= merger.getLastColumn();
            boolean b3 = cb.getRowIndex() >= merger.getFirstRow() && cb.getRowIndex() <= merger.getLastRow();
            boolean b4 = cb.getColumnIndex() >= merger.getFirstColumn() && cb.getColumnIndex() <= merger.getLastColumn();
            if (b1 && b2 && b3 && b4)
                return true;
        }
        return false;
    }

    //合并单元格
    public static void mergeCell(XSSFSheet sheet, int startRow, int endRow, int startCol, int endCol) {
        sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, startCol, endCol));
    }

    //=======================================  Alignment  =======================================

    //设置单元格内容对齐方式
    //枚举值参考CellStyle.ALIGN_XXX常量
    public static void setAlignment(XSSFCell cell, short ha, short va) {
        CellStyle style = cell.getCellStyle();
        style.setAlignment(ha);
        style.setVerticalAlignment(va);
        cell.setCellStyle(style);
    }

    //设置单元格内容对齐方式
    //枚举值参考CellStyle.ALIGN_XXX常量
    public static void setAlignment(XSSFSheet sheet, int row, int col, short ha, short va) {
        XSSFCell cell = getCell(sheet, row, col);
        CellStyle style = cell.getCellStyle();
        style.setAlignment(ha);
        style.setVerticalAlignment(va);
        cell.setCellStyle(style);
    }

    //=======================================  Wrap  =======================================

    //设置文本自动换行
    public static void setTextWrap(XSSFCell cell) {
        CellStyle style = cell.getCellStyle();
        style.setWrapText(true);
        cell.setCellStyle(style);
    }

    //设置文本自动换行
    public static void setTextWrap(XSSFSheet sheet, int row, int col) {
        XSSFCell cell = getCell(sheet, row, col);
        setTextWrap(cell);
    }

    //=======================================  Size  =======================================

    //自动调整单元格大小
    public static void autoSize(XSSFSheet sheet, int startCol, int endCol) {
        for (int col = startCol; col <= endCol; col++)
            sheet.autoSizeColumn(col);
    }

    //调整列宽度
    public static void width(XSSFSheet sheet, int col, int width) {
        sheet.setColumnWidth(col, (width * 256));
    }

    //调整行高度
    public static void height(XSSFSheet sheet, int row, int height) {
        if (sheet.getRow(row) == null) sheet.createRow(row);
        sheet.getRow(row).setHeight((short) (height * 20));
    }

    //=======================================  Visibility  =======================================

    //隐藏列
    public static void hideColumn(XSSFSheet sheet, int startCol, int endCol) {
        for (int col = startCol; col <= endCol; col++)
            sheet.setColumnHidden(col, true);
    }

    //=======================================  Border  =======================================

    //为单元格设置默认边框
    public static void setDefaultBorder(XSSFCell cell) {
        CellStyle style = cell.getCellStyle();
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setTopBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_80_PERCENT.getIndex());
        cell.setCellStyle(style);
    }

    //为单元格设置默认边框
    public static void setDefaultBorder(XSSFSheet sheet, int row, int col) {
        XSSFCell cell = getCell(sheet, row, col);
        setDefaultBorder(cell);
    }

    //设置单元格边框
    //枚举值参考CellStyle.BORDER_XXX常量
    public static void setBorder(XSSFCell cell, short borderStyle, IndexedColors color) {
        CellStyle style = cell.getCellStyle();
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setLeftBorderColor(color.getIndex());
        style.setRightBorderColor(color.getIndex());
        style.setTopBorderColor(color.getIndex());
        style.setBottomBorderColor(color.getIndex());
        cell.setCellStyle(style);
    }

    //设置单元格边框
    //枚举值参考CellStyle.BORDER_XXX常量
    public static void setBorder(XSSFSheet sheet, int row, int col, short borderStyle, IndexedColors color) {
        XSSFCell cell = getCell(sheet, row, col);
        setBorder(cell, borderStyle, color);
    }

    //=======================================  Fill  =======================================

    //设置填充色
    public static void setFillColor(XSSFCell cell, IndexedColors color) {
        CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFillForegroundColor(color.getIndex());
        style.setFillBackgroundColor(color.getIndex());
        cell.setCellStyle(style);
    }

    //设置填充色
    public static void setFillColor(XSSFSheet sheet, int row, int col, IndexedColors color) {
        XSSFCell cell = getCell(sheet, row, col);
        setFillColor(cell, color);
    }

    //=======================================  Font  =======================================

    //设置雅黑字体
    public static void setArialFont(XSSFCell cell, int size) {
        CellStyle style = cell.getCellStyle();
        XSSFWorkbook xbook = (XSSFWorkbook) cell.getSheet().getWorkbook();
        XSSFFont font = xbook.createFont();
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short) size);
        style.setFont(font);
        cell.setCellStyle(style);
    }

    //设置雅黑字体
    public static void setArialFont(XSSFSheet sheet, int row, int col, int size) {
        XSSFCell cell = getCell(sheet, row, col);
        setArialFont(cell, size);
    }

    //设置字体色
    public static void setFontColor(XSSFCell cell, IndexedColors color) {
        XSSFCellStyle style = (XSSFCellStyle) cell.getCellStyle();
        XSSFFont font = style.getFont();
        font.setColor(color.getIndex());
        style.setFont(font);
        cell.setCellStyle(style);
    }

    //设置字体色
    public static void setFontColor(XSSFSheet sheet, int row, int col, IndexedColors color) {
        XSSFCell cell = getCell(sheet, row, col);
        setFontColor(cell, color);
    }

    //=======================================  Style  =======================================

    //默认普通样式：居中对齐，雅黑字体
    public static void setDefaultNormalStyle(XSSFCell cell) {
        ExcelUtil.setAlignment(cell, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER);
        ExcelUtil.setArialFont(cell, 9);
    }

    //默认强调样式：黄色填充，居中对齐，文字换行，雅黑字体
    public static void setDefaultMarkStyle(XSSFCell cell) {
        ExcelUtil.setFillColor(cell, IndexedColors.YELLOW);
        ExcelUtil.setAlignment(cell, CellStyle.ALIGN_CENTER, CellStyle.VERTICAL_CENTER);
        ExcelUtil.setTextWrap(cell);
        ExcelUtil.setArialFont(cell, 9);
    }
}

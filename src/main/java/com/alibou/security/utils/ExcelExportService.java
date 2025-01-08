package com.alibou.security.utils;

import com.alibou.security.entity.StocksHistory;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class ExcelExportService {

    public void exportToExcel(HttpServletResponse response, List<StocksHistory> historyList, long fromDate, long toDate) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stocks Report");

        Row headerRow = sheet.createRow(0);

        // Hợp nhất 14 ô ở dòng đầu tiên và căn giữa
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13)); // Hợp nhất từ cột 0 đến cột 13
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Xuất / Nhập / Tồn kho");

        // Tăng chiều cao dòng lên gấp 3 lần (kích thước mặc định là 15, ví dụ muốn tăng lên 45)
        headerRow.setHeightInPoints(45);

        // Căn giữa cho ô đã hợp nhất
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCell.setCellStyle(headerStyle);

        // Tạo font in đậm và cỡ chữ 20
        Font headerFont = workbook.createFont();
        headerFont.setBold(true); // In đậm
        headerFont.setFontHeightInPoints((short) 20); // Cỡ chữ 20
        headerStyle.setFont(headerFont);

        // Tạo dòng thứ 2 (Thông tin Từ ngày và Đến ngày)
        Row infoRow = sheet.createRow(1);

        // Tạo các ô cho thông tin "Từ ngày" và "Đến ngày"
        infoRow.createCell(0).setCellValue("Từ ngày:");

        // Chuyển đổi fromDate và toDate sang dạng dd/MM/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        infoRow.createCell(1).setCellValue(dateFormat.format(fromDate*1000));

        infoRow.createCell(3).setCellValue("Đến ngày:");
        infoRow.createCell(4).setCellValue(dateFormat.format(toDate*1000));

        // Tạo header dòng 2
        Row headerRow2 = sheet.createRow(3);
        String[] headers = {"Tên sản phẩm", "Đơn vị tính", "Đơn giá", "Đầu kỳ", "", "Nhập trong kỳ", "", "Xuất trong kỳ", "", "Điều chỉnh", "", "Cuối kỳ", ""};
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow2.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(centerStyle); // Canh giữa dữ liệu
        }

        // Hợp nhất các ô có ô tiếp theo là ""
        for (int i = 0; i < headers.length - 1; i++) {
            if (headers[i + 1].equals("")) {
                sheet.addMergedRegion(new CellRangeAddress(3, 3, i, i + 1)); // Hợp nhất ô
                i++; // Bỏ qua ô tiếp theo đã được hợp nhất
            }
        }

        // Tạo header dòng 3
        Row headerRow3 = sheet.createRow(4);
        String[] headers2 = {"", "", "", "Số lượng", "Thành tiền", "Số lượng", "Thành tiền", "Số lượng", "Thành tiền", "Số lượng", "Thành tiền", "Số lượng", "Thành tiền"};
        for (int i = 0; i < headers2.length; i++) {
            Cell cell = headerRow3.createCell(i);
            cell.setCellValue(headers2[i]);
            cell.setCellStyle(centerStyle); // Canh giữa dữ liệu
        }

        // Hợp nhất các ô giữa headerRow2 và headerRow3
        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].equals("") && headers2[i].equals("")) {
                sheet.addMergedRegion(new CellRangeAddress(3, 4, i, i)); // Hợp nhất ô trong headerRow2 và headerRow3
            }
        }

        // Thiết lập độ rộng cho các cột
        sheet.setColumnWidth(0, 9000); // Cột 1 "Tên sản phẩm" độ rộng là 6000 (tương đương 600)
        for (int i = 1; i < headers.length; i++) {
            sheet.setColumnWidth(i, 4000); // Các cột còn lại độ rộng là 4000 (tương đương 400)
        }


        // Thêm dữ liệu
        int rowIndex = 5;
        for (StocksHistory history : historyList) {
            Row row = sheet.createRow(rowIndex++);

            // Tạo các ô
            row.createCell(0).setCellValue(history.getName());
            row.createCell(2).setCellValue(history.getPrice());

            // Tạo CellStyle để canh giữa cho ô thứ 3 (history.getPrice())
            CellStyle centerStyle2 = workbook.createCellStyle();
            centerStyle2.setAlignment(HorizontalAlignment.CENTER); // Canh giữa theo chiều ngang
            centerStyle2.setVerticalAlignment(VerticalAlignment.CENTER); // Canh giữa theo chiều dọc

            // Áp dụng style cho ô chứa giá trị (ô thứ 3)
            row.getCell(2).setCellStyle(centerStyle2);
        }

        // Ghi vào response
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=stocks_report.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}

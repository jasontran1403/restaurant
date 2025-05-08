package com.alibou.security.utils;

import com.alibou.security.entity.Food;
import com.alibou.security.entity.StocksHistory;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.repository.StocksHistoryRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReportService {

    private final FoodRepository foodRepository;
    private final StocksHistoryRepository stocksHistoryRepository;

    public void exportStockReport(String type, HttpServletResponse response, long dateStartMillis, long dateEndMillis) throws IOException {
        List<Food> foods = foodRepository.findAll();
        List<StocksHistory> allHistories = stocksHistoryRepository.getAllStocksHistory(type);

        // Filter histories within the date range
        List<StocksHistory> relevantHistories = allHistories.stream()
                .filter(h -> h.getDate() >= dateStartMillis && h.getDate() <= dateEndMillis)
                .toList();

        // Get unique dates with records
        Set<LocalDate> activeDates = new HashSet<>();
        for (StocksHistory history : relevantHistories) {
            LocalDate date = Instant.ofEpochSecond(history.getDate()).atZone(ZoneId.systemDefault()).toLocalDate();
            activeDates.add(date);
        }
        List<LocalDate> sortedDates = activeDates.stream().sorted().toList();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        // Convert dateStartMillis and dateEndMillis to dd/MM/yyyy format
        String startDateFormatted = sdf.format(new Date(dateStartMillis * 1000)); // Convert to milliseconds
        String endDateFormatted = sdf.format(new Date(dateEndMillis * 1000)); // Convert to milliseconds

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stock Report");

        // Create the first row (header)
        Row headerRow = sheet.createRow(0);
        int totalColumns = 2 + sortedDates.size() + 1; // Columns for Product, Initial Stock, Dates, and Final Stock

        // Merge the cells for the header title, covering all columns
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalColumns - 1)); // Merge from column 0 to last column

        // Set the header cell value
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Báo cáo Tồn kho");

        // Adjust header row height
        headerRow.setHeightInPoints(45);

        // Apply header cell style (center alignment, bold, font size 20)
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerCell.setCellStyle(headerStyle);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 20);
        headerStyle.setFont(headerFont);

        // Create the second row (Information row for "From Date" and "To Date")
        Row infoRow = sheet.createRow(1);

        // Merge the cells for the "From Date" and "To Date" information, covering all columns
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, totalColumns - 1)); // Merge from column 0 to last column

        // Set the text for the merged cell
        Cell infoCell = infoRow.createCell(0);
        String infoText = "Từ ngày " + startDateFormatted + " đến ngày " + endDateFormatted;
        infoCell.setCellValue(infoText);

        // Apply info cell style (left alignment, center vertically)
        CellStyle infoStyle = workbook.createCellStyle();
        infoStyle.setAlignment(HorizontalAlignment.LEFT);
        infoStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        infoCell.setCellStyle(infoStyle);

        infoRow.setHeightInPoints(18);

        // Tạo style căn giữa
        CellStyle centerStyle = workbook.createCellStyle();
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Create border style
        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        Row header = sheet.createRow(2);
        header.createCell(0).setCellValue("Tên sản phẩm");

        Cell cell = header.createCell(1);
        cell.setCellValue("Tồn kho đầu");
        cell.setCellStyle(centerStyle);

        int colIdx = 2;
        for (LocalDate date : sortedDates) {
            header.createCell(colIdx++).setCellValue(date.toString());
        }

        cell = header.createCell(colIdx);
        cell.setCellValue("Tồn kho cuối");
        cell.setCellStyle(centerStyle);

        // Apply border to header cells
        for (int i = 0; i <= colIdx; i++) {
            header.getCell(i).setCellStyle(borderStyle);
        }

        int rowIdx = 3;

        for (Food food : foods) {
            Row row = sheet.createRow(rowIdx++);
            colIdx = 0;

            // Food name column
            row.createCell(colIdx++).setCellValue(food.getName());
            row.getCell(0).setCellStyle(borderStyle); // Apply border to food name cell

            // Initial stock calculation (no history before dateStartMillis)
            int initialStockIn = stocksHistoryRepository.calculateInitStockReport(food.getName(), dateStartMillis, "In");
            int initialStockOut = stocksHistoryRepository.calculateInitStockReport(food.getName(), dateStartMillis, "Out");
            int initialStockAdjustt = stocksHistoryRepository.calculateInitStockReport(food.getName(), dateStartMillis, "Adjustment");
            int initialStock = initialStockIn - initialStockOut + initialStockAdjustt;

            // Set the initial stock value in the Excel sheet
            row.createCell(colIdx++).setCellValue(initialStock);
            row.getCell(colIdx - 1).setCellStyle(centerStyle); // Center align the initial stock
            row.getCell(colIdx - 1).setCellStyle(borderStyle); // Apply border to initial stock cell

            int totalChange = 0; // Variable for calculating total changes (final stock)

            // Define cell styles for color coding (Green, Red, Yellow)
            CellStyle greenStyle = workbook.createCellStyle();
            greenStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            greenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle redStyle = workbook.createCellStyle();
            redStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            redStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle yellowStyle = workbook.createCellStyle();
            yellowStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Process each active date
            for (LocalDate date : sortedDates) {
                long dayStartMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
                long dayEndMillis = dayStartMillis + 24 * 60 * 60;

                // Calculate in quantities for this day (In type)
                int inQty = relevantHistories.stream()
                        .filter(h -> h.getName().equals(food.getName()))
                        .filter(h -> h.getType().equalsIgnoreCase("In"))
                        .filter(h -> h.getDate() >= dayStartMillis && h.getDate() < dayEndMillis)
                        .mapToInt(StocksHistory::getQuantity)
                        .sum();

                // Calculate out quantities for this day (Out type)
                int outQty = relevantHistories.stream()
                        .filter(h -> h.getName().equals(food.getName()))
                        .filter(h -> h.getType().equalsIgnoreCase("Out"))
                        .filter(h -> h.getDate() >= dayStartMillis && h.getDate() < dayEndMillis)
                        .mapToInt(StocksHistory::getQuantity)
                        .sum();

                // Calculate adjustment quantities for this day (Adjustment type)
                int adjusttQty = relevantHistories.stream()
                        .filter(h -> h.getName().equals(food.getName()))
                        .filter(h -> h.getType().equalsIgnoreCase("Adjustment"))
                        .filter(h -> h.getDate() >= dayStartMillis && h.getDate() < dayEndMillis)
                        .mapToInt(StocksHistory::getQuantity)
                        .sum();

                // Calculate daily change: In - Out + Adjustment
                int dailyChange = inQty - outQty + adjusttQty;
                totalChange += dailyChange;

                // Create and set the daily change value for this date in the Excel sheet
                Cell cellDaily = row.createCell(colIdx++);
                cellDaily.setCellValue(dailyChange);
                cellDaily.setCellStyle(centerStyle); // Center align the daily change cell
                cellDaily.setCellStyle(borderStyle); // Apply border to daily change cell

                // Add color to the daily change cell based on the value
                if (dailyChange > 0) {
                    cellDaily.setCellStyle(greenStyle); // Green for positive change
                } else if (dailyChange < 0) {
                    cellDaily.setCellStyle(redStyle); // Red for negative change
                } else {
                    cellDaily.setCellStyle(yellowStyle); // Yellow for zero change
                }

                // Apply border style to the daily change cell
                row.getCell(colIdx - 1).setCellStyle(borderStyle);

                if (inQty != 0 || outQty != 0) {
                    // Create drawing object for the comment
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    CreationHelper factory = workbook.getCreationHelper();
                    ClientAnchor anchor = factory.createClientAnchor();
                    anchor.setCol1(cellDaily.getColumnIndex());
                    anchor.setCol2(cellDaily.getColumnIndex() + 2);
                    anchor.setRow1(row.getRowNum());
                    anchor.setRow2(row.getRowNum() + 3);

                    // Create the comment
                    Comment comment = drawing.createCellComment(anchor);

                    // Create a rich text string for the comment content
                    XSSFRichTextString richTextString = new XSSFRichTextString(
                            "In: " + inQty + "\nOut: " + outQty + "\nAdjustment: " + adjusttQty);

                    // Create font for comment text
                    Font font = workbook.createFont();

                    if (dailyChange > 0) {
                        font.setColor(IndexedColors.GREEN.getIndex()); // Green for positive change
                    } else if (dailyChange < 0) {
                        font.setColor(IndexedColors.RED.getIndex()); // Red for negative change
                    } else {
                        font.setColor(IndexedColors.YELLOW.getIndex()); // Yellow for zero change
                    }

                    richTextString.applyFont(font);

                    // Set the comment string to the cell comment
                    comment.setString(richTextString);
                    cellDaily.setCellComment(comment);
                }
            }

            // Write the final stock value (initial stock + total change) to the sheet
            row.createCell(colIdx).setCellValue(initialStock + totalChange);
            row.getCell(colIdx).setCellStyle(centerStyle); // Center align the final stock cell
            row.getCell(colIdx).setCellStyle(borderStyle); // Apply border to final stock cell
        }

        // Auto-size columns
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=stocks_report.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}

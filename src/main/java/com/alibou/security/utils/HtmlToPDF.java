package com.alibou.security.utils;

import com.alibou.security.dto.OrderPDFDTO;
import com.alibou.security.entity.Agency;
import com.alibou.security.entity.OrderDetail;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@RequiredArgsConstructor
public class HtmlToPDF {
    private static final String TEMPLATE_PATH = "pdf.html";

    public static byte[] GenerateInvoicePdf(OrderPDFDTO orderDTO, Agency agency) throws IOException {
        // Đọc nội dung HTML từ file trong classpath
        InputStream inputStream = HtmlToPDF.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Template file not found in classpath: " + TEMPLATE_PATH);
        }
        String htmlContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        double totalAfterFee = orderDTO.getOrder().getTotal();
        double fee = orderDTO.getOrderDetails().get(0).getRate();
        double vat = orderDTO.getOrder().getVat();

        double totalBeforeFee = 0.0;

        for (OrderDetail item : orderDTO.getOrderDetails()) {
            totalBeforeFee += item.getPrice() * item.getQuantity();
        }
        double priceFee = fee * totalBeforeFee / 100;

        // Thay thế các placeholder trong HTML bằng dữ liệu từ OrderPDFDTO
        htmlContent = htmlContent.replace("{{order_staff}}", String.format("%10s", orderDTO.getName()));
        Instant instant = Instant.ofEpochSecond(orderDTO.getDate());
        LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        int day = localDate.getDayOfMonth();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();
        long count = orderDTO.getOrder().getId();
        String nameConverted = orderDTO.getName().toUpperCase();
        String orderId = nameConverted + count + day + month + year;

        long payment = (long)(totalAfterFee + vat/100.0 * totalAfterFee);
        String support = agency.getEmail() + " // " + agency.getPhone();
        String staffName = agency.getFullname();

        String qr_url = "https://img.vietqr.io/image/BIDV-8812856177-qr_only.jpg?amount=" + payment + "&addInfo=Thanh%20toan%20don%20hang%20tai%20OriginalTaste&accountName=NGUYEN%20LE%20HOAI%20VIET";
        htmlContent = htmlContent.replace("{{order_id}}", String.format("%30s", orderId));
        htmlContent = htmlContent.replace("{{customer_name}}", orderDTO.getOrder().getName());
        htmlContent = htmlContent.replace("{{customer_phone}}", orderDTO.getOrder().getPhone());
        htmlContent = htmlContent.replace("{{customer_address}}", orderDTO.getOrder().getAddress());
        htmlContent = htmlContent.replace("{{customer_addressReceive}}", orderDTO.getOrder().getAddressReceive());
        htmlContent = htmlContent.replace("{{print_time}}", formatDate(System.currentTimeMillis() / 1000));
        htmlContent = htmlContent.replace("{{order_note}}", orderDTO.getOrder().getMessage());
        htmlContent = htmlContent.replace("{{order_totalBeforeFee}}", formatCurrency(totalBeforeFee));
        htmlContent = htmlContent.replace("{{order_sale}}", String.format("%.0f", fee));
        htmlContent = htmlContent.replace("{{order_salePrice}}", formatCurrency(priceFee));
        htmlContent = htmlContent.replace("{{order_vat}}", String.format("%.0f", vat));
        htmlContent = htmlContent.replace("{{order_vatPrice}}", formatCurrency(vat/100*totalAfterFee));
        htmlContent = htmlContent.replace("{{order_totalAfterFee}}", formatCurrency(totalAfterFee+vat/100*totalAfterFee));
        htmlContent = htmlContent.replace("{{order_totalPayment}}", String.format("%s", payment));
        htmlContent = htmlContent.replace("{{qr_url}}", String.format("%s", qr_url));
        htmlContent = htmlContent.replace("{{support}}", String.format("%s", support));
        htmlContent = htmlContent.replace("{{staffName}}", String.format("%s", staffName));


        htmlContent = htmlContent.replace("{{deli_Time}}", formatDay(System.currentTimeMillis() / 1000));


        // Tạo nội dung bảng sản phẩm
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderDetail detail : orderDTO.getOrderDetails()) {
            itemsHtml.append("<tr>")
                    .append("<td>").append(detail.getName()).append("</td>")
                    .append("<td class=\"center\">").append(detail.getQuantity()).append(" X ").append(detail.getUnit()).append("</td>")
                    .append("<td class=\"center\">").append(formatCurrency(detail.getPrice())).append("</td>")
                    .append("<td class=\"center\">").append(formatCurrency(detail.getPrice() * detail.getQuantity())).append("</td>")
                    .append("</tr>");
        }

        // Thay thế placeholder bảng sản phẩm
        htmlContent = htmlContent.replace("{{order_items}}", itemsHtml.toString());

        // Chuyển đổi HTML sang PDF
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)), outputStream);
            return outputStream.toByteArray();
        }
    }

    private static String formatDate(long epochTime) {
        return new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new java.util.Date(epochTime * 1000));
    }

    private static String formatDay(long epochTime) {
        // Chuyển epochTime (s) → LocalDateTime
        LocalDateTime inputTime = Instant.ofEpochSecond(epochTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        int hour = inputTime.getHour();
        // Tính ngày giao hàng
        LocalDate deliveryDate = (hour < 15)
                ? inputTime.toLocalDate().plusDays(1)
                : inputTime.toLocalDate().plusDays(2);

        // Định dạng ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return deliveryDate.format(formatter);
    }


    private static String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}

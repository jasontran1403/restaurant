package com.alibou.security.utils;

import com.alibou.security.dto.OrderPDFDTO;
import com.alibou.security.entity.OrderDetail;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;


@RequiredArgsConstructor
public class HtmlToPDF {
    private static final String TEMPLATE_PATH = "pdf.html";

    public static byte[] GenerateInvoicePdf(OrderPDFDTO orderDTO) throws IOException {
        // Đọc nội dung HTML từ file
        String htmlContent = new String(Files.readAllBytes(Paths.get(TEMPLATE_PATH)));

        double totalAfterFee = orderDTO.getOrder().getTotal();
        double fee = orderDTO.getOrderDetails().get(0).getRate();

        double totalBeforeFee = 0.0;

        for (OrderDetail item : orderDTO.getOrderDetails()) {
            totalBeforeFee += item.getPrice() * item.getQuantity();
        }
        double priceFee = orderDTO.getOrderDetails().get(0).getRate() * totalBeforeFee / 100;

        // Thay thế các placeholder trong HTML bằng dữ liệu từ OrderPDFDTO
        String name = "Jason Trần";
        htmlContent = htmlContent.replace("{{order_staff}}", String.format("%10s", name));
        int day = 15;
        int month = 12;
        int year = 2024;
        long count = orderDTO.getOrder().getId();
        String nameConverted = "jason".toUpperCase();
        String oderId = count + nameConverted + count + day + month + year;
        htmlContent = htmlContent.replace("{{order_id}}", String.format("%30s", oderId));
        htmlContent = htmlContent.replace("{{customer_name}}", orderDTO.getOrder().getName());
        htmlContent = htmlContent.replace("{{customer_phone}}", orderDTO.getOrder().getPhone());
        htmlContent = htmlContent.replace("{{customer_address}}", orderDTO.getOrder().getAddress());
        htmlContent = htmlContent.replace("{{order_time}}", formatDate(orderDTO.getOrder().getTime()));
        htmlContent = htmlContent.replace("{{print_time}}", formatDate(System.currentTimeMillis()/1000));
        htmlContent = htmlContent.replace("{{order_note}}", orderDTO.getOrder().getMessage());
        htmlContent = htmlContent.replace("{{order_totalBeforeFee}}", formatCurrency(totalBeforeFee));
        htmlContent = htmlContent.replace("{{order_salePrice}}", formatCurrency(priceFee));
        htmlContent = htmlContent.replace("{{order_sale}}", String.format("%.2f", fee));
        htmlContent = htmlContent.replace("{{order_totalAfterFee}}", formatCurrency(totalAfterFee));


        // Tạo nội dung bảng sản phẩm
        StringBuilder itemsHtml = new StringBuilder();
        for (OrderDetail detail : orderDTO.getOrderDetails()) {
            itemsHtml.append("<tr>")
                    .append("<td>").append(detail.getName()).append("</td>")
                    .append("<td class=\"center\">").append(detail.getQuantity()).append("</td>")
                    .append("<td class=\"center\">").append(formatCurrency(detail.getPrice())).append("</td>")
                    .append("<td class=\"center\">").append(formatCurrency(detail.getPrice() * detail.getQuantity())).append("</td>")
                    .append("</tr>");
        }

        // Thay thế placeholder bảng sản phẩm
        htmlContent = htmlContent.replace("{{order_items}}", itemsHtml.toString());

        // Chuyển đổi HTML sang PDF
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(new ByteArrayInputStream(htmlContent.getBytes()), outputStream);
            return outputStream.toByteArray();
        }
    }

    private static String formatDate(long epochTime) {
        return new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm:ss").format(new java.util.Date(epochTime * 1000));
    }

    private static String formatCurrency(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}

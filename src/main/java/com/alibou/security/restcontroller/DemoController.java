package com.alibou.security.restcontroller;

import com.alibou.security.dto.CustomerInfoResponse;
import com.alibou.security.dto.LoginRequest;
import com.alibou.security.dto.LoginResponse;
import com.alibou.security.dto.OrderPDFDTO;
import com.alibou.security.entity.Order;
import com.alibou.security.entity.OrderDetail;
import com.alibou.security.entity.StocksHistory;
import com.alibou.security.repository.AgencyRepository;
import com.alibou.security.repository.CommissionHistoryRepository;
import com.alibou.security.repository.OrderDetailRepository;
import com.alibou.security.service.OrderService;
import com.alibou.security.service.StocksService;
import com.alibou.security.utils.*;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/demo-controller")
@Hidden
public class DemoController {
  @Autowired
  OrderService orderService;
  @Autowired
  private OrderDetailRepository orderDetailRepository;
  @Autowired
  private AgencyRepository agencyRepository;
  @Autowired
  private CommissionHistoryRepository commissionHistoryRepository;
  @Autowired
  TelegramService telegramService;
  @Autowired
  private StocksService service;
  @Autowired
  private StockReportService stockReportService;

  @GetMapping("/customer-info/{name}")
  public ResponseEntity<CustomerInfoResponse> GetCustomerInfo(@PathVariable String name) {
    CustomerInfoResponse response = new CustomerInfoResponse();

    return ResponseEntity.ok(response);
  }


  @GetMapping
  public ResponseEntity<String> sayHello() {
    String message = "[Test message]";
    telegramService.sendMessageToGroup(message);
    return ResponseEntity.ok("Hello from secured endpoint");
  }

  @GetMapping("/generate-invoice/{orderId}")
  public ResponseEntity<ByteArrayResource> generateInvoice(@PathVariable Long orderId) {
    var orderHistory = commissionHistoryRepository.findCommissionHistoryByOrderId(orderId);
    if (orderHistory.isEmpty()) return ResponseEntity.notFound().build();
    try {
      // Lấy thông tin đơn hàng từ orderId
      Optional<Order> orderOptional = orderService.findOrderById(orderId);
      if (orderOptional.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      List<OrderDetail> orderDetails = orderDetailRepository.getOrderDetailsById(orderOptional.get().getId());

      String name = orderHistory.get().getReceiveF2() != null
              ? orderHistory.get().getReceiveF2()
              : orderHistory.get().getReceiveF1();

      var agency = agencyRepository.findByUsername(orderOptional.get().getStaff());

      OrderPDFDTO requestHTMLToPDF = new OrderPDFDTO();
      requestHTMLToPDF.setOrder(orderOptional.get());
      requestHTMLToPDF.setName(name);
      requestHTMLToPDF.setDate(orderHistory.get().getDate());
      requestHTMLToPDF.setOrderDetails(orderDetails);
      // Tạo PDF từ đơn hàng
      byte[] pdfBytes = HtmlToPDF.GenerateInvoicePdf(requestHTMLToPDF, agency.get());
      ByteArrayResource resource = new ByteArrayResource(pdfBytes);

      // Thiết lập headers
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf");
      headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

      return ResponseEntity.ok()
              .headers(headers)
              .contentLength(pdfBytes.length)
              .contentType(MediaType.APPLICATION_PDF)
              .body(resource);

    } catch (Exception e) {
      // Log lỗi và trả về 500 Internal Server Error
      System.out.println(e.getMessage());
      return ResponseEntity.status(500).build();
    }
  }


}

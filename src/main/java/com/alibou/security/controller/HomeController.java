package com.alibou.security.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibou.security.dto.*;
import com.alibou.security.entity.*;
import com.alibou.security.repository.AgencyRepository;
import com.alibou.security.repository.CommissionHistoryRepository;
import com.alibou.security.repository.CustomerRepository;
import com.alibou.security.repository.OrderRepository;
import com.alibou.security.service.*;
import com.alibou.security.user.UserRepository;
import com.alibou.security.utils.ExcelExportService;
import com.alibou.security.utils.HtmlToPDF;
import com.alibou.security.utils.StockReportService;
import com.alibou.security.utils.TelegramService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {
    @Autowired
    private StockReportService stockReportService;

    @Autowired
    FoodService foodService;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderDetailService orderDetailService;

    @Autowired
    CouponService coupService;

    @Autowired
    CategoryService cateService;

    @Autowired
    ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AgencyRepository agencyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CommissionHistoryRepository commissionHistoryRepository;

    @Autowired
    StocksService stocksService;

    @Autowired
    TelegramService telegramService;

    @Autowired
    AgencyService agencyService;
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/login")
    public String login() {
        return "landing-page/login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new SignupRequest()); // Đổi tên attribute thành "user"
        return "landing-page/signup";
    }

    @PostMapping("/signup")
    public String handleSignup(@Valid @ModelAttribute("user") SignupRequest request,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "landing-page/signup";
        }

        try {
            // Thử tạo user
            boolean created = agencyService.createUser(request);

            if (!created) {
                // Nếu username/email đã tồn tại
                redirectAttributes.addFlashAttribute("errorMessage", "Username or email already exists");
                return "redirect:/signup";
            }

            // Thành công
            redirectAttributes.addFlashAttribute("successMessage", "Signup successful! Redirecting to login...");
            return "redirect:/signup?success";
        } catch (Exception e) {
            // Lỗi server/database
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed. Please try again later.");
            return "redirect:/signup";
        }
    }


    @GetMapping("/sales")
    public String staff(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập tài khoản để theo dõi doanh thu cá nhân!");
            return "redirect:/login";
        }

        var agency = agencyRepository.findByUsername(loggedInUser);

        List<Order> listOrders = orderService.findOrderByStaff(loggedInUser);

        List<OrderDetailDTO> listOrdersDetail = new ArrayList<>();

        for (Order order : listOrders) {
            var commissionHistory = commissionHistoryRepository.findCommissionHistoryByOrderId(order.getId());
            OrderDetailDTO item = new OrderDetailDTO();
            if (commissionHistory.isPresent()) {
                item.setCommission(commissionHistory.get().getAmount());
            } else {
                item.setCommission(0);
            }
            item.setId(order.getId());
            item.setName(order.getName());
            item.setPhone(order.getPhone());
            item.setAddress(order.getAddress());
            item.setTime(convertUnixToDateTime(order.getTime()));
            item.setTotal(order.getTotal());
            item.setVat(order.getVat());
            item.setActual(order.getActual());
            item.setStatus(order.getStatus());
            item.setUserRole(order.getUserRole());
            listOrdersDetail.add(item);
        }

        model.addAttribute("listOrders", listOrdersDetail);

        model.addAttribute("userRole", agency.get().getRole());
        return "landing-page/staff";
    }

    public static String convertUnixToDateTime(long timeUnix) {
        // Định dạng thời gian mong muốn
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        // Set múi giờ GMT+7
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        // Chuyển đổi từ Unix timestamp sang định dạng chuỗi
        return sdf.format(new Date(timeUnix * 1000)); // Nhân 1000 vì Unix timestamp tính bằng giây
    }


    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate(); // Xoá toàn bộ session
        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginExecute(@RequestParam String username,
                               @RequestParam String password,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        // Kiểm tra nếu username hoặc password rỗng
        if (username.isEmpty() || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Username và Password không được để trống!");
            return "redirect:/login";
        }

        // Xác thực tài khoản
        boolean isAuthenticated = agencyService.isAuthenticated(username, password);
        var agency = agencyRepository.findByUsername(username);

        if (isAuthenticated) {
            session.setAttribute("loggedInUser", username);
            if (agency.isPresent()) {
                if (agency.get().getRole().equalsIgnoreCase("admin staff")) {
                    return "redirect:/admin-staff";
                } else if (agency.get().getRole().equalsIgnoreCase("admin customer")) {
                    return "redirect:/admin-customer";
                } else {
                    return "redirect:/";
                }
            } else {
                return "redirect:/login";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Đăng nhập thất bại, vui lòng kiểm tra lại thông tin!");
            return "redirect:/login";
        }
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        return "landing-page/cart";
    }

    @GetMapping({"/menu", "/"})
    public String menu(Model model, @RequestParam(name = "page", defaultValue = "0") int page, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        var agency = agencyRepository.findByUsername(loggedInUser);

        if (agency.isPresent()) {
            if (agency.get().getRole().equalsIgnoreCase("admin staff")) {
                return "redirect:/admin-staff";
            } else if (agency.get().getRole().equalsIgnoreCase("admin customer")) {
                return "redirect:/admin-customer";
            }
        }

        int pageSize = 100;
        String type = agency.get().getRole();

        Page<Food> foods = foodService.getPaginatedFoodsShow(type, PageRequest.of(page, pageSize));
        model.addAttribute("foodPage", foods);

        List<Category> cates = cateService.getAllCates();

        model.addAttribute("cates", cates);
        return "landing-page/menu";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "query") String query, Model model,
                         @RequestParam(name = "page", defaultValue = "0") int page, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        model.addAttribute("query", query);
        int pageSize = 6;
        Page<Food> foods = foodService.findPaginatedFoods(PageRequest.of(page, pageSize), query);
        model.addAttribute("foodPage", foods);

        List<Category> cates = cateService.getAllCates();
        model.addAttribute("cates", cates);
        return "landing-page/search";
    }

    @PostMapping("/add-review")
    public String addReview(AddReviewRequest request) {
        // Xử lý yêu cầu ở đây
        reviewService.addReview(request);
        return "redirect:/product/" + request.getFoodId();
    }

    @GetMapping("/product/{id}")
    public String product(Model model, @PathVariable Long id, @RequestParam(name = "page", defaultValue = "0") int page, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        Food food = foodService.getById(id);
        model.addAttribute("food", food);
        AddReviewRequest review = new AddReviewRequest();
        review.setFoodId(id);
        model.addAttribute("review", review);

        List<Food> foods = foodService.getAll().subList(0, 3);
        model.addAttribute("topSellers", foods);

        int pageSize = 6;
        Page<Review> reviews = reviewService.getReviewByFoodIdAndPageable(PageRequest.of(page, pageSize), id);
        model.addAttribute("reviewPage", reviews);
        return "landing-page/product";
    }

    @GetMapping({"/admin-staff/dashboard", "/admin-staff/", "/admin-staff", "/admin-staff/dashboard/{date}"})
    public String dashboardStaff(Model model, @PathVariable(name = "date", required = false) String date, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        var agencyOptional = agencyRepository.findByUsername(loggedInUser);

        if (agencyOptional.isPresent() && !agencyOptional.get().getRole().equalsIgnoreCase("admin staff")) {
            session.invalidate(); // Xoá toàn bộ session
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải là admin để truy cập tài nguyên này!");
            return "redirect:/login";
        }

        long timestamp = getTimestamp();

        if (date == null || date.equals("today") || date.isBlank()) {
            processDashboard(model, timestamp, timestamp + 86400);
        } else if (date.equals("lastweek")) {
            processDashboard(model, timestamp - 7 * 86400, timestamp);
        } else if (date.equals("lastmonth")) {
            processDashboard(model, timestamp - 30 * 86400, timestamp);
        }


        return "admin/index-staff";
    }

    @GetMapping({"/admin-customer/dashboard", "/admin-customer/", "/admin-customer", "/admin-customer/dashboard/{date}"})
    public String dashboardCustomer(Model model, @PathVariable(name = "date", required = false) String date, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        var agencyOptional = agencyRepository.findByUsername(loggedInUser);

        if (agencyOptional.isPresent() && !agencyOptional.get().getRole().equalsIgnoreCase("admin customer")) {
            session.invalidate(); // Xoá toàn bộ session
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không phải là admin để truy cập tài nguyên này!");
            return "redirect:/login";
        }

        long timestamp = getTimestamp();

        if (date == null || date.equals("today") || date.isBlank()) {
            processDashboard(model, timestamp, timestamp + 86400);
        } else if (date.equals("lastweek")) {
            processDashboard(model, timestamp - 7 * 86400, timestamp);
        } else if (date.equals("lastmonth")) {
            processDashboard(model, timestamp - 30 * 86400, timestamp);
        }


        return "admin/index-customer";
    }

    private long getTimestamp() {
        Date currentDateTime = new Date();

        // Lấy ngày hiện tại
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(currentDateTime);

        // Đặt thời gian thành 00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Lấy timestamp sau khi đặt thời gian
        long timestamp = calendar.getTimeInMillis() / 1000;
        return timestamp;
    }

    private void processDashboard(Model model, long timeFrom, long timeTo) {
        List<Double> statistics = orderService.getTodayReport(timeFrom, timeTo);
        List<BestSellerDto> bestSellers = orderService.getBestSellerStat();

        model.addAttribute("todaySales", statistics.get(0));
        model.addAttribute("todayOrders", statistics.get(1));
        model.addAttribute("ordersCompleted", statistics.get(2));
        model.addAttribute("ordersCanceled", statistics.get(3));
        model.addAttribute("expectSales", statistics.get(4));
        model.addAttribute("newOrders", statistics.get(5));
        model.addAttribute("deliveryOrders", statistics.get(6));
        model.addAttribute("highest", orderService.getHighestOrder(timeFrom, timeTo));
        model.addAttribute("lowest", orderService.getLowestOrder(timeFrom, timeTo));
        model.addAttribute("bestSellers", bestSellers);
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        OrderResponse result = orderService.placeOrder(orderRequest);
        return "redirect:/cart";
    }

    @GetMapping("/admin/toggle-paid/{id}")
    public String togglePaidOrder(RedirectAttributes redirectAttributes, @PathVariable Long id, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "Staff") String type) {
        Order order = orderService.getOrderById(id);

        if (order.getStatus() != 2) {
            redirectAttributes.addFlashAttribute("error", "Đon hàng ở trạng thái 'Hoàn thành' mới có thể thay đổi trạng thái thanh toán.");
            return "redirect:/admin-" + type.toLowerCase() + "/orders?page=" + page + "&type=" + type;
        }

        order.setPaid(!order.isPaid());

        orderRepository.save(order);

        String status = order.isPaid() ? " đã thanh toán." : " chưa thanh toán.";

        String message = "[Cập nhật trạng thái thanh toán]\nĐơn hàng số: " + id + " đã cập nhật trạng thái thành" + status;
        telegramService.sendMessageToGroup(message);

        redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thanh toán thành công!");
        return "redirect:/admin-" + type.toLowerCase() + "/orders?page=" + page + "&type=" + type;
    }

    @GetMapping("/admin/toggle-order/{id}")
    public String toggleOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "Staff") String type) {
        int oldOrderStatus = orderService.getOrderById(id).getStatus();
        if (oldOrderStatus == 2) return "redirect:/admin-" + type.toLowerCase() + "/orders?page=" + page + "&type=" + type;

        Order order = orderService.toggleStatus(id);
        int newOrderStatus = order.getStatus();

        String orderStatus = "";

        if (order.getStatus() == 0) {
            orderStatus = "Đơn mới";
        } else if (order.getStatus() == 2) {
            orderStatus = "Hoàn thành";
        } else if (order.getStatus() == 3) {
            orderStatus = "Đã hủy";
        }

        if (oldOrderStatus != newOrderStatus) {
            String message = "[Cập nhật đơn hàng]\nĐơn hàng số: " + id + " đã cập nhật trạng thái thành " + orderStatus;
            telegramService.sendMessageToGroup(message);
        }

        var commissionHistory = commissionHistoryRepository.findCommissionHistoryByOrderId(id);

        if (commissionHistory.isPresent()) {
            var agencyF1 = agencyRepository.findByUsername(commissionHistory.get().getReceiveF1());
            var agencyF2 = agencyRepository.findByUsername(commissionHistory.get().getReceiveF2());

            if (order.getStatus() == 2) {
                commissionHistory.get().setStatus(1);
            } else if (order.getStatus() == 3) {
                commissionHistory.get().setStatus(2);
            } else {
                commissionHistory.get().setStatus(0);
            }

            commissionHistoryRepository.save(commissionHistory.get());

            if (oldOrderStatus != 2 && newOrderStatus == 2) {
                if (commissionHistory.get().getAmount() > 0) {
                    agencyF1.ifPresent(agency -> {
                        agency.setTotal(agency.getTotal() + commissionHistory.get().getAmount());
                        agencyRepository.save(agency);
                    });
                    agencyF2.ifPresent(agency -> {
                        agency.setTotal(agency.getTotal() + commissionHistory.get().getAmount());
                        agencyRepository.save(agency);
                    });
                }
            } else if (oldOrderStatus == 2 && newOrderStatus != 2) {
                if (commissionHistory.get().getAmount() > 0) {
                    agencyF1.ifPresent(agency -> {
                        agency.setTotal(agency.getTotal() - commissionHistory.get().getAmount());
                        agencyRepository.save(agency);
                    });
                    agencyF2.ifPresent(agency -> {
                        agency.setTotal(agency.getTotal() - commissionHistory.get().getAmount());
                        agencyRepository.save(agency);
                    });
                }
            }
        }

        // Redirect về trang hiện tại
        return "redirect:/admin-" + type.toLowerCase() + "/orders?page=" + page + "&type=" + type;
    }

    @GetMapping("/admin/cancel-order/{id}")
    public String cencelOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "0") int page, @RequestParam(required = false, defaultValue = "Staff") String type) {
        Order order = orderService.findOrderById(id).get();
        if (order.getStatus() < 3) {
            orderService.cancelOrder(id);
            String message = "[Cập nhật đơn hàng]\nĐơn hàng số: " + id + " đã cập nhật trạng thái thành Đã hủy";
            telegramService.sendMessageToGroup(message);

            var commissionHistory = commissionHistoryRepository.findCommissionHistoryByOrderId(id);
            if (commissionHistory.isPresent()) {
                var agencyF1 = agencyRepository.findByUsername(commissionHistory.get().getReceiveF1());
                var agencyF2 = agencyRepository.findByUsername(commissionHistory.get().getReceiveF2());

                commissionHistory.get().setStatus(2);
                commissionHistoryRepository.save(commissionHistory.get());

                if (commissionHistory.get().getAmount() > 0) {
                    agencyF1.ifPresent(agency -> {
                        agency.setTotal(agency.getTotal() - commissionHistory.get().getAmount());
                        agencyRepository.save(agency);
                    });
                    agencyF2.ifPresent(agency -> {
                        agency.setTotal(agency.getTotal() - commissionHistory.get().getAmount());
                        agencyRepository.save(agency);
                    });
                }
            }
        }
        return "redirect:/admin-" + type.toLowerCase() + "/orders?page=" + page + "&type=" + type;

    }

    @GetMapping("/admin-staff/orders")
    public String ordersStaff(Model model,
                         @RequestParam(name = "page", defaultValue = "0") int page) {

        int pageSize = 10;
        Page<Order> orders = orderService.getPaginatedOrders("Staff", PageRequest.of(page, pageSize));

        model.addAttribute("orderPage", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("type", "Staff"); // Thêm fetchType vào model

        return "admin/orders";
    }

    @GetMapping("/admin-customer/orders")
    public String ordersCustomer(Model model,
                         @RequestParam(name = "page", defaultValue = "0") int page) {

        int pageSize = 10;
        Page<Order> orders = orderService.getPaginatedOrders("Customer", PageRequest.of(page, pageSize));

        model.addAttribute("orderPage", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("type", "Customer"); // Thêm fetchType vào model

        return "admin/orders-customer";
    }

    @GetMapping("/admin/toglge-paid-status/{orderId}")
    public String togglePaidOrder(@PathVariable int orderId, @RequestParam(required = false, defaultValue = "0") int page) {
        System.out.println("Orderid: " + orderId + " page: " + page);
        return "redirect:/admin/orders?page=" + page;
    }

    @GetMapping("/admin-staff/cates")
    public String catesStaff(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Category> cates = cateService.getAllCatesPageable("Staff", PageRequest.of(page, pageSize));
        model.addAttribute("catePage", cates);

        model.addAttribute("cate", new Category());

        return "admin/cates";
    }

    @GetMapping("/admin-customer/cates")
    public String catesCustomer(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Category> cates = cateService.getAllCatesPageable("Customer", PageRequest.of(page, pageSize));
        model.addAttribute("catePage", cates);

        model.addAttribute("cate", new Category());

        return "admin/cates-customer";
    }

    @GetMapping("/admin-staff/users")
    public String users(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Agency> agency = agencyRepository.findAllByType("Staff", PageRequest.of(page, pageSize));
        model.addAttribute("usersPage", agency);

        model.addAttribute("user", new Agency());

        return "admin/users";
    }

    @GetMapping("/admin-customer/users")
    public String usersCustomer(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Agency> agency = agencyRepository.findAllByType("Customer", PageRequest.of(page, pageSize));
        model.addAttribute("usersPage", agency);

        model.addAttribute("user", new Agency());

        return "admin/users-customer";
    }

    @PostMapping("/admin/add-agency-staff")
    public String addAgency(AddAgencyRequest request) {
        var agencyExisted = agencyRepository.findByUsername(request.getUsername());
        if (agencyExisted.isPresent()) {
            return "redirect:/admin-staff/users"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
        }
        Agency agency = new Agency();
        agency.setEmail(request.getEmail());
        agency.setFullname(request.getFullname());
        agency.setPassword(request.getPassword());
        agency.setPhone(request.getPhone());
        agency.setUsername(request.getUsername());
        agency.setRole("Staff");
        agencyRepository.save(agency);
        return "redirect:/admin-staff/users"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @PostMapping("/admin/add-agency-customer")
    public String addAgencyCustomer(AddAgencyRequest request) {
        var agencyExisted = agencyRepository.findByUsername(request.getUsername());
        if (agencyExisted.isPresent()) {
            return "redirect:/admin-customer/users"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
        }
        Agency agency = new Agency();
        agency.setEmail(request.getEmail());
        agency.setFullname(request.getFullname());
        agency.setPassword(request.getPassword());
        agency.setPhone(request.getPhone());
        agency.setUsername(request.getUsername());
        agency.setRole("Customer");
        agencyRepository.save(agency);
        return "redirect:/admin-customer/users"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @GetMapping("/admin-staff/commissions")
    public String commissions(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<CommissionHistory> commissionHistory = commissionHistoryRepository.findAll(
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("commissionPage", commissionHistory);

        return "admin/commission";
    }

    @GetMapping("/admin-customer/commissions")
    public String commissionsCustomer(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<CommissionHistory> commissionHistory = commissionHistoryRepository.findAll(
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("commissionPage", commissionHistory);

        return "admin/commission-customer";
    }


    @PostMapping("/admin/add-cate-staff")
    public String addCateStaff(AddCateRequest request) {
        cateService.saveCate("Staff", request);

        return "redirect:/admin-staff/cates"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @PostMapping("/admin/add-cate-customer")
    public String addCate(AddCateRequest request) {
        cateService.saveCate("Customer", request);

        return "redirect:/admin-customer/cates"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @PostMapping("/admin/edit-cate-staff")
    public String updateCate(@RequestParam("id") int id, @RequestParam("cateName") String cateName,
                             @RequestParam("status") int status, RedirectAttributes redirectAttributes) {
        UpdateCateRequest request = new UpdateCateRequest();
        request.setId(id);
        request.setCateName(cateName);
        request.setStatus(status);
        cateService.updateCate(request);
        return "redirect:/admin-staff/cates"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @GetMapping("/admin/toggle-cate-staff/{id}")
    public String toggleCate(@PathVariable int id) {
        cateService.toggleCateStatus(id);
        return "redirect:/admin-staff/cates";
    }

    @Autowired
    private StocksService service;

    @Autowired
    ExcelExportService excelExportService;

    @GetMapping("/admin/export")
    public void exportReport(HttpServletResponse response,
                             @RequestParam long startDate,
                             @RequestParam long endDate,
                             @RequestParam String type) throws IOException {
        stockReportService.exportStockReport(type, response, startDate/1000, endDate/1000);
    }

    @GetMapping("/admin-staff/foods")
    public String foodsStaff(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Food> foodPage = foodService.getPaginatedFoods("Staff", PageRequest.of(page, pageSize));
        model.addAttribute("foodPage", foodPage);

        model.addAttribute("newFood", new Food());

        List<Category> cates = cateService.getAllCatesByType("Staff");
        model.addAttribute("cates", cates);
        return "admin/foods";
    }

    @GetMapping("/admin-customer/foods")
    public String foodsCustomer(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Food> foodPage = foodService.getPaginatedFoods("Customer", PageRequest.of(page, pageSize));
        model.addAttribute("foodPage", foodPage);

        model.addAttribute("newFood", new Food());

        List<Category> cates = cateService.getAllCatesByType("Customer");
        model.addAttribute("cates", cates);
        return "admin/foods-customer";
    }

    @GetMapping("/admin-staff/stocks")
    public String stocks(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<StocksHistory> stocksHistoryPage = stocksService.getAllStocksHistory("Staff", PageRequest.of(page, pageSize));
        model.addAttribute("stocksPage", stocksHistoryPage);

        List<Food> listFood = foodService.getAllByType("Staff");
        AddStocksRequest request = new AddStocksRequest();
        model.addAttribute("stockRequest", request);
        model.addAttribute("listFood", listFood);
        model.addAttribute("editStock", new EditStocksRequest());
        model.addAttribute("type", "Staff");

        List<String> listType = new ArrayList<>();
        listType.add("In");
        listType.add("Out");
        model.addAttribute("listType", listType);

        return "admin/stocks";
    }

    @GetMapping("/admin-customer/stocks")
    public String stocksCustomer(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<StocksHistory> stocksHistoryPage = stocksService.getAllStocksHistory("Customer", PageRequest.of(page, pageSize));
        model.addAttribute("stocksPage", stocksHistoryPage);

        List<Food> listFood = foodService.getAllByType("Customer");
        AddStocksRequest request = new AddStocksRequest();
        model.addAttribute("stockRequest", request);
        model.addAttribute("listFood", listFood);
        model.addAttribute("editStock", new EditStocksRequest());
        model.addAttribute("type", "Customer");

        List<String> listType = new ArrayList<>();
        listType.add("In");
        listType.add("Out");
        model.addAttribute("listType", listType);

        return "admin/stocks-customer";
    }


    @PostMapping("/admin/add-stocks-staff")
    public String addStocks(@ModelAttribute AddStocksRequest request, RedirectAttributes redirectAttributes) {
        String msg = "";

        // Ánh xạ loại kho với thông báo tương ứng
        Map<String, String> typeMessages = new HashMap<>();
        typeMessages.put("in", "Nhập");
        typeMessages.put("out", "Xuất");
        typeMessages.put("adjustment", "Điều chỉnh");

        // Kiểm tra loại kho và xử lý thông báo
        String action = typeMessages.get(request.getType().toLowerCase());

        if (action != null) {
            if ("adjustment".equalsIgnoreCase(request.getType())) {
                // Với loại adjustment, chỉ kiểm tra giá
                if (request.getPrice() >= 0) {
                    stocksService.updateStocks(request.getId(), request.getQuantity(), request.getPrice(), request.getType(), "Staff");
                    msg = action + " kho " + request.getType() + " thành công";
                } else {
                    msg = action + " kho " + request.getType() + " thất bại, giá " + request.getPrice();
                }
            } else if ("in".equalsIgnoreCase(request.getType()) || "out".equalsIgnoreCase(request.getType())) {
                // Với loại in hoặc out, kiểm tra cả số lượng và giá
                if (request.getQuantity() > 0 && request.getPrice() >= 0) {
                    stocksService.updateStocks(request.getId(), request.getQuantity(), request.getPrice(), request.getType(), "Staff");
                    msg = action + " kho " + request.getType() + " thành công";
                } else {
                    msg = action + " kho " + request.getType() + " thất bại, "
                            + (request.getQuantity() <= 0 ? "số lượng" : "giá") + " "
                            + (request.getQuantity() <= 0 ? request.getQuantity() : request.getPrice());
                }
            }
            redirectAttributes.addFlashAttribute("msg", msg); // Gửi thông báo
        }

        return "redirect:/admin-staff/stocks?type=" + request.getUserRole();
    }

    @PostMapping("/admin/add-stocks-customer")
    public String addStocksCustomer(@ModelAttribute AddStocksRequest request, RedirectAttributes redirectAttributes) {
        String msg = "";

        // Ánh xạ loại kho với thông báo tương ứng
        Map<String, String> typeMessages = new HashMap<>();
        typeMessages.put("in", "Nhập");
        typeMessages.put("out", "Xuất");
        typeMessages.put("adjustment", "Điều chỉnh");

        // Kiểm tra loại kho và xử lý thông báo
        String action = typeMessages.get(request.getType().toLowerCase());

        if (action != null) {
            if ("adjustment".equalsIgnoreCase(request.getType())) {
                // Với loại adjustment, chỉ kiểm tra giá
                if (request.getPrice() >= 0) {
                    stocksService.updateStocks(request.getId(), request.getQuantity(), request.getPrice(), request.getType(), "Customer");
                    msg = action + " kho " + request.getType() + " thành công";
                } else {
                    msg = action + " kho " + request.getType() + " thất bại, giá " + request.getPrice();
                }
            } else if ("in".equalsIgnoreCase(request.getType()) || "out".equalsIgnoreCase(request.getType())) {
                // Với loại in hoặc out, kiểm tra cả số lượng và giá
                if (request.getQuantity() > 0 && request.getPrice() >= 0) {
                    stocksService.updateStocks(request.getId(), request.getQuantity(), request.getPrice(), request.getType(), "Customer");
                    msg = action + " kho " + request.getType() + " thành công";
                } else {
                    msg = action + " kho " + request.getType() + " thất bại, "
                            + (request.getQuantity() <= 0 ? "số lượng" : "giá") + " "
                            + (request.getQuantity() <= 0 ? request.getQuantity() : request.getPrice());
                }
            }
            redirectAttributes.addFlashAttribute("msg", msg); // Gửi thông báo
        }

        return "redirect:/admin-customer/stocks?type=" + request.getUserRole();
    }

    @PostMapping("/admin/edit-stocks-staff")
    public String editStocks(@ModelAttribute EditStocksRequest request, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        stocksService.editStocks(request.getNewId(), request.getNewQuantity(), request.getNewPrice(), request.getNewType());
        redirectAttributes.addFlashAttribute("msgSuccess", "Điều chỉnh tồn kho thành công");

        return "redirect:/admin-staff/stocks";
    }

    @PostMapping("/admin/edit-stocks-customer")
    public String editStocksCustomer(@ModelAttribute EditStocksRequest request, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        stocksService.editStocks(request.getNewId(), request.getNewQuantity(), request.getNewPrice(), request.getNewType());
        redirectAttributes.addFlashAttribute("msgSuccess", "Điều chỉnh tồn kho thành công");

        return "redirect:/admin-customer/stocks";
    }

    @PostMapping("/admin/check-stocks-staff")
    public String checkStocks(@ModelAttribute CheckStocksRequest request, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        stocksService.checkStocks("Staff", request.getFoodName(), request.getRealQuantity());
        redirectAttributes.addFlashAttribute("msgSuccess", "Kiểm tra tồn kho thành công");

        return "redirect:/admin-staff/stocks";
    }

    @PostMapping("/admin/check-stocks-customer")
    public String checkStocksCustomer(@ModelAttribute CheckStocksRequest request, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        stocksService.checkStocks("Customer", request.getFoodName(), request.getRealQuantity());
        redirectAttributes.addFlashAttribute("msgSuccess", "Kiểm tra tồn kho thành công");

        return "redirect:/admin-customer/stocks";
    }

    @PostMapping("/admin/add-food-staff")
    public String addFood(@ModelAttribute AddFoodRequest foodRequest, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        foodService.addNewFood(foodRequest, "Staff");
        redirectAttributes.addFlashAttribute("msgSuccess", "Thêm món ăn thành công");

        return "redirect:/admin-staff/foods";
    }

    @PostMapping("/admin/add-food-customer")
    public String addFoodCustomer(@ModelAttribute AddFoodRequest foodRequest, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        foodService.addNewFood(foodRequest, "Customer");
        redirectAttributes.addFlashAttribute("msgSuccess", "Thêm món ăn thành công");

        return "redirect:/admin-customer/foods";
    }

    @GetMapping("/admin/toggle-food/{id}")
    public String toggleFood(@PathVariable Long id) {
        foodService.toggleFoodStatus(id);
        return "redirect:/admin/foods";
    }

    @PostMapping("/admin/edit-food-staff")
    public String updateFood(@RequestParam("id") Long id, @RequestParam("name") String name,
                             @RequestParam("description") String description, @RequestParam("price") double price,
                             @RequestParam(value = "categories", required = false) List<Integer> categories,
                             @RequestParam("image") MultipartFile image, @RequestParam("status") int status,
                             @RequestParam("quantity") String quantity,
                             RedirectAttributes redirectAttributes) {

        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setId(id);
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategories(categories);
        request.setImage(image);
        request.setQuantity(quantity);
        request.setStatus(status);

        foodService.updateFood(request);
        redirectAttributes.addFlashAttribute("message", "Cập nhật món ăn thành công.");
        return "redirect:/admin-staff/foods";
    }

    @PostMapping("/admin/edit-food-customer")
    public String updateFoodCustomer(@RequestParam("id") Long id, @RequestParam("name") String name,
                             @RequestParam("description") String description, @RequestParam("price") double price,
                             @RequestParam(value = "categories", required = false) List<Integer> categories,
                             @RequestParam("image") MultipartFile image, @RequestParam("status") int status,
                             @RequestParam("quantity") String quantity,
                             RedirectAttributes redirectAttributes) {

        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setId(id);
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategories(categories);
        request.setImage(image);
        request.setQuantity(quantity);
        request.setStatus(status);

        foodService.updateFood(request);
        redirectAttributes.addFlashAttribute("message", "Cập nhật món ăn thành công.");
        return "redirect:/admin-customer/foods";
    }

    @GetMapping("/admin-staff/coupons")
    public String couponsStaff(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Coupon> coupons = coupService.getCouponPaginate(PageRequest.of(page, pageSize));
        model.addAttribute("couponPage", coupons);

        model.addAttribute("coupon", new Coupon());
        return "admin/coupon";
    }

    @GetMapping("/admin-customer/coupons")
    public String couponsCustomer(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Coupon> coupons = coupService.getCouponPaginate(PageRequest.of(page, pageSize));
        model.addAttribute("couponPage", coupons);

        model.addAttribute("coupon", new Coupon());
        return "admin/coupon-customer";
    }

    @PostMapping("/admin/add-coupon")
    public String addCoupon(@ModelAttribute CouponRequest request) {
        coupService.addCoupon(request);
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/toggle-coupon/{id}")
    public String toggleCoupon(@PathVariable Long id) {
        coupService.toggleStatus(id);
        return "redirect:/admin/coupons";
    }

    @PostMapping("/admin/edit-coupon")
    public String updateCoupon(@RequestParam("id") Long id, @RequestParam("code") String code,
                               @RequestParam("rate") double rate, @RequestParam("count") int count, RedirectAttributes redirectAttributes) {
        UpdateCouponRequest request = new UpdateCouponRequest();
        request.setId(id);
        request.setCode(code);
        request.setRate(rate);
        request.setCount(count);
        coupService.updateCoupon(request);
        redirectAttributes.addFlashAttribute("message", "Cập nhật coupon thành công.");
        return "redirect:/admin/coupons"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }
}

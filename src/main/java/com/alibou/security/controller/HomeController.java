package com.alibou.security.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.alibou.security.dto.*;
import com.alibou.security.entity.*;
import com.alibou.security.repository.AgencyRepository;
import com.alibou.security.repository.CommissionHistoryRepository;
import com.alibou.security.repository.CustomerRepository;
import com.alibou.security.service.*;
import com.alibou.security.user.UserRepository;
import com.alibou.security.utils.ExcelExportService;
import com.alibou.security.utils.HtmlToPDF;
import com.alibou.security.utils.TelegramService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping("/login")
    public String login() {
        return "landing-page/login";
    }

    @GetMapping("/sales")
    public String staff(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập tài khoản để theo dõi doanh thu cá nhân!");
            return "redirect:/login";
        }

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
            item.setActual(order.getActual());
            item.setStatus(order.getStatus());
            listOrdersDetail.add(item);
        }

        System.out.println(listOrdersDetail);
        model.addAttribute("listOrders", listOrdersDetail);

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
            if (agency.isPresent() && agency.get().getRole().equalsIgnoreCase("admin")) {
                return "redirect:/admin";
            } else {
                return "redirect:/";
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

        int pageSize = 6;
        Page<Food> foods = foodService.getPaginatedFoodsShow(PageRequest.of(page, pageSize));
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

    @GetMapping({"/admin/dashboard", "/admin/", "/admin", "/admin/dashboard/{date}"})
    public String dashboard(Model model, @PathVariable(name = "date", required = false) String date, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thao tác đặt hàng!");
            return "redirect:/login";
        }

        var agencyOptional = agencyRepository.findByUsername(loggedInUser);

        if (agencyOptional.isPresent() && !agencyOptional.get().getRole().equalsIgnoreCase("admin")) {
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


        return "admin/index";
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

    @GetMapping("/admin/toggle-order/{id}")
    public String toggleOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "0") int page) {
        int oldOrderStatus = orderService.getOrderById(id).getStatus();

        Order order = orderService.toggleStatus(id);
        int newOrderStatus = order.getStatus();

        String orderStatus = "";

        if (order.getStatus() == 0) {
            orderStatus = "Đơn mới";
        } else if (order.getStatus() == 1) {
            orderStatus = "Đang giao";
        } else if (order.getStatus() == 2) {
            orderStatus = "Hoàn thành";
        } else if (order.getStatus() == 3) {
            orderStatus = "Đã hủy";
        }

        String message = "[Cập nhật đơn hàng]\nĐơn hàng số: " + id + " đã cập nhật trạng thái thành " + orderStatus;
        telegramService.sendMessageToGroup(message);

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
        return "redirect:/admin/orders?page=" + page;
    }

    @GetMapping("/admin/cancel-order/{id}")
    public String cencelOrder(@PathVariable Long id, @RequestParam(required = false, defaultValue = "0") int page) {
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
        return "redirect:/admin/orders?page=" + page;

    }

    @GetMapping("/admin/orders")
    public String orders(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 10;
        Page<Order> orders = orderService.getPaginatedOrders(PageRequest.of(page, pageSize));
        model.addAttribute("orderPage", orders);
        model.addAttribute("currentPage", page); // Truyền page hiện tại vào Model

        return "admin/orders";
    }

    @GetMapping("/admin/cates")
    public String cates(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Category> cates = cateService.getAllCatesPageable(PageRequest.of(page, pageSize));
        model.addAttribute("catePage", cates);

        model.addAttribute("cate", new Category());

        return "admin/cates";
    }

    @GetMapping("/admin/users")
    public String users(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Agency> agency = agencyRepository.findAll(PageRequest.of(page, pageSize));
        model.addAttribute("usersPage", agency);

        model.addAttribute("user", new Agency());

        return "admin/users";
    }

    @PostMapping("/admin/add-agency")
    public String addAgency(AddAgencyRequest request) {
        var agencyExisted = agencyRepository.findByUsername(request.getUsername());
        if (agencyExisted.isPresent()) {
            return "redirect:/admin/users"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
        }
        Agency agency = new Agency();
        agency.setEmail(request.getEmail());
        agency.setPassword(request.getPassword());
        agency.setPhone(request.getPhone());
        agency.setUsername(request.getUsername());
        agencyRepository.save(agency);
        return "redirect:/admin/users"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @GetMapping("/admin/commissions")
    public String commissions(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<CommissionHistory> commissionHistory = commissionHistoryRepository.findAll(
                PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id"))
        );
        model.addAttribute("commissionPage", commissionHistory);

        return "admin/commission";
    }


    @PostMapping("/admin/add-cate")
    public String addCate(AddCateRequest request) {
        cateService.saveCate(request);

        return "redirect:/admin/cates"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @PostMapping("/admin/edit-cate")
    public String updateCate(@RequestParam("id") int id, @RequestParam("cateName") String cateName,
                             @RequestParam("status") int status, RedirectAttributes redirectAttributes) {
        UpdateCateRequest request = new UpdateCateRequest();
        request.setId(id);
        request.setCateName(cateName);
        request.setStatus(status);
        cateService.updateCate(request);
        return "redirect:/admin/cates"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @GetMapping("/admin/toggle-cate/{id}")
    public String toggleCate(@PathVariable int id) {
        cateService.toggleCateStatus(id);
        return "redirect:/admin/cates";
    }

    @Autowired
    private StocksService service;

    @Autowired
    ExcelExportService excelExportService;

    @GetMapping("/admin/export")
    public void exportReport(HttpServletResponse response,
                             @RequestParam long startDate,
                             @RequestParam long endDate) throws IOException {
        List<StocksHistory> historyList = service.getHistoryByDateRange(startDate/1000, endDate/1000);
        excelExportService.exportToExcel(response, historyList, startDate/1000, endDate/1000);
    }

    @GetMapping("/admin/foods")
    public String foods(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Food> foodPage = foodService.getPaginatedFoods(PageRequest.of(page, pageSize));
        model.addAttribute("foodPage", foodPage);

        model.addAttribute("newFood", new Food());

        List<Category> cates = cateService.getAllCates();
        model.addAttribute("cates", cates);
        return "admin/foods";
    }

    @GetMapping("/admin/stocks")
    public String stocks(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<StocksHistory> stocksHistoryPage = stocksService.getAllStocksHistory(PageRequest.of(page, pageSize));
        model.addAttribute("stocksPage", stocksHistoryPage);

        List<Food> listFood = foodService.getAll();
        AddStocksRequest request = new AddStocksRequest();
        model.addAttribute("stockRequest", request);
        model.addAttribute("listFood", listFood);
        model.addAttribute("editStock", new EditStocksRequest());


        List<String> listType = new ArrayList<>();
        listType.add("In");
        listType.add("Out");
        model.addAttribute("listType", listType);

        return "admin/stocks";
    }

    @PostMapping("/admin/add-stocks")
    public String addStocks(@ModelAttribute AddStocksRequest request, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        if (request.getQuantity() > 0) {
            stocksService.updateStocks(request.getId(), request.getQuantity(), request.getPrice(), request.getType());
            redirectAttributes.addFlashAttribute("msgSuccess", "Thêm tồn kho thành công");
        }

        return "redirect:/admin/stocks";
    }

    @PostMapping("/admin/edit-stocks")
    public String editStocks(@ModelAttribute EditStocksRequest request, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        stocksService.editStocks(request.getNewId(), request.getNewQuantity(), request.getNewPrice(), request.getNewType());
        redirectAttributes.addFlashAttribute("msgSuccess", "Điều chỉnh tồn kho thành công");

        return "redirect:/admin/stocks";
    }

    @PostMapping("/admin/add-food")
    public String addFood(@ModelAttribute AddFoodRequest foodRequest, RedirectAttributes redirectAttributes) {
        // Tiếp tục xử lý và lưu thông tin thực phẩm
        foodService.addNewFood(foodRequest);
        redirectAttributes.addFlashAttribute("msgSuccess", "Thêm món ăn thành công");

        return "redirect:/admin/foods";
    }

    @GetMapping("/admin/toggle-food/{id}")
    public String toggleFood(@PathVariable Long id) {
        foodService.toggleFoodStatus(id);
        return "redirect:/admin/foods";
    }

    @PostMapping("/admin/edit-food")
    public String updateFood(@RequestParam("id") Long id, @RequestParam("name") String name,
                             @RequestParam("description") String description, @RequestParam("price") double price,
                             @RequestParam(value = "categories", required = false) List<Integer> categories,
                             @RequestParam("image") MultipartFile image, @RequestParam("status") int status,
                             RedirectAttributes redirectAttributes) {
        UpdateFoodRequest request = new UpdateFoodRequest();
        request.setId(id);
        request.setName(name);
        request.setDescription(description);
        request.setPrice(price);
        request.setCategories(categories);
        request.setImage(image);
        request.setStatus(status);

        foodService.updateFood(request);
        redirectAttributes.addFlashAttribute("message", "Cập nhật món ăn thành công.");
        return "redirect:/admin/foods"; // Chuyển hướng đến trang danh sách món ăn sau khi cập nhật
    }

    @GetMapping("/admin/coupons")
    public String coupons(Model model, @RequestParam(name = "page", defaultValue = "0") int page) {
        int pageSize = 4;
        Page<Coupon> coupons = coupService.getCouponPaginate(PageRequest.of(page, pageSize));
        model.addAttribute("couponPage", coupons);

        model.addAttribute("coupon", new Coupon());
        return "admin/coupon";
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

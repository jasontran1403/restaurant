package com.alibou.security.service.serviceimpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import com.alibou.security.dto.*;
import com.alibou.security.entity.*;
import com.alibou.security.repository.*;
import com.alibou.security.service.StocksService;
import com.alibou.security.utils.TelegramService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibou.security.exception.NotFoundException;
import com.alibou.security.service.OrderService;
import com.alibou.security.service.RealtimeService;

@Service
public class OrderServiceImpl implements OrderService {
	@Autowired
	OrderRepository orderRepo;

	@Autowired
	OrderDetailRepository orderDetailRepo;

	@Autowired
	FoodRepository foodRepo;

	@Autowired
	CouponRepository coupRepo;

	@Autowired
	RealtimeService realtimeService;

	@Autowired
	AgencyRepository agencyRepo;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CommissionHistoryRepository commissionHistoryRepository;

	@Autowired
	TelegramService telegramService;

	@Autowired
	StocksService stocksService;

	@Override
	public OrderResponse placeOrder(OrderRequest request) {
		// TODO Auto-generated method stub
		var agency = agencyRepo.findByUsername(request.getAgency());

		if (agency.isEmpty()) {
			OrderResponse response = new OrderResponse();
			response.setMessage1("Agency không tồn tại!");
			return response;
		}

		StringBuilder sb = new StringBuilder();

		double total = 0;
		List<OrderDetail> orderDetails = new ArrayList<>();
		List<Food> foods = new ArrayList<>();
		String messageNotAvailable = "";
		StringBuilder messagePriceDiff = new StringBuilder();

		double code = 0;
		double amountBefore = 0;
		List<FoodsDTO> foodsDTOs = new ArrayList<>();
		for (String phrase : request.getFood()) {
			String[] item = phrase.split("-");
			long foodId = Long.parseLong(item[0]);
			int quantity = Integer.parseInt(item[1]);
			long price = Long.parseLong(item[2]);
			code += price * quantity;
			Optional<Food> food = foodRepo.getFoodById(foodId);
			if (food.isEmpty()) {
				throw new NotFoundException("Món ăn không tồn tại!");
			} else if (food.get().getStatus() != 0) {
				sb.append(food.get().getName()).append(" + ");
			} else {
				if (price != food.get().getPrice()) {
					messagePriceDiff.append(food.get().getId()).append("-").append(food.get().getName()).append("---");
				} else {
					FoodsDTO itemFoodsDTO = new FoodsDTO();
					itemFoodsDTO.setQuantity(quantity);
					itemFoodsDTO.setId(food.get().getId());
					itemFoodsDTO.setName(food.get().getName());
					itemFoodsDTO.setFoodCate(food.get().getCategories().get(0));
					foodsDTOs.add(itemFoodsDTO);
					OrderDetail orderDetail = new OrderDetail();
					orderDetail.setFood_id(foodId);
					orderDetail.setQuantity(quantity);
					orderDetail.setName(food.get().getName());
					Optional<Coupon> coupon = coupRepo.getByCode(request.getCode());

					if (coupon.isPresent() && coupon.get().getStatus() == 0 && coupon.get().getCount() >= 1) {
						if (coupon.get().getCode().equalsIgnoreCase("LPMARKETING")) {
							total += food.get().getDefaultPrice() * quantity;
							coupon.get().setCount(coupon.get().getCount() - 1);
							coupRepo.save(coupon.get());

							orderDetail.setPrice(food.get().getDefaultPrice());
						} else {
							double rate = request.getRate();
							orderDetail.setRate(rate);
							total += (food.get().getPrice() - food.get().getPrice()*rate/100) * quantity;
							coupon.get().setCount(coupon.get().getCount() - 1);
							coupRepo.save(coupon.get());

							orderDetail.setPrice(food.get().getPrice() - food.get().getPrice()*rate/100);
						}

					} else {
						orderDetail.setRate(0);
						total += (food.get().getPrice()) * quantity;
						orderDetail.setPrice(food.get().getPrice());
					}


					orderDetails.add(orderDetail);
					
					foods.add(food.get());

					food.get().setStocks(food.get().getStocks() - quantity);
					foodRepo.save(food.get());
					stocksService.updateStocks((int)foodId, quantity, 0, "Out");
				}
			}
		}

		if (sb.toString().contains(" + ")) {
			messageNotAvailable = sb.substring(0, sb.toString().length() - 2) + "hết món!";
		}

		double amountAfter = 0;
		if (messageNotAvailable.isEmpty() && (messagePriceDiff.isEmpty())) {
			Order order = new Order();
			order.setStatus(0);
			order.setTime(System.currentTimeMillis() / 1000);
			order.setName(request.getName());
			order.setPhone(request.getPhone());
			order.setAddress(request.getAddress());
			if (request.getMessage().equalsIgnoreCase("")) {
				order.setMessage("Không có");
			} else {
				order.setMessage(request.getMessage());
			}

			order.setTotal(total);
			order.setActual(code);
			order.setStaff(agency.get().getUsername().toUpperCase());
			amountBefore = code;
			amountAfter = total;

			Order savedOrder = orderRepo.save(order);

			for (OrderDetail orderDetail : orderDetails) {
				orderDetail.setOrder_id(savedOrder.getId());
				orderDetail.setCode(request.getCode());
				orderDetail.setRate(request.getRate());
				orderDetailRepo.save(orderDetail);
			}

			String message = "[Đơn hàng mới]\nNgười nhận: " + request.getName() + "\nSĐT: " + request.getPhone() + "\nAgency: " + request.getAgency();
			telegramService.sendMessageToGroup(message);

			var customerExisted = customerRepository.findByUsername(request.getPhone());

			int countIndexType1 = 0;
			int countIndexType2 = 0;

			for (FoodsDTO food : foodsDTOs) {
				if (food.getFoodCate().equalsIgnoreCase("Sausage")) {
					countIndexType1 += food.getQuantity();
				}
				if (food.getFoodCate().equalsIgnoreCase("Saurce") && food.getId() == 6) {
					countIndexType2 += food.getQuantity();
				}
			}

			double amount = countIndexType1 * 30_000 + countIndexType2 * 5_000;

			double amountCommission = 0;

			if (amountBefore == amountAfter) {
				// Không sử dụng mã giảm giá
				amountCommission = amount;
			} else {
				// Sử dụng mã giảm giá, tính phần chênh lệch
				double discountAmount = amountBefore - amountAfter; // Phần giá giảm

				if (amount > discountAmount) {
					// Hoa hồng lớn hơn phần giá giảm, hoa hồng = Số tiền hoa hồng thực tế - số tiền giảm
					amountCommission = amount - discountAmount;
				} else {
					// Hoa hồng nhỏ hơn phần giá giảm, không nhận được hoa hồng
					amountCommission = 0;
				}
			}

			if (customerExisted.isEmpty()) {
				Customer customer = new Customer();
				customer.setPhone(request.getPhone());
				customer.setF1(agency.get().getUsername());

				Customer resultSave = customerRepository.save(customer);

				CommissionHistory commissionHistory = new CommissionHistory();
				commissionHistory.setFromOrder(savedOrder.getId());
				commissionHistory.setFromCustomer(resultSave.getPhone());
				commissionHistory.setReceiveF1(agency.get().getUsername());
				commissionHistory.setStatus(0);
				commissionHistory.setDate(System.currentTimeMillis()/1000);
				double actualAmountCommission = amountCommission * 1;
				commissionHistory.setAmount(actualAmountCommission);
				commissionHistoryRepository.save(commissionHistory);
			} else {
				double actualAmountCommission = 0;
				if (customerExisted.get().getF1().equalsIgnoreCase(request.getAgency())) {
					actualAmountCommission = amountCommission;
					CommissionHistory commissionHistory = new CommissionHistory();
					commissionHistory.setFromOrder(savedOrder.getId());
					commissionHistory.setFromCustomer(customerExisted.get().getPhone());
					commissionHistory.setReceiveF1(agency.get().getUsername());
					commissionHistory.setStatus(0);
					commissionHistory.setDate(System.currentTimeMillis()/1000);
					commissionHistory.setAmount(actualAmountCommission);
					commissionHistoryRepository.save(commissionHistory);
				} else {
					actualAmountCommission = amountCommission / 2;
					CommissionHistory commissionHistory = new CommissionHistory();
					commissionHistory.setFromOrder(savedOrder.getId());
					commissionHistory.setFromCustomer(customerExisted.get().getPhone());
					commissionHistory.setReceiveF1(customerExisted.get().getF1());
					commissionHistory.setReceiveF2(request.getAgency());
					commissionHistory.setStatus(0);
					commissionHistory.setDate(System.currentTimeMillis()/1000);
					commissionHistory.setAmount(actualAmountCommission);
					commissionHistoryRepository.save(commissionHistory);
				}
			}
		}


		OrderResponse response = new OrderResponse();
		response.setMessage1(messageNotAvailable);
		response.setMessage2(messagePriceDiff.toString());

		return response;
	}

	@Override
	@Transactional
	public Order toggleStatus(long orderId) {
		Order order = orderRepo.getById(orderId);
		int status = order.getStatus();
		int statusNext = status + 1;
		if (statusNext > 2) {
			statusNext = 2;
		}

		order.setStatus(statusNext);
		if (status != 2 && statusNext == 2) {
			List<OrderDetail> orderDetail = orderDetailRepo.getOrderDetailsById(orderId);

			List<Food> listFoodUpdate = new ArrayList<>();
			for (OrderDetail item : orderDetail) {
				Food food = foodRepo.findFoodById(item.getFood_id());
				food.setStocks(food.getStocks() - item.getQuantity());

				listFoodUpdate.add(food);
			}

			if (!listFoodUpdate.isEmpty()) {
				foodRepo.saveAll(listFoodUpdate);
			}
		}
		return orderRepo.save(order);
	}

	@Override
	public List<Order> getAllOrders() {
		// TODO Auto-generated method stub
		return orderRepo.findAll();
	}

	@Override
	public List<Order> findOrderByStaff(String staffId) {
		return orderRepo.findOrdersByStaff(staffId);
	}

	@Override
	public Order toggleOrderStatus(ToggleOrderRequest request) {
		// TODO Auto-generated method stub
		Order order = orderRepo.getById(request.getId());
		order.setStatus(request.getStatus());
		order.setMessage(request.getMessage());
		return orderRepo.save(order);
	}

	@Override
	public List<Order> getAllOrdersByStatus(int status) {
		// TODO Auto-generated method stub
		return orderRepo.findOrderByStatus(status);
	}

	@Override
	public Order getOrderById(long id) {
		// TODO Auto-generated method stub
		return orderRepo.getById(id);
	}

	@Override
	public Optional<Order> findOrderById(long id) {
		return orderRepo.findById(id);
	}

	@Override
	public Page<Order> getPaginatedOrders(Pageable pageable) {
		// TODO Auto-generated method stub
		return orderRepo.findAllPagable(pageable);
	}

	@Override
	@Transactional
	public void cancelOrder(long orderId) {
		// TODO Auto-generated method stub
		Order order = orderRepo.getById(orderId);
		if (order.getStatus() == 2) {
			List<OrderDetail> orderDetail = orderDetailRepo.getOrderDetailsById(orderId);

			List<Food> listFoodUpdate = new ArrayList<>();
			for (OrderDetail item : orderDetail) {
				Food food = foodRepo.findFoodById(item.getFood_id());
				food.setStocks(food.getStocks() + item.getQuantity());

				listFoodUpdate.add(food);
			}

			if (!listFoodUpdate.isEmpty()) {
				foodRepo.saveAll(listFoodUpdate);
			}
		}

		order.setStatus(3);
		orderRepo.save(order);
	}

	@Override
	public List<Double> getTodayReport(long timeFrom, long timeTo) {
		// TODO Auto-generated method stub
		List<Order> orders = orderRepo.getOrderByTimeRange(timeFrom, timeTo+86400);
		List<Double> result = new ArrayList<>();
		double expectedSales = 0;
		double amountOfNewOrders = 0, amountOfDeliOrders = 0, amountOfCompletedOrders = 0, amountOfCanceledOrders = 0, totalSales = 0;
		double totalOrders = 0;

		for (Order order : orders) {
			if (order.getStatus() == 0) {
				amountOfNewOrders++;
				totalOrders += 1;
			}else if (order.getStatus() == 1) {
				amountOfDeliOrders++;
				totalOrders += 1;
			}else if (order.getStatus() == 2) {
				amountOfCompletedOrders++;
				totalOrders += 1;
				totalSales += order.getTotal();
			}else {
				amountOfCanceledOrders++;
			}

			expectedSales += order.getTotal();
		}

		result.add(totalSales);
		result.add(totalOrders);
		result.add(amountOfCompletedOrders);
		result.add(amountOfCanceledOrders);
		result.add(expectedSales);
		result.add(amountOfNewOrders);
		result.add(amountOfDeliOrders);
		return result;
	}

	@Override
	public double getHighestOrder(long timeFrom, long timeTo) {
		// TODO Auto-generated method stub
		List<Order> orders = orderRepo.getOrderByTimeRange(timeFrom, timeTo);
		double highestOrderValue = Double.MIN_VALUE;
		
		for (Order order : orders) {
			double totalAmount = 0;
			List<OrderDetail> orderDetails = orderDetailRepo.getOrderDetailsById(order.getId());
			for (OrderDetail orderDetail : orderDetails) {
				double actualPrice = orderDetail.getPrice() - orderDetail.getPrice() * orderDetail.getRate()/100;
				totalAmount += (actualPrice * orderDetail.getQuantity());
			}

			highestOrderValue = Math.max(highestOrderValue, totalAmount);
		}
		

		return highestOrderValue > 0 ? highestOrderValue : 0;
	}

	@Override
	public double getLowestOrder(long timeFrom, long timeTo) {
		// TODO Auto-generated method stub
		List<Order> orders = orderRepo.getOrderByTimeRange(timeFrom, timeTo);
		double lowestOrderAmount = Double.MAX_VALUE;
		for (Order order : orders) {
			double totalAmount = order.getTotal();
			lowestOrderAmount = Math.min(lowestOrderAmount, totalAmount);
		}

		return lowestOrderAmount == Double.MAX_VALUE ? 0 : lowestOrderAmount;
	}

	@Override
	public List<BestSellerDto> getBestSellerStat() {
		HashMap<Long, Integer> map = new HashMap<>();
		List<Order> orders = orderRepo.findAll();

		for (Order order : orders) {
			List<OrderDetail> orderDetails = orderDetailRepo.getOrderDetailsById(order.getId());
			for (OrderDetail orderDetail : orderDetails) {
				Food food = foodRepo.findFoodById(orderDetail.getFood_id());
				if (map.containsKey(food.getId())) {
					int currentQuantity = map.get(food.getId());
					map.put(food.getId(), currentQuantity + orderDetail.getQuantity());
				} else {
					map.put(food.getId(), orderDetail.getQuantity());
				}
			}
		}

		List<BestSellerDto> bestSellers = new ArrayList<>();

		for (Map.Entry<Long, Integer> entry : map.entrySet()) {
			Long key = entry.getKey();
			Integer value = entry.getValue();
			List<OrderDetail> orderDetails = orderDetailRepo.getOrderDetailsByFoodId(key);

			BestSellerDto bestSeller = new BestSellerDto();
			Food food = foodRepo.findFoodById(key);
			double subTotal = 0;
			for (OrderDetail item : orderDetails) {
				subTotal += item.getPrice() * item.getQuantity();
			}
			bestSeller.setName(food.getName());
			bestSeller.setPrice(food.getPrice());
			bestSeller.setAmountSales(value);
			bestSeller.setTotalSales(subTotal);

			bestSellers.add(bestSeller);
		}

		// Sắp xếp bestSellers theo amountSales giảm dần
		Collections.sort(bestSellers, (seller1, seller2) -> seller2.getAmountSales() - seller1.getAmountSales());

		// Giới hạn danh sách kết quả chỉ lấy tối đa 5 phần tử
		if (bestSellers.size() > 5) {
			bestSellers = bestSellers.subList(0, 5);
		}

		return bestSellers;

	}

	@Override
	public StatResponse getStatByTime(String time) {
		// TODO Auto-generated method stub
		Date currentDateTime = new Date();

		// Lấy ngày hiện tại
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.setTime(currentDateTime);

		// Đặt thời gian thành 00:00:01
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		// Lấy timestamp sau khi đặt thời gian
		long timestamp = calendar.getTimeInMillis() / 1000;

		StatResponse result = new StatResponse();

		if (time.equals("today")) {
			long timeTo = timestamp + 86400;
			List<Order> orders = orderRepo.getOrderByTimeRange(timestamp, timeTo);

			result.setLabel(new ArrayList<>());
			result.setCompleted(new ArrayList<>());
			result.setCanceled(new ArrayList<>());

			// Sử dụng Set để theo dõi giờ đã xuất hiện
			Set<String> uniqueHours = new HashSet<>();

			// Duyệt qua từng đơn hàng
			for (Order order : orders) {
				// Lấy giờ từ thời gian đơn hàng (đang ở dạng unix timestamp)
				long unixTimestamp = order.getTime();
				String hour = getHourFromUnixTimestamp(unixTimestamp);

				// Kiểm tra xem giờ đã xuất hiện chưa
				if (uniqueHours.add(hour)) {
					// Nếu giờ chưa xuất hiện, thêm giờ vào label và thêm số lượng
					// completed/canceled tương ứng
					result.getLabel().add(hour);
					if (order.getStatus() == 2) {
						// Trạng thái là 2, tức là completed
						result.getCompleted().add(1);
						result.getCanceled().add(0);
					} else {
						// Trạng thái khác 2, tức là canceled
						result.getCompleted().add(0);
						result.getCanceled().add(1);
					}
				} else {
					// Nếu giờ đã xuất hiện, tìm vị trí của giờ trong label và tăng số lượng
					// completed/canceled tương ứng
					int index = result.getLabel().indexOf(hour);
					if (order.getStatus() == 2) {
						// Trạng thái là 2, tức là completed
						result.getCompleted().set(index, result.getCompleted().get(index) + 1);
					} else {
						// Trạng thái khác 2, tức là canceled
						result.getCanceled().set(index, result.getCanceled().get(index) + 1);
					}
				}
			}

			return result;
		} else if (time.equals("lastweek")) {
			long timeForm = timestamp - 7 * 86400;
			List<Order> orders = orderRepo.getOrderByTimeRange(timeForm, timestamp+86400);
			result.setLabel(new ArrayList<>());
			result.setCompleted(new ArrayList<>());
			result.setCanceled(new ArrayList<>());

			// Sử dụng Set để theo dõi giờ đã xuất hiện
			Set<String> uniqDate = new HashSet<>();

			// Duyệt qua từng đơn hàng
			for (Order order : orders) {
				// Lấy giờ từ thời gian đơn hàng (đang ở dạng unix timestamp)
				long unixTimestamp = order.getTime();
				String day = getDayFromUnixTimestamp(unixTimestamp);

				// Kiểm tra xem giờ đã xuất hiện chưa
				if (uniqDate.add(day)) {
					// Nếu giờ chưa xuất hiện, thêm giờ vào label và thêm số lượng
					// completed/canceled tương ứng
					result.getLabel().add(day);
					if (order.getStatus() == 2) {
						// Trạng thái là 2, tức là completed
						result.getCompleted().add(1);
						result.getCanceled().add(0);
					} else {
						// Trạng thái khác 2, tức là canceled
						result.getCompleted().add(0);
						result.getCanceled().add(1);
					}
				} else {
					// Nếu giờ đã xuất hiện, tìm vị trí của giờ trong label và tăng số lượng
					// completed/canceled tương ứng
					int index = result.getLabel().indexOf(day);
					if (order.getStatus() == 2) {
						// Trạng thái là 2, tức là completed
						result.getCompleted().set(index, result.getCompleted().get(index) + 1);
					} else {
						// Trạng thái khác 2, tức là canceled
						result.getCanceled().set(index, result.getCanceled().get(index) + 1);
					}
				}
			}

			return result;
		} else if (time.equals("lastmonth")) {
			long timeForm = timestamp - 30 * 86400;
			List<Order> orders = orderRepo.getOrderByTimeRange(timeForm, timestamp+86400);
			result.setLabel(new ArrayList<>());
			result.setCompleted(new ArrayList<>());
			result.setCanceled(new ArrayList<>());

			// Sử dụng Set để theo dõi giờ đã xuất hiện
			Set<String> uniqDate = new HashSet<>();

			// Duyệt qua từng đơn hàng
			for (Order order : orders) {
				// Lấy giờ từ thời gian đơn hàng (đang ở dạng unix timestamp)
				long unixTimestamp = order.getTime();
				String day = getDayFromUnixTimestamp(unixTimestamp);

				// Kiểm tra xem giờ đã xuất hiện chưa
				if (uniqDate.add(day)) {
					// Nếu giờ chưa xuất hiện, thêm giờ vào label và thêm số lượng
					// completed/canceled tương ứng
					result.getLabel().add(day);
					if (order.getStatus() == 2) {
						// Trạng thái là 2, tức là completed
						result.getCompleted().add(1);
						result.getCanceled().add(0);
					} else {
						// Trạng thái khác 2, tức là canceled
						result.getCompleted().add(0);
						result.getCanceled().add(1);
					}
				} else {
					// Nếu giờ đã xuất hiện, tìm vị trí của giờ trong label và tăng số lượng
					// completed/canceled tương ứng
					int index = result.getLabel().indexOf(day);
					if (order.getStatus() == 2) {
						// Trạng thái là 2, tức là completed
						result.getCompleted().set(index, result.getCompleted().get(index) + 1);
					} else {
						// Trạng thái khác 2, tức là canceled
						result.getCanceled().set(index, result.getCanceled().get(index) + 1);
					}
				}
			}

			return result;
		} else {
			return result;
		}
	}

	private static String getHourFromUnixTimestamp(long unixTimestamp) {
		TimeZone gmt7TimeZone = TimeZone.getTimeZone("Asia/Bangkok"); // Múi giờ GMT+7

		long gmt7Millis = unixTimestamp * 1000 + gmt7TimeZone.getRawOffset();

		int hour = (int) ((gmt7Millis / (1000 * 60 * 60)) % 24);

		return hour + "h";

	}

	public static String getDayFromUnixTimestamp(long unixTimestamp) {
		TimeZone gmt7TimeZone = TimeZone.getTimeZone("Asia/Bangkok"); // Múi giờ GMT+7

		Date date = new Date(unixTimestamp * 1000 + gmt7TimeZone.getRawOffset());
		int month = date.getMonth() + 1;

		return date.getDate() + "/" + month;
	}

	@Override
	public OrderResponse checkCart(OrderRequest request) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		double total = 0;
		List<OrderDetail> orderDetails = new ArrayList<>();
		List<Food> foods = new ArrayList<>();
		String messageNotAvailable = "";
		String messagePriceDiff = "";
		for (String phrase : request.getFood()) {
			String[] item = phrase.split("-");
			long foodId = Long.parseLong(item[0]);
			int quantity = Integer.parseInt(item[1]);
			long price = Long.parseLong(item[2]);
			Optional<Food> food = foodRepo.getFoodById(foodId);
			if (food.isEmpty()) {
				throw new NotFoundException("Food is not existed!");
			} else if (food.get().getStatus() != 0) {
				messageNotAvailable += food.get().getName() + "\n";
			} else {
				if (price != food.get().getPrice()) {
					messagePriceDiff += food.get().getName() + "\n";
				} else {
					OrderDetail orderDetail = new OrderDetail();
					orderDetail.setFood_id(foodId);
					orderDetail.setQuantity(quantity);
					Optional<Coupon> coupon = coupRepo.findById(request.getCodeId());
					if (coupon.isPresent()) {
						orderDetail.setRate(coupon.get().getRate());
					} else {
						orderDetail.setRate(0);
					}
					orderDetail.setPrice(food.get().getPrice());
					orderDetails.add(orderDetail);
					total += food.get().getPrice();
					foods.add(food.get());
				}
			}
		}

		OrderResponse response = new OrderResponse();
		response.setMessage1(messageNotAvailable);
		response.setMessage2(messagePriceDiff);

		return response;
	}

}

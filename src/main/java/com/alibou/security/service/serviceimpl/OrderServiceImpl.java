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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibou.security.dto.BestSellerDto;
import com.alibou.security.dto.OrderRequest;
import com.alibou.security.dto.OrderResponse;
import com.alibou.security.dto.StatResponse;
import com.alibou.security.dto.ToggleOrderRequest;
import com.alibou.security.entity.Coupon;
import com.alibou.security.entity.Food;
import com.alibou.security.entity.Order;
import com.alibou.security.entity.OrderDetail;
import com.alibou.security.exception.NotFoundException;
import com.alibou.security.repository.CouponRepository;
import com.alibou.security.repository.FoodRepository;
import com.alibou.security.repository.OrderDetailRepository;
import com.alibou.security.repository.OrderRepository;
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

	@Override
	public OrderResponse placeOrder(OrderRequest request) {
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
				throw new NotFoundException("Món ăn không tồn tại!");
			} else if (food.get().getStatus() != 0) {
				sb.append(food.get().getName() + " + ");
			} else {
				if (price != food.get().getPrice()) {
					messagePriceDiff += food.get().getId() + "-" + food.get().getName() + "---";
				} else {
					OrderDetail orderDetail = new OrderDetail();
					orderDetail.setFood_id(foodId);
					orderDetail.setQuantity(quantity);
					Optional<Coupon> coupon = coupRepo.getByCode(request.getCode());
					if (coupon.isPresent()) {
						double rate = request.getRate();
						orderDetail.setRate(rate);
						total += (food.get().getPrice() - food.get().getPrice()*rate/100) * quantity;
					} else {
						orderDetail.setRate(0);
						total += (food.get().getPrice()) * quantity;
					}
					orderDetail.setPrice(food.get().getPrice());
					orderDetails.add(orderDetail);
					
					foods.add(food.get());
				}
			}
		}
		
		if (sb.toString().contains(" + ")) {
			messageNotAvailable = sb.toString().substring(0, sb.toString().length() - 2) + "hết món!";
		}

		if (messageNotAvailable.equals("") && messagePriceDiff.equals("")) {
			Order order = new Order();
			order.setStatus(0);
			order.setTime(System.currentTimeMillis() / 1000);
			order.setName(request.getName());
			order.setPhone(request.getPhone());
			order.setAddress(request.getAddress());
			order.setMessage(request.getMessage());
			order.setTotal(total);

			Order savedOrder = orderRepo.save(order);

			for (OrderDetail orderDetail : orderDetails) {
				orderDetail.setOrder_id(savedOrder.getId());
				orderDetail.setCode(request.getCode());
				orderDetail.setRate(request.getRate());
				orderDetailRepo.save(orderDetail);
			}
		}

		OrderResponse response = new OrderResponse();
		response.setMessage1(messageNotAvailable);
		response.setMessage2(messagePriceDiff);

		return response;
	}

	@Override
	public void toggleStatus(long orderId) {
		Order order = orderRepo.getById(orderId);
		int status = order.getStatus();
		int statusNext = status + 1;
		if (statusNext > 2) {
			statusNext = 2;
		}
		order.setStatus(statusNext);
		orderRepo.save(order);
	}

	@Override
	public List<Order> getAllOrders() {
		// TODO Auto-generated method stub
		return orderRepo.findAll();
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
	public Page<Order> getPaginatedOrders(Pageable pageable) {
		// TODO Auto-generated method stub
		return orderRepo.findAllPagable(pageable);
	}

	@Override
	public void cancelOrder(long orderId) {
		// TODO Auto-generated method stub
		Order order = orderRepo.getById(orderId);
		order.setStatus(3);
		orderRepo.save(order);
	}

	@Override
	public List<Integer> getTodayReport(long timeFrom, long timeTo) {
		// TODO Auto-generated method stub
		List<Order> orders = orderRepo.getOrderByTimeRange(timeFrom, timeTo+86400);
		List<Integer> result = new ArrayList<>();
		int amountOfCompletedOrders = 0, amountOfCanceledOrders = 0, totalSales = 0;

		for (Order order : orders) {
			if (order.getStatus() == 2) {
				amountOfCompletedOrders++;
			}
			if (order.getStatus() == 3) {
				amountOfCanceledOrders++;
			}
			List<OrderDetail> orderDetails = orderDetailRepo.getOrderDetailsById(order.getId());
			for (OrderDetail orderDetail : orderDetails) {
				totalSales += (orderDetail.getPrice() - orderDetail.getPrice() * orderDetail.getRate()/100)
						* orderDetail.getQuantity();
			}
		}

		result.add(totalSales);
		result.add(orders.size());
		result.add(amountOfCompletedOrders);
		result.add(amountOfCanceledOrders);
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
		int lowestOrderAmount = Integer.MAX_VALUE;

		for (Order order : orders) {
			int totalAmount = 0;
			List<OrderDetail> orderDetails = orderDetailRepo.getOrderDetailsById(order.getId());
			for (OrderDetail orderDetail : orderDetails) {
				totalAmount += (orderDetail.getPrice() - orderDetail.getPrice() * orderDetail.getRate()/100)
						* orderDetail.getQuantity();
			}
			lowestOrderAmount = Math.min(lowestOrderAmount, totalAmount);
		}

		return lowestOrderAmount == Integer.MAX_VALUE ? 0 : lowestOrderAmount;
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
				subTotal += (item.getPrice() - item.getPrice() * item.getRate()/100) * item.getQuantity();
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

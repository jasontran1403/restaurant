var stompClient = null;
var username = null;

function formatPrice(price) {
    // Sử dụng Number.toLocaleString() để định dạng số
    return price.toLocaleString('en-US');
}

function connect(event) {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, onConnected, onError);
}

var check = false;
var rate = 0.0;
var codeCoupon = "";


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/register",
        {},
        JSON.stringify({ sender: "guest", type: 'JOIN' })
    )
}


function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}

function send(event) {
    var messageContent = "TESTSTSTST";

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: "guest",
            content: messageContent,
            type: 'CHAT'
        };

        stompClient.send("/app/send", {}, JSON.stringify(chatMessage));
    }
}

var checkout = () => {
    var user = localStorage.getItem("user");

    if (!user) {
        displayToast("Cần phải đăng nhập để đặt đơn hàng!", "error");
        return;
    }
    var cartItems = JSON.parse(localStorage.getItem("cartItems"));

    if (cartItems.length === 0) {
        displayToast("Giỏ hàng trống!", "error");
        return;
    }

    var name = document.getElementById("name").value.trim();
    var phone = document.getElementById("phone").value.trim();
    var address = document.getElementById("address").value.trim();
    var message = document.getElementById("message").value.trim();

    if (name === "" || phone === "" || address === "") {
        displayToast("Thông tin người nhận hàng trống!", "error");
        return;
    }

    if (!isValidVietnamesePhone(phone)) {
        displayToast("Số điện thoại không đúng định dạng!", "error");
        return;
    }

    var myHeaders = new Headers();
    myHeaders.append("Content-Type", "application/json");

    var raw = JSON.stringify({
        food: cartItems.map(item => `${item.id}-${item.quantity}-${item.price}`),
        name: name,
        phone: phone,
        address: address,
        message: message,
        code: check ? codeCoupon : "",
        rate: check ? rate : 0,
        agency: user
    });

    var requestOptions = {
        method: 'POST',
        headers: myHeaders,
        body: raw,
        redirect: 'follow'
    };

    fetch("/api/v1/auth/check-cart", requestOptions)
        .then(response => response.text())
        .then(result => {
            console.log(result);
            if (result === 'Đặt thành công!') {
                const buyBtns = document.querySelectorAll('.js-cart')
                const modal = document.querySelector('.plus-modal')
                const modalClose = document.querySelector('.modal-close')

                const bill = document.querySelector('.plus-modal__body');
                bill.innerHTML = ''; // Clear the content of the bill element

                // Create the table element
                const table = document.createElement('table');
                table.className = 'table';

                // Create the table header
                const thead = document.createElement('thead');
                const headerRow = document.createElement('tr');
                const headers = ['Tên món ăn', 'Số lượng', 'Đơn giá', 'Thành tiền'];

                headers.forEach(headerText => {
                    const th = document.createElement('th');
                    th.textContent = headerText;
                    headerRow.appendChild(th);
                });

                thead.appendChild(headerRow);
                table.appendChild(thead);

                // Create the table body
                const tbody = document.createElement('tbody');

                cartItems.forEach(item => {
                    const row = document.createElement('tr');

                    // Customize the columns based on your item structure
                    const nameCell = document.createElement('td');
                    nameCell.textContent = item.name;

                    const quantityCell = document.createElement('td');
                    quantityCell.textContent = item.quantity;

                    const priceCell = document.createElement('td');
                    priceCell.textContent = formatPrice(item.price);

                    const totalCell = document.createElement('td');
                    totalCell.textContent = formatPrice(item.quantity * item.price) + " VNĐ";

                    // Append cells to the row
                    row.appendChild(nameCell);
                    row.appendChild(quantityCell);
                    row.appendChild(priceCell);
                    row.appendChild(totalCell);

                    // Append the row to the table body
                    tbody.appendChild(row);
                });

                // Add a row for subtotal
                const subtotalRow = document.createElement('tr');
                const emptyCell1 = document.createElement('td'); // Empty cell for alignment
                const emptyCell2 = document.createElement('td'); // Empty cell for alignment
                const subtotalCell1 = document.createElement('td');
                subtotalCell1.textContent = 'Tạm tính: ';
                const subtotalCell2 = document.createElement('td');
                subtotalCell2.textContent = subtotal.innerText;

                subtotalRow.appendChild(emptyCell1);
                subtotalRow.appendChild(emptyCell2);
                subtotalRow.appendChild(subtotalCell1);
                subtotalRow.appendChild(subtotalCell2);

                // Add a row for discount
                const discountRow = document.createElement('tr');
                const emptyCell3 = document.createElement('td'); // Empty cell for alignment
                const emptyCell4 = document.createElement('td'); // Empty cell for alignment
                const discountCell1 = document.createElement('td');
                discountCell1.textContent = 'Giảm giá: ';
                const discountCell2 = document.createElement('td');
                discountCell2.textContent = discount.innerText;

                discountRow.appendChild(emptyCell3);
                discountRow.appendChild(emptyCell4);
                discountRow.appendChild(discountCell1);
                discountRow.appendChild(discountCell2);

                // Add a row for total
                const totalRow = document.createElement('tr');
                const emptyCell5 = document.createElement('td'); // Empty cell for alignment
                const emptyCell6 = document.createElement('td'); // Empty cell for alignment
                const totalCell1 = document.createElement('td');
                totalCell1.textContent = 'Số tiền cần thanh toán: ';
                const totalCell2 = document.createElement('td');
                totalCell2.textContent = total.innerText;

                totalRow.appendChild(emptyCell5);
                totalRow.appendChild(emptyCell6);
                totalRow.appendChild(totalCell1);
                totalRow.appendChild(totalCell2);

                // Append the rows to the table body
                tbody.appendChild(subtotalRow);
                tbody.appendChild(discountRow);
                tbody.appendChild(totalRow);

                table.appendChild(tbody);

                // Append the table to the bill element
                bill.appendChild(table);

                modal.classList.add('open');
                function showBuyModal() {
                    modal.classList.add('open')
                }

                function hideBuyModal() {
                    modal.classList.remove('open')
                    window.location.reload();
                }

                for (const buyBtn of buyBtns) {
                    buyBtn.addEventListener('click', showBuyModal)

                }

                modalClose.addEventListener('click', hideBuyModal)

                fetch(`/place-order`, requestOptions)
                    .then(response => {
                        if (response.ok === true) {
                            send();
                            localStorage.removeItem("cartItems");

                            // Hiển thị thông báo và đợi 5 giây trước khi chuyển về trang chủ
                            setTimeout(() => {
                                window.location.href = '/';
                            }, 5000); // 5000ms = 5 giây
                        }
                    })
                    .catch(error => {
                        console.error('Lỗi:', error);
                    });
            } else {
                displayToast(result, "error");
            }
        })
        .catch(error => console.log('error', error));
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);
}

function isValidVietnamesePhone(phone) {
    // Xóa tất cả khoảng trắng trong số điện thoại
    let cleanedPhone = phone.replace(/\s+/g, "");

    // Biểu thức chính quy kiểm tra số điện thoại VN hợp lệ
    const regex = /^(0\d{9}|0\d{10})$/;
    return regex.test(cleanedPhone);
}

function formatVietnamesePhone(phone) {
    // Xóa khoảng trắng trước khi định dạng
    let cleanedPhone = phone.replace(/\s+/g, "");

    // Kiểm tra độ dài để format chuẩn
    if (cleanedPhone.length === 10) {
        return cleanedPhone.replace(/(\d{4})(\d{3})(\d{3})/, "$1 $2 $3");
    } else if (cleanedPhone.length === 11) {
        return cleanedPhone.replace(/(\d{4})(\d{3})(\d{4})/, "$1 $2 $3");
    }
    return phone; // Nếu không hợp lệ, trả về nguyên gốc
}

connect();


// Hàm renderCartTable để hiển thị giỏ hàng trong bảng
function renderCartTable() {
    var cartTableBody = document.querySelector("table tbody");
    cartTableBody.innerHTML = ''; // Xóa dữ liệu cũ trong bảng

    var cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];

    let total = 0;
    cartItems.forEach(function (cartItem) {
        var row = document.createElement("tr");
        row.classList.add("cart__product");

        row.innerHTML = `
      <td class="cart__product-item">
        <div class="cart__product-remove">
          <i class="fa fa-close" ></i>
        </div>
        <div class="cart__product-img">
          <img th:src="${cartItem.image}" alt="product" />
        </div>
        <div class="cart__product-title">
          <h6>${cartItem.name}</h6>
        </div>
      </td>
      <td class="cart__product-price">${formatPrice(cartItem.price)} VNĐ</td>
      <td class="cart__product-quantity">
        <div class="product-quantity">
          <div class="quantity__input-wrap">
            <i class="fa fa-minus decrease-qty" onclick="decreaseQuantity(${cartItem.id})"></i>
            <input type="number" value="${cartItem.quan}" class="qty-input" disabled readonly />
            <i class="fa fa-plus increase-qty" onclick="increaseQuantity(${cartItem.id})"></i>
          </div>
        </div>
      </td>
      <td class="cart__product-total">${formatPrice(cartItem.price * cartItem.quan)} VNĐ</td>
    `;

        cartTableBody.appendChild(row);
        total += cartItem.price * cartItem.quan;

    });

    var cartTableActionRow = document.querySelector(".cart__product-action-content");
    cartTableActionRow.innerHTML = `
    <form class="d-flex flex-wrap">
      <input type="text" class="form-control mb-10 mr-10" placeholder="Coupon Code">
      <button type="submit" class="btn btn__primary mb-10">Apply Coupon</button>
    </form>
    <div>
      <a class="btn btn__secondary mr-10" href="#" onclick="updateCart()">Update Cart</a>
      <a class="btn btn__primary" href="#" onclick="checkout()">Checkout</a>
    </div>
  `;

    var subtotalElement = document.querySelector("#subtotal");
    subtotalElement.textContent = `${total.toFixed(2)} VNĐ`;
}

// Hàm xóa sản phẩm khỏi giỏ hàng
// function removeCartItem(id) {
//
//     let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
//
//     // Lọc ra các sản phẩm khác với id cần xóa
//     cartItems = cartItems.filter(item => item.id !== id);
//
//     // Lưu lại danh sách mới
//     localStorage.setItem("cartItems", JSON.stringify(cartItems));
//     displayToast("Xoá món ăn khỏi giỏ thành công!", "success");
//     // Cập nhật lại bảng giỏ hàng
//     renderCartTable();
// }

// Hàm giảm số lượng sản phẩm
function decreaseQuantity(id) {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    // Tìm sản phẩm cần giảm
    var item = cartItems.find(item => item.id === id);

    if (item && item.quantity > 1) {
        item.quantity--;
    }

    // Lưu lại danh sách mới
    localStorage.setItem("cartItems", JSON.stringify(cartItems));

    // Cập nhật lại bảng giỏ hàng
    renderCartTable();
}

// Hàm tăng số lượng sản phẩm
function increaseQuantity(id) {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];

    // Tìm sản phẩm cần tăng
    var item = cartItems.find(item => item.id === id);

    if (item) {
        item.quantity++;
    }

    // Lưu lại danh sách mới
    localStorage.setItem("cartItems", JSON.stringify(cartItems));

    // Cập nhật lại bảng giỏ hàng
    renderCartTable();
}

// Hàm cập nhật số lượng sản phẩm
function updateQuantity(id, newQuantity) {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];

    // Tìm sản phẩm cần cập nhật
    var item = cartItems.find(item => item.id === id);

    if (item) {
        item.quantity = parseInt(newQuantity);
    }

    // Lưu lại danh sách mới
    localStorage.setItem("cartItems", JSON.stringify(cartItems));

    // Cập nhật lại bảng giỏ hàng
    renderCartTable();
}

// Hàm cập nhật giỏ hàng
function updateCart() {
    // Đưa ra xử lý cập nhật giỏ hàng (ví dụ: kiểm tra giảm giá, áp dụng mã giảm giá)
    // Sau đó cập nhật lại bảng giỏ hàng
    renderCartTable();
}

// Render giỏ hàng lên table cart.
function updateQuantityAction(row, action) {
    var quantityInput = row.querySelector(".qty-input");
    var currentValue = parseInt(quantityInput.value);
    if (!isNaN(currentValue)) {
        let quantity = currentValue;

        quantityInput.value = currentValue;

        var priceCell = row.querySelector(".cart__product-price");
        var totalPriceCell = row.querySelector(".cart__product-total");
        var price = parseFloat(priceCell.textContent.replace("$", ""));
        let adjustedPrice = price < 1000 ? price * 1000 : price; // Nếu price < 1000 thì nhân với 1000
        totalPriceCell.textContent = `${formatPrice(adjustedPrice * quantity)} VNĐ`;
        updateCart();
    }
}

function removeProduct(row) {
    row.remove();
    displayToast("Xoá sản phẩm giỏ thành công!", "success");
    updateCart();
}

function updateCart() {
    var rows = document.querySelectorAll(".cart__product");
    var updatedCartItems = [];

    var subTotal = 0;
    rows.forEach(function (row, index) {

        var idCell = row.querySelector(".cart__product-id"); // Cột ID
        var name = row.querySelector(".cart__product-title h6").textContent;
        var price = parseFloat(row.querySelector(".cart__product-price").textContent.replace("$", ""));
        let adjustedPrice = price < 1000 ? price * 1000 : price; // Nếu price < 1000 thì nhân với 1000
        var quantityInput = row.querySelector(".qty-input");
        var quantity = parseInt(quantityInput.value);
        var image = row.querySelector(".cart__product-img img").src;
        subTotal += adjustedPrice * quantity;
        if (!isNaN(quantity)) {
            let adjustedPrice2 = price < 1000 ? price * 1000 : price;
            updatedCartItems.push({
                id: idCell.textContent, // Lấy ID từ cột ID
                name,
                price: adjustedPrice2,
                quantity,
                image
            });
        }
    });

    // Lưu dữ liệu giỏ hàng cập nhật vào Local Storage
    localStorage.setItem("cartItems", JSON.stringify(updatedCartItems));
    updateTotal(subTotal, subTotal);
}

document.addEventListener("DOMContentLoaded", function () {
    var cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    var cartTableBody = document.getElementById("cart-table-body");
    var subTotal = 0.0;
    var total = 0.0;

    cartItems.forEach(function (item, index) {
        var row = document.createElement("tr");
        row.className = "cart__product";
        let adjustedPrice = item.price < 1000 ? item.price * 1000 : item.price; // Nếu price < 1000 thì nhân với 1000
        subTotal += adjustedPrice * item.quantity;
        updateTotal(subTotal, subTotal);
        row.innerHTML = `
      <td class="cart__product-id" style="display:none;">${item.id}</td>
      <td class="cart__product-item">
        <div class="cart__product-remove">
          <i class="fa fa-close" ></i>
        </div>
        <div class="cart__product-img">
          <img src="${item.image}" alt="product" />
        </div>
        <div class="cart__product-title">
          <h6>${item.name}</h6>
        </div>
      </td>
      <td class="cart__product-price">${formatPrice(adjustedPrice)} VNĐ</td>
      <td class="cart__product-quantity">
        <div class="product-quantity">
          <div class="quantity__input-wrap">
            <i class="fa fa-minus decrease-qty"></i>
            <input type="number" value="${item.quantity}" class="qty-input">
            <i class="fa fa-plus increase-qty"></i>
          </div>
        </div>
      </td>
      <td class="cart__product-total">${formatPrice(adjustedPrice * item.quantity)} VNĐ</td>
    `;

        cartTableBody.appendChild(row);
    });
});

function updateTotal(subTotal, total) {
    document.getElementById("subtotal").innerText = `${formatPrice(subTotal)} VNĐ`;
    document.getElementById("total").innerText = `${formatPrice(total)} VNĐ`;
}

function displayToast(message, type) {
    const main = document.getElementById("toast-list");
    if (main) {

        const toastElement = document.createElement("div");

        const title = "Notification!";

        const icon = "fas fa-check-circle";

        toastElement.classList.add("toast", `toast--${type}`);
        toastElement.style.animation = `slideInLeft ease .3s forwards`;

        const autoRemoveId = setTimeout(function () {
            main.removeChild(toastElement);
        }, 1500);

        // Remove toast when clicked
        toastElement.onclick = function (e) {
            main.removeChild(toastElement);
        };

        toastElement.innerHTML = `
            <div class="toast__icon">
                <i class="${icon}"></i>
            </div>
            <div class="toast__body">
                <h3 class="toast__title">${title}</h3>
                <p class="toast__msg">${message}</p>
            </div>
            <div class="toast__close">
                <i class="fas fa-times"></i>
            </div>
        `;
        main.appendChild(toastElement);
    }
}

var isCouponApplied = false;

function checkCoupon() {
    if (localStorage.getItem("user") == null) {
        return;
    }
    var code = document.getElementById('coupon-code');
    if (code.value === "") {
        alert("Vui lòng nhập mã giảm giá!");
        return;
    }

    var data = {
        username: localStorage.getItem("user"),
        couponCode: code.value
    };

    var requestOptions = {
        method: 'POST', // Change to 'POST' or another method that supports body
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        redirect: 'follow'
    };

    fetch("/api/v1/auth/validate-coupon", requestOptions)
        .then(response => response.text())
        .then(result => {
            console.log(result);
            if (result === '0.0') {
                // Handle invalid coupon
                displayToast("Mã giảm giá không hợp lệ!", "error");
                check = true;
                rate = result;
                codeCoupon = '';
                resetCoupon(); // Đặt lại các giá trị khi mã không hợp lệ
            } else if (result === '-1.0') {
                // Handle inactive coupon
                check = true;
                rate = 0;
                codeCoupon = '';
                displayToast("Mã giảm giá đã ngưng hoạt động!", "error");
                resetCoupon(); // Đặt lại các giá trị khi mã không hợp lệ
            } else {

                if (isCouponApplied) {
                    displayToast("Mã giảm giá đã được áp dụng!", "info");
                } else {
                    // Handle successful coupon application
                    displayToast("Áp dụng mã giảm giá thành công!", "success");
                    var discountPercentage = parseFloat(result);
                    applyDiscount(discountPercentage);
                    if (code.value === "LPMARKETING") {
                        applyDiscountDefaultPrice(discountPercentage);
                    } else {
                        applyDiscount(discountPercentage);
                    }
                    rate = result;
                    isCouponApplied = true;
                    check = true;
                    codeCoupon = code.value;
                }
            }
        })
        .catch(error => console.log('error', error));
}

function applyDiscount(discountPercentage) {
    var totalElement = document.getElementById('total');
    var subtotalElement = document.getElementById('subtotal');

    var total = parseFloat(subtotalElement.innerText.replace(/[^0-9.-]+/g, ''));
    var discountedTotal = total - (total * discountPercentage / 100);

    // Update UI with discounted total
    totalElement.innerText = `${formatPrice(discountedTotal)} VNĐ`;

    // Update discount display
    document.getElementById('discount').innerText = `${discountPercentage}%`;
}

function applyDiscountDefaultPrice(discountPercentage) {
    var totalElement = document.getElementById('total');
    var subtotalElement = document.getElementById('subtotal');

    var total = parseFloat(subtotalElement.innerText.replace(/[^0-9.-]+/g, ''));
    var discountedTotal = total - (total * discountPercentage / 100);

    // Update UI with discounted total
    totalElement.innerText = `${formatPrice(discountedTotal)} VNĐ`;

    // Update discount display
    document.getElementById('discount').innerText = `Mã giảm giá gốc`;
}

function resetCoupon() {
    isCouponApplied = false;
    // Reset UI elements as needed
    document.getElementById('total').innerText = ""; // Reset total amount
    document.getElementById('discount').innerText = "0.0%"; // Reset discount display
    // Additional reset steps as needed
    var totalElement = document.getElementById('total');
    totalElement.innerText = document.getElementById('subtotal').innerText;
}

document.addEventListener("click", function (e) {
    if (e.target.classList.contains("increase-qty")) {
        e.preventDefault();
        var row = e.target.closest(".cart__product");
        updateQuantityAction(row, "increase");
    } else if (e.target.classList.contains("decrease-qty")) {
        e.preventDefault();
        var row = e.target.closest(".cart__product");
        updateQuantityAction(row, "decrease");
    } else if (e.target.classList.contains("fa-close")) {
        var row = e.target.closest(".cart__product");
        removeProduct(row);
    }
});
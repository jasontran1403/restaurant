var stompClient = null;
var username = null;
var isDefaultPriceApplied = false; // Flag to check if default price is applied
var isCouponApplied = false; // Flag to check if a coupon is applied
var check = false;
var rate = 0.0; // Discount rate (percentage or 200 for default price)
var codeCoupon = ""; // Coupon code

function formatPrice(price) {
    return price.toLocaleString('en-US');
}

function onVatChange() {
    // Get current cart state
    const cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    const subtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const vatSelect = document.getElementById('vat');

    // Calculate discount amount based on current state
    let discountAmount = 0;
    if (isCouponApplied) {
        if (isDefaultPriceApplied) {
            discountAmount = cartItems.reduce((sum, item) => {
                const defaultPrice = item.default || item.price;
                // Sửa ở đây - thêm Math.abs()
                return sum + Math.abs((defaultPrice - item.price) * item.quantity);
            }, 0);
        } else {
            discountAmount = subtotal * rate / 100;
        }
    }

    const discountedSubtotal = subtotal - discountAmount;
    const { vatAmount, total } = calculateVAT(discountedSubtotal);

    // Update UI
    document.getElementById('subtotal').innerText = `${formatPrice(subtotal)} VNĐ`;
    document.getElementById('discount').innerText = isCouponApplied
        ? `${formatPrice(discountAmount)} VNĐ${isDefaultPriceApplied ? '' : ` (${rate}%)`}`
        : '0 VNĐ';
    document.getElementById('vatAmount').innerText = `${formatPrice(vatAmount)} VNĐ (${vatSelect.value}%)`;
    document.getElementById('total').innerText = `${formatPrice(total)} VNĐ`;
}

function calculateVAT(subtotal) {
    // If subtotal not provided, get it from the UI
    if (subtotal === undefined) {
        const subtotalText = document.getElementById('subtotal').innerText;
        subtotal = parseFloat(subtotalText.replace(/[^0-9.-]+/g, ''));
    }

    const vatSelect = document.getElementById('vat');
    const vatRate = parseFloat(vatSelect.value);
    const vatAmount = Math.abs(subtotal) * vatRate / 100;
    const total = subtotal + vatAmount;

    return { vatAmount, total };
}

function applyDiscount(discountPercentage) {
    const subtotalText = document.getElementById('subtotal').innerText;
    const subtotal = parseFloat(subtotalText.replace(/[^0-9.-]+/g, ''));
    const discountAmount = subtotal * discountPercentage / 100;
    const discountedSubtotal = subtotal - discountAmount;

    const { vatAmount, total } = calculateVAT(discountedSubtotal);
    const vatRate = parseFloat(document.getElementById('vat').value);

    // Update UI
    document.getElementById('subtotal').innerText = `${formatPrice(subtotal)} VNĐ`;
    document.getElementById('discount').innerText = `${formatPrice(discountAmount)} VNĐ (${discountPercentage}%)`;
    document.getElementById('vatAmount').innerText = `${formatPrice(vatAmount)} VNĐ (${vatRate}%)`;
    document.getElementById('total').innerText = `${formatPrice(total)} VNĐ`;

    updateTotal(subtotal, total, vatAmount, discountAmount);
    onVatChange(); // Add this at the end
}

function applyDefaultPrice() {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    let total = 0;
    let totalDiscount = 0;
    let tempTotal = 0;

    cartItems.forEach(item => {
        const defaultPrice = item.default || item.price;
        total += defaultPrice * item.quantity;
        // Sửa ở đây - thêm Math.abs()
        totalDiscount += Math.abs((defaultPrice - item.price) * item.quantity);
        tempTotal += item.price * item.quantity;
    });

    const discountedSubtotal = total - totalDiscount;
    const { vatAmount, total: finalTotal } = calculateVAT(discountedSubtotal);
    const vatRate = parseFloat(document.getElementById('vat').value);

    // Update UI
    document.getElementById('subtotal').innerText = `${formatPrice(tempTotal)} VNĐ`;
    document.getElementById('discount').innerText = `${formatPrice(totalDiscount)} VNĐ`;
    document.getElementById('vatAmount').innerText = `${formatPrice(vatAmount)} VNĐ (${vatRate}%)`;
    document.getElementById('total').innerText = `${formatPrice(finalTotal)} VNĐ`;

    updateTotal(tempTotal, finalTotal, vatAmount, totalDiscount);
    onVatChange();
}

function resetCoupon() {
    isCouponApplied = false;
    isDefaultPriceApplied = false;
    const subtotalText = document.getElementById('subtotal').innerText;
    const subtotal = parseFloat(subtotalText.replace(/[^0-9.-]+/g, ''));

    const { vatAmount, total } = calculateVAT(subtotal);
    const vatRate = parseFloat(document.getElementById('vat').value);

    // Reset UI
    document.getElementById('subtotal').innerText = `${formatPrice(subtotal)} VNĐ`;
    document.getElementById('discount').innerText = "0 VNĐ";
    document.getElementById('vatAmount').innerText = `${formatPrice(vatAmount)} VNĐ (${vatRate}%)`;
    document.getElementById('total').innerText = `${formatPrice(total)} VNĐ`;

    updateTotal(subtotal, total, vatAmount, 0);
    rate = 0.0;
    codeCoupon = "";
    check = false;
    onVatChange(); // Add this at the end
}

function connect(event) {
    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, onConnected, onError);
}

function onConnected() {
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.send("/app/register", {}, JSON.stringify({ sender: "guest", type: 'JOIN' }));
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
    var addressReceive = document.getElementById("addressReceive").value.trim();
    var vat = document.getElementById("vat").value.trim();

    if (name === "" || phone === "" || address === "" || addressReceive === "") {
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
        addressReceive: addressReceive,
        vat: vat,
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
                const buyBtns = document.querySelectorAll('.js-cart');
                const modal = document.querySelector('.plus-modal');
                const modalClose = document.querySelector('.modal-close');

                const bill = document.querySelector('.plus-modal__body');
                bill.innerHTML = '';

                const table = document.createElement('table');
                table.className = 'table';

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

                const tbody = document.createElement('tbody');

                cartItems.forEach(item => {
                    const row = document.createElement('tr');
                    const nameCell = document.createElement('td');
                    nameCell.textContent = item.name;
                    const quantityCell = document.createElement('td');
                    quantityCell.textContent = item.quantity + " X " + item.unit;
                    const priceCell = document.createElement('td');
                    priceCell.textContent = formatPrice(item.price);
                    const totalCell = document.createElement('td');
                    totalCell.textContent = formatPrice(item.quantity * item.price) + " VNĐ";

                    row.appendChild(nameCell);
                    row.appendChild(quantityCell);
                    row.appendChild(priceCell);
                    row.appendChild(totalCell);
                    tbody.appendChild(row);
                });

                const subtotalRow = document.createElement('tr');
                const emptyCell1 = document.createElement('td');
                const emptyCell2 = document.createElement('td');
                const subtotalCell1 = document.createElement('td');
                subtotalCell1.textContent = 'Tạm tính: ';
                const subtotalCell2 = document.createElement('td');
                subtotalCell2.textContent = document.getElementById('subtotal').innerText;

                subtotalRow.appendChild(emptyCell1);
                subtotalRow.appendChild(emptyCell2);
                subtotalRow.appendChild(subtotalCell1);
                subtotalRow.appendChild(subtotalCell2);

                const discountRow = document.createElement('tr');
                const emptyCell3 = document.createElement('td');
                const emptyCell4 = document.createElement('td');
                const discountCell1 = document.createElement('td');
                discountCell1.textContent = 'Giảm giá: ';
                const discountCell2 = document.createElement('td');
                discountCell2.textContent = document.getElementById('discount').innerText;

                discountRow.appendChild(emptyCell3);
                discountRow.appendChild(emptyCell4);
                discountRow.appendChild(discountCell1);
                discountRow.appendChild(discountCell2);

                const vatRow = document.createElement('tr');
                const emptyCell7 = document.createElement('td');
                const emptyCell8 = document.createElement('td');
                const vatCell1 = document.createElement('td');
                vatCell1.textContent = `VAT (${vat}%): `;
                const vatCell2 = document.createElement('td');
                vatCell2.textContent = document.getElementById('vatAmount').innerText;

                vatRow.appendChild(emptyCell7);
                vatRow.appendChild(emptyCell8);
                vatRow.appendChild(vatCell1);
                vatRow.appendChild(vatCell2);

                const totalRow = document.createElement('tr');
                const emptyCell5 = document.createElement('td');
                const emptyCell6 = document.createElement('td');
                const totalCell1 = document.createElement('td');
                totalCell1.textContent = 'Số tiền cần thanh toán: ';
                const totalCell2 = document.createElement('td');
                totalCell2.textContent = document.getElementById('total').innerText;

                totalRow.appendChild(emptyCell5);
                totalRow.appendChild(emptyCell6);
                totalRow.appendChild(totalCell1);
                totalRow.appendChild(totalCell2);

                tbody.appendChild(subtotalRow);
                tbody.appendChild(discountRow);
                tbody.appendChild(vatRow);
                tbody.appendChild(totalRow);
                table.appendChild(tbody);
                bill.appendChild(table);

                modal.classList.add('open');
                function showBuyModal() {
                    modal.classList.add('open');
                }

                function hideBuyModal() {
                    modal.classList.remove('open');
                    window.location.reload();
                }

                for (const buyBtn of buyBtns) {
                    buyBtn.addEventListener('click', showBuyModal);
                }

                modalClose.addEventListener('click', hideBuyModal);

                fetch(`/place-order`, requestOptions)
                    .then(response => {
                        if (response.ok === true) {
                            send();
                            localStorage.removeItem("cartItems");
                            setTimeout(() => {
                                window.location.href = '/menu';
                            }, 3000);
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
    let cleanedPhone = phone.replace(/\s+/g, "");
    const regex = /^(0\d{9}|0\d{10})$/;
    return regex.test(cleanedPhone);
}

function renderCartTable() {
    var cartTableBody = document.querySelector("table tbody");
    cartTableBody.innerHTML = '';
    var cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    let total = 0;
    let totalDiscount = 0;

    cartItems.forEach(function (cartItem) {
        var row = document.createElement("tr");
        row.classList.add("cart__product");

        const currentPrice = cartItem.price;
        const defaultPrice = cartItem.default || cartItem.price;
        const itemDiscount = Math.abs((defaultPrice - currentPrice) * cartItem.quantity);
        totalDiscount += itemDiscount;
        total += currentPrice * cartItem.quantity;

        const priceDisplay = isDefaultPriceApplied ?
            `<span style="text-decoration: line-through; color: #999;">${formatPrice(currentPrice)} VNĐ</span><br>
             ${formatPrice(defaultPrice)} VNĐ` :
            `${formatPrice(currentPrice)} VNĐ`;

        row.innerHTML = `
      <td class="cart__product-id" style="display:none;">${cartItem.id}</td>
      <td class="cart__product-item">
        <div class="cart__product-remove">
          <i class="fa fa-close"></i>
        </div>
        <div class="cart__product-img">
          <img src="${cartItem.image}" alt="product" />
        </div>
        <div class="cart__product-title">
          <h6>${cartItem.name}</h6>
        </div>
      </td>
      <td class="cart__product-price">${priceDisplay}</td>
      <td class="cart__product-quantity">
        <div class="product-quantity">
          <div class="quantity__input-wrap">
            <i class="fa fa-minus decrease-qty" onclick="decreaseQuantity(${cartItem.id})"></i>
            <input type="number" value="${cartItem.quantity}" class="qty-input" disabled readonly />
            <i class="fa fa-plus increase-qty" onclick="increaseQuantity(${cartItem.id})"></i>
          </div>
        </div>
      </td>
      <td class="cart__product-unit">${cartItem.unit}</td>
      <td class="cart__product-total">${formatPrice(currentPrice * cartItem.quantity)} VNĐ</td>
    `;

        cartTableBody.appendChild(row);
    });

    const subtotal = total;
    const discountedSubtotal = total - totalDiscount;
    const { vatAmount, total: finalTotal } = calculateVAT(discountedSubtotal);
    const vatRate = parseFloat(document.getElementById('vat').value);

    // Update UI
    document.getElementById('subtotal').innerText = `${formatPrice(subtotal)} VNĐ`;
    document.getElementById('discount').innerText = totalDiscount > 0 ? `${formatPrice(totalDiscount)} VNĐ` : `0 VNĐ`;
    document.getElementById('vatAmount').innerText = `${formatPrice(vatAmount)} VNĐ (${vatRate}%)`;
    document.getElementById('total').innerText = `${formatPrice(finalTotal)} VNĐ`;

    if (isCouponApplied) {
        if (isDefaultPriceApplied) {
            applyDefaultPrice();
        } else {
            applyDiscount(parseFloat(rate));
        }
    }
}

function decreaseQuantity(id) {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    var item = cartItems.find(item => item.id === id);

    if (item && item.quantity > 1) {
        item.quantity--;
    }

    localStorage.setItem("cartItems", JSON.stringify(cartItems));
    renderCartTable();
}

function increaseQuantity(id) {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    var item = cartItems.find(item => item.id === id);

    if (item) {
        item.quantity++;
    }

    localStorage.setItem("cartItems", JSON.stringify(cartItems));
    renderCartTable();
}

function updateQuantity(id, newQuantity) {
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    var item = cartItems.find(item => item.id === id);

    if (item) {
        item.quantity = parseInt(newQuantity);
    }

    localStorage.setItem("cartItems", JSON.stringify(cartItems));
    renderCartTable();
}

function updateQuantityAction(row, action) {
    var quantityInput = row.querySelector(".qty-input");
    var currentValue = parseInt(quantityInput.value);

    if (!isNaN(currentValue)) {
        let quantity = action === "increase" ? currentValue + 1 : Math.max(1, currentValue - 1);
        quantityInput.value = quantity;

        var idCell = row.querySelector(".cart__product-id");
        updateQuantity(idCell.textContent, quantity);
    }
}

function removeProduct(row) {
    var idCell = row.querySelector(".cart__product-id");
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    cartItems = cartItems.filter(item => item.id != idCell.textContent);
    localStorage.setItem("cartItems", JSON.stringify(cartItems));
    row.remove();
    displayToast("Xoá sản phẩm giỏ thành công!", "success");
    renderCartTable();
}

function updateCart() {
    var rows = document.querySelectorAll(".cart__product");
    var updatedCartItems = [];
    var subTotal = 0;
    var originalCartItems = JSON.parse(localStorage.getItem("cartItems")) || [];

    rows.forEach(function (row, index) {
        var idCell = row.querySelector(".cart__product-id");
        var name = row.querySelector(".cart__product-title h6").textContent;
        var priceText = row.querySelector(".cart__product-price").textContent;
        var price = parseFloat(priceText.replace(/[^0-9.-]+/g, ""));
        var unit = row.querySelector(".cart__product-unit").textContent;
        var quantityInput = row.querySelector(".qty-input");
        var quantity = parseInt(quantityInput.value);
        var image = row.querySelector(".cart__product-img img").src;

        var originalItem = originalCartItems.find(item => item.id == idCell.textContent);
        var defaultPrice = originalItem ? (originalItem.default || originalItem.price) : price;

        subTotal += price * quantity;

        updatedCartItems.push({
            id: idCell.textContent,
            name,
            price: price,
            default: defaultPrice,
            quantity,
            unit,
            image
        });
    });

    localStorage.setItem("cartItems", JSON.stringify(updatedCartItems));
    renderCartTable();
}

document.addEventListener("DOMContentLoaded", function () {
    var cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    var cartTableBody = document.getElementById("cart-table-body");
    var subTotal = 0.0;

    cartItems.forEach(function (item, index) {
        var row = document.createElement("tr");
        row.className = "cart__product";

        subTotal += item.price * item.quantity;

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
      <td class="cart__product-price">${formatPrice(item.price)} VNĐ</td>
      <td class="cart__product-quantity">
        <div class="product-quantity">
          <div class="quantity__input-wrap">
            <i class="fa fa-minus decrease-qty"></i>
            <input type="number" value="${item.quantity}" class="qty-input">
            <i class="fa fa-plus increase-qty"></i>
          </div>
        </div>
      </td>
      <td class="cart__product-unit">${item.unit}</td>
      <td class="cart__product-total">${formatPrice(item.price * item.quantity)} VNĐ</td>
    `;

        cartTableBody.appendChild(row);
    });

    updateTotal(subTotal, subTotal);
});

function validateNameInput(nameInput) {
    if (!nameInput.value.trim()) {
        displayToast("Vui lòng nhập tên khách hàng", "error");
        return false;
    }
    return true;
}

function loadCustomer(customerId) {
    const name = document.getElementById("name");
    var isNameValid = validateNameInput(name);
    if (!isNameValid) return;
    const main = document.getElementById("toast-list");
    if (main) {
        const type = "success";
        const toastElement = document.createElement("div");
        const title = "Notification!";
        const icon = "fas fa-check-circle";

        toastElement.classList.add("toast", `toast--${type}`);
        toastElement.style.animation = `slideInLeft ease .3s forwards`;

        const autoRemoveId = setTimeout(function () {
            main.removeChild(toastElement);
        }, 1500);

        toastElement.onclick = function (e) {
            main.removeChild(toastElement);
        };

        toastElement.innerHTML = `
            <div class="toast__icon">
                <i class="${icon}"></i>
            </div>
            <div class="toast__body">
                <h3 class="toast__title">${title}</h3>
                <p class="toast__msg">${customerId}</p>
            </div>
            <div class="toast__close">
                <i class="fas fa-times"></i>
            </div>
        `;
        main.appendChild(toastElement);
    }
}

function updateTotal(subtotal, total, vatAmount = 0, discountAmount = 0) {
    const vatRate = parseFloat(document.getElementById('vat').value);

    if (isDefaultPriceApplied) {
        let currentSubtotal = 0;
        const cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
        currentSubtotal = cartItems.reduce((total, item) => total + (item.price * item.quantity), 0);

        document.getElementById("subtotal").innerHTML = `
            ${formatPrice(currentSubtotal)} VNĐ
        `;
    } else {
        document.getElementById("subtotal").innerText = `${formatPrice(subtotal)} VNĐ`;
    }

    document.getElementById("vatAmount").innerText = `${formatPrice(vatAmount)} VNĐ (${vatRate}%)`;
    document.getElementById("total").innerText = `${formatPrice(total)} VNĐ`;

    if (!document.getElementById("discount").innerText.includes('%')) {
        document.getElementById("discount").innerText = `${formatPrice(discountAmount)} VNĐ`;
    }
}

function displayToast(message, type) {
    const main = document.getElementById("toast-list");
    if (main) {
        const toastElement = document.createElement("div");
        const title = "Notification!";
        const icon = type === "success" ? "fas fa-check-circle" : "fas fa-exclamation-circle";

        toastElement.classList.add("toast", `toast--${type}`);
        toastElement.style.animation = `slideInLeft ease .3s forwards`;

        const autoRemoveId = setTimeout(function () {
            main.removeChild(toastElement);
        }, 1500);

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

function checkCoupon() {
    if (localStorage.getItem("user") == null) {
        displayToast("Cần đăng nhập để sử dụng mã giảm giá!", "error");
        return;
    }
    var code = document.getElementById('coupon-code');
    if (code.value === "") {
        displayToast("Vui lòng nhập mã giảm giá!", "error");
        return;
    }

    var data = {
        username: localStorage.getItem("user"),
        couponCode: code.value
    };

    var requestOptions = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data),
        redirect: 'follow'
    };

    fetch("/api/v1/auth/validate-coupon", requestOptions)
        .then(response => response.text())
        .then(result => {
            if (result === '0.0') {
                displayToast("Mã giảm giá không hợp lệ!", "error");
                check = true;
                rate = result;
                codeCoupon = '';
                resetCoupon();
            } else if (result === '-1.0') {
                check = true;
                rate = 0;
                codeCoupon = '';
                displayToast("Mã giảm giá đã ngưng hoạt động!", "error");
                resetCoupon();
            } else {
                if (isCouponApplied) {
                    displayToast("Mã giảm giá đã được áp dụng!", "info");
                } else {
                    displayToast("Áp dụng mã giảm giá thành công!", "success");
                    rate = parseFloat(result);
                    if (result == 200) {
                        isDefaultPriceApplied = true;
                        applyDefaultPrice();
                    } else {
                        isDefaultPriceApplied = false;
                        applyDiscount(rate);
                    }
                    isCouponApplied = true;
                    check = true;
                    codeCoupon = code.value;
                    onVatChange(); // Add this at the end
                }
            }
        })
        .catch(error => console.log('error', error));
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

connect();
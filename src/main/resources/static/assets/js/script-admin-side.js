const cartList = document.getElementById("cart-items");
const subtotalElement = document.getElementById("subtotal");
let total = 0;

function addToCart(id, name, price, image) {
    // Lấy danh sách sản phẩm từ localStorage
    let cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];

    // Kiểm tra xem sản phẩm đã tồn tại trong giỏ hàng chưa
    const existingItemIndex = cartItems.findIndex(item => item.id === id);

    if (existingItemIndex !== -1) {
        // Nếu sản phẩm đã tồn tại, cập nhật số lượng
        cartItems[existingItemIndex].quantity += 1;
    } else {
        // Nếu sản phẩm chưa tồn tại, thêm mới vào giỏ hàng
        cartItems.push({
            id: id,
            name: name,
            price: price,
            quantity: 1,
            image: image
        });
    }

    // Lưu danh sách giỏ hàng vào localStorage
    localStorage.setItem("cartItems", JSON.stringify(cartItems));
    const amountItems = document.getElementsByClassName("cart__label")[0];
	amountItems.innerText = JSON.parse(localStorage.getItem("cartItems")).length;

    // Cập nhật và hiển thị giỏ hàng
    renderCart();
}

function showCartAmountItems(item) {
	const amountItems = document.getElementsByClassName("cart__label")[0];
	amountItems.innerText = cartItems.length;
}

const cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
showCartAmountItems(cartItems.length);

function renderCart() {
    // Xóa tất cả mục hiện có trong giỏ hàng
    while (cartList.firstChild) {
        cartList.removeChild(cartList.firstChild);
    }

    // Lấy danh sách sản phẩm từ localStorage
    const cartItems = JSON.parse(localStorage.getItem("cartItems")) || [];
    total = 0;

    cartItems.forEach(function (cartItem) {
        // Tạo một phần tử li cho mỗi mục giỏ hàng
        const cartItemElement = document.createElement("li");
        cartItemElement.className = "cart-item";

        // Tạo phần tử cho ảnh sản phẩm
        const itemImg = document.createElement("div");
        itemImg.className = "cart__item-img";
        const itemImage = document.createElement("img");
        itemImage.src = cartItem.image;
        itemImage.alt = "thumb";
        itemImg.appendChild(itemImage);
        cartItemElement.appendChild(itemImg);

        // Tạo phần tử cho nội dung mục giỏ hàng
        const itemContent = document.createElement("div");
        itemContent.className = "cart__item-content";
        const title = document.createElement("h6");
        title.className = "cart__item-title";
        title.innerText = cartItem.name;
        itemContent.appendChild(title);
        const detail = document.createElement("div");
        detail.className = "cart__item-detail";
        detail.innerText = cartItem.quantity + " X " + "$" + cartItem.price;
        itemContent.appendChild(detail);
        const deleteIcon = document.createElement("i");
        deleteIcon.className = "cart__item-delete";
        deleteIcon.innerText = "×";
        deleteIcon.addEventListener("click", function () {
            // Xóa mục giỏ hàng khi người dùng bấm vào biểu tượng x
            total -= cartItem.quantity * cartItem.price;
            subtotalElement.innerText = "$" + total;

            // Xóa mục khỏi danh sách và cập nhật lại giao diện
            const index = cartItems.findIndex(item => item.id === cartItem.id);
            if (index !== -1) {
                cartItems.splice(index, 1);
                // Lưu danh sách giỏ hàng sau khi xóa
                localStorage.setItem("cartItems", JSON.stringify(cartItems));
                const amountItems = document.getElementsByClassName("cart__label")[0];
				amountItems.innerText = JSON.parse(localStorage.getItem("cartItems")).length;
                renderCart();
            }
        });
        itemContent.appendChild(deleteIcon);

        cartItemElement.appendChild(itemContent);
        cartList.appendChild(cartItemElement);

        // Tính tổng giá trị
        total += cartItem.quantity * cartItem.price;
    });

    // Hiển thị tổng giá trị
    subtotalElement.innerText = "$" + total;
}

// Gọi hàm renderCart khi trang được tải
renderCart();
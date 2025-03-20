var stompClient = null;
var username = null;

function connect(event) {
    username = "ADMIN"

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, onConnected, onError);
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Tell your username to the server
    stompClient.send("/app/register",
        {},
        JSON.stringify({ sender: "user", type: 'JOIN' })
    )
}


function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}

const toastQueue = [];
let isClickingToast = false;

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    if (message.type === 'CHAT') {
        console.log(">> " + message.content + " from " + message.sender);
        showSuccessToast();
    }
}

connect();

const showSuccessToast = () => {
    const toast = {
        title: "Notification!",
        message: `Bạn có ${toastQueue.length} đơn hàng mới.`,
        type: "success"
    };

    if (!isClickingToast) {
        if (toastQueue.length === 0) {
            toastQueue.push(toast);
            displayToast(toast);
        } else {
            toastQueue.push(toast);
            const combinedToast = combineToasts(toastQueue);
            updateToast(combinedToast);
        }
    }
}

function combineToasts(toasts) {
    const combinedToast = {
        title: "Multiple Notifications",
        message: `Bạn có ${toastQueue.length} đơn hàng mới.`,
    };

    return combinedToast;
}

function updateToast(toast) {
    const main = document.getElementById("toast-list");
    const toastElement = main.querySelector(".toast");
    if (toastElement) {
        const messageElement = toastElement.querySelector(".toast__msg");
        messageElement.textContent = toast.message;
    }
}



function displayToast() {
    const main = document.getElementById("toast-list");
    if (main) {
        const toastElement = document.createElement("div");
        const title = "Notification!";
        const message = `Bạn có ${toastQueue.length} đơn hàng mới.`;

        // Remove toast when clicked
        toastElement.onclick = function (e) {
            isClickingToast = true;
            main.removeChild(toastElement);
            toastQueue.pop(); // Xoá toast khỏi hàng đợi
            showNextToast(); // Hiển thị toast tiếp theo nếu có
        };

        const icon = "fas fa-check-circle";
        const type = 'success';

        toastElement.classList.add("toast", `toast--${type}`);
        toastElement.style.animation = `slideInLeft ease .3s forwards`;

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

function showNextToast() {
    if (toastQueue.length > 0) {
        const nextToastCount = toastQueue.length;
        displayToast(nextToastCount);
        
    } else {
        isClickingToast = false; // Không còn toast nào để click
    }
}

function fetchData(username) {
    console.log(username);
}

function editFood(id, name, description, price, categories, image, status) {
    document.getElementById('id').value = id;
    document.getElementById('name').value = name;
    document.getElementById('description').innerText = description;
    document.getElementById('price').value = price;

    // Xử lý danh mục: Bỏ dấu ngoặc vuông và tách thành mảng
    categories = categories.replace("[", "").replace("]", "");
    var categoryMapping = {
        "Sausage": 1,
        "Saurce": 2
    };

    var categoryIds = categories.split(',').map(function(cate) {
        var trimmedCate = cate.trim(); // Loại bỏ khoảng trắng
        return categoryMapping[trimmedCate] || 3; // Nếu không có trong mapping, trả về 3
    });

    // Lấy tất cả radio button của danh mục
    var radioButtons = document.querySelectorAll('input[type="radio"][name="categories"]');
    radioButtons.forEach(function(radio) {
        console.log(categoryIds.includes(radio.value) + " " + categoryIds + " " + radio.value);

        if (categoryIds == radio.value) {
            radio.checked = true; // Đánh dấu radio button đúng
        } else {
            radio.checked = false;
        }
    });

    // Set trạng thái món ăn
    document.getElementById('status').value = status;
}


function editCate(id, cateName, status) {
	document.getElementById('id').value = id;
	document.getElementById('cateName').value = cateName;
}

function editCoupon(id, code, rate, status) {
console.log({id, code, rate, status});
	document.getElementById('id').value = id;
	document.getElementById('code').value = code;
	document.getElementById('rate').value = rate;
}




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

function editFood(id, name, description, price, categories, image, status) {
	document.getElementById('id').value = id;
	document.getElementById('name').value = name;
	document.getElementById('description').innerText = description;
	document.getElementById('price').value = price;
	categories = categories.replace("[", "").replace("]", "");
	
	var categoryNames = categories.split(',').map(function(cate) {
        return cate.trim();
    });
    
    var checkboxes = document.querySelectorAll('input[type="checkbox"]');
    for (var i = 0; i < checkboxes.length; i++) {
        var checkbox = checkboxes[i];
        var label = document.querySelector('label[for="' + checkbox.id + '"]');
        var labelText = label.innerText.trim();

        // Kiểm tra xem tên danh mục có trong danh sách categoryNames không
        if (categoryNames.indexOf(labelText) !== -1) {
            checkbox.checked = true; // Đánh dấu checkbox nếu tên danh mục tương ứng được tìm thấy
        } else {
            checkbox.checked = false; // Bỏ đánh dấu checkbox nếu không tìm thấy
        }
    }
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




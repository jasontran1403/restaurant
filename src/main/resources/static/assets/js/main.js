/*---------------------------
      Table of Contents
    --------------------
    
    01- Mobile Menu
    02- Sticky Navbar
    03- Search Popup
    04- Cart Popup
    05- Scroll Top Button
    06- Set Background-img to section 
    07- Increase and Decrease Input Value
    08- Add active class to accordions
    09- Switch Between List view and Grid View
    10- Owl Carousel
    11- Products Filtering and Sorting
    12- Range Slider
    13- lightbox Gallery 
    14- NiceSelect Plugin
    15- image zoomsl Plugin 
    16- AOS Animation Plugin
    
 ----------------------------*/

$(function () {

    // Global variables
    var $win = $(window);

    /*==========   Mobile Menu   ==========*/
    var $navToggler = $('.navbar-toggler');
    $navToggler.on('click', function () {
        $(this).toggleClass('actived');
    })
    $navToggler.on('click', function () {
        $('.navbar-collapse').toggleClass('menu-opened');
    })

    // Toggle dropdown Menu in Mobile
    $('.dropdown-menu [data-toggle=dropdown]').on('click', function (e) {
        e.stopPropagation();
        e.preventDefault();
        $(this).parent().siblings().removeClass("opened");
        $(this).parent().toggleClass("opened");
    });
    $('.dropdown-submenu [data-toggle=dropdown]').on('click', function (e) {
        $(this).next().toggleClass("show");
        $(this).parent().siblings().find('.dropdown-menu').removeClass('show');
    });

    /*==========   Sticky Navbar   ==========*/
    $win.on('scroll', function () {
        if ($win.width() >= 992) {
            var $navbar = $('.sticky-navbar');
            if ($win.scrollTop() > 100) {
                $navbar.addClass('fixed-navbar');
            } else {
                $navbar.removeClass('fixed-navbar');
            }
        }
    });

    /*==========  Search Popup ==========*/
    var $moduleBtnSearch = $('.search-popup-trigger'),
        $searchPopup = $('.search-popup');
    // Show Module Search
    $moduleBtnSearch.on('click', function (e) {
        e.preventDefault();
        $searchPopup.toggleClass('active', 'inActive').removeClass('inActive');
    });
    // Close Module Search
    $('.close-search').on('click', function () {
        $searchPopup.removeClass('active').addClass('inActive');
    });

    /*==========  Cart Popup   ==========*/
    var $cartPopup = $('.cart-popup');
    // show module cart
    $('.navbar__action-btn-cart').on('click', function (e) {
        e.preventDefault();
        $cartPopup.toggleClass('active');
    });
    // Close Module Cart
    $('.close-cart').on('click', function () {
        $cartPopup.removeClass('active');
    });
    $(document).on('mouseup', function (e) {
        if (!$cartPopup.is(e.target) && !$('.navbar__action-btn-cart').is(e.target) && $cartPopup.has(e.target).length === 0 && $cartPopup.has(e.target).length === 0) {
            $cartPopup.removeClass('active');
        }
    });

    /*==========   Scroll Top Button   ==========*/
    var $scrollTopBtn = $('#scrollTopBtn');
    // Show Scroll Top Button
    $win.on('scroll', function () {
        if ($(this).scrollTop() > 700) {
            $scrollTopBtn.addClass('actived');
        } else {
            $scrollTopBtn.removeClass('actived');
        }
    });
    // Animate Body after Clicking on Scroll Top Button
    $scrollTopBtn.on('click', function () {
        $('html, body').animate({
            scrollTop: 0
        }, 500);
    });

    /*==========   Set Background-img to section   ==========*/
    $('.bg-img').each(function () {
        var imgSrc = $(this).children('img').attr('src');
        $(this).parent().css({
            'background-image': 'url(' + imgSrc + ')',
            'background-size': 'cover',
            'background-position': 'center',
        });
        $(this).parent().addClass('bg-img');
        $(this).remove();
    });

    /*==========   Increase and Decrease Input Value   ==========*/
    // Increase Value
    $('.increase-qty').on('click', function () {
        var $qty = $(this).parent().find('.qty-input');
        var currentVal = parseInt($qty.val());
        if (!isNaN(currentVal)) {
            $qty.val(currentVal + 1);
        }
    });
    // Decrease Value
    $('.decrease-qty').on('click', function () {
        var $qty = $(this).parent().find('.qty-input');
        var currentVal = parseInt($qty.val());
        if (!isNaN(currentVal) && currentVal > 1) {
            $qty.val(currentVal - 1);
        }
    });

    /*==========   Add active class to accordions   ==========*/
    $('.accordion__item-header').on('click', function () {
        $(this).parent('.accordion-item').addClass('opened');
        $(this).parent('.accordion-item').siblings().removeClass('opened');
    })
    $('.accordion__item-title').on('click', function (e) {
        e.preventDefault()
    });

    /*==========   Switch Between List view and Grid View   ==========*/
    $('.filter-option-view a').on('click', function (e) {
        e.preventDefault()
        $(this).addClass('active').siblings().removeClass('active');
    })
    $('#listView').on('click', function (e) {
        $('.product-item').parent().addClass('list-view');
    });
    $('#gridView').on('click', function (e) {
        $('.product-item').parent().removeClass('list-view');
    });

    /*==========   Owl Carousel  ==========*/
    $('.carousel').each(function () {
        $(this).owlCarousel({
            nav: $(this).data('nav'),
            dots: $(this).data('dots'),
            loop: $(this).data('loop'),
            margin: $(this).data('space'),
            center: $(this).data('center'),
            dotsSpeed: $(this).data('speed'),
            autoplay: $(this).data('autoplay'),
            transitionStyle: $(this).data('transition'),
            animateOut: $(this).data('animate-out'),
            animateIn: $(this).data('animate-in'),
            autoplayTimeout: 15000,
            responsive: {
                0: {
                    items: 1,
                },
                400: {
                    items: $(this).data('slide-sm'),
                },
                700: {
                    items: $(this).data('slide-md'),
                },
                1000: {
                    items: $(this).data('slide'),
                }
            }
        });
    });

    /*==========   Products Filtering and Sorting  ==========*/
    $(".filtered-items-wrap").mixItUp();
    $(".portfolio-filter li a").on('click', function (e) {
        e.preventDefault();
    });

    $('.loadMoreportfolio').on('click', function (e) {
        e.preventDefault();
        $('.portfolio-hidden > .portfolio-item').fadeIn();
        $(this).fadeOut();
    });

    /*==========   Range Slider  ==========*/
    var $rangeSlider = $("#rangeSlider"),
        $rangeSliderResult = $("#rangeSliderResult");
    $rangeSlider.slider({
        range: true,
        min: 0,
        max: 300,
        values: [50, 200],
        slide: function (event, ui) {
            $rangeSliderResult.val("$" + ui.values[0] + " - $" + ui.values[1]);
        }
    });
    $rangeSliderResult.val("$" + $rangeSlider.slider("values", 0) + " - $" + $rangeSlider.slider("values", 1));

    /*==========   lightbox PLugin  ==========*/
    lightbox.option({
        fadeDuration: 300
    });

    /*==========  NiceSelect Plugin  ==========*/
    $('select').niceSelect();

    /*==========  image zoomsl Plugin  ==========*/
    $(".zoomin").imagezoomsl();

    /*==========  AOS Animation Plugin  ==========*/
    AOS.init({ duration: 800 });

    /*==========  Contact Form validation  ==========*/
    var contactForm = $("#contactForm"),
        contactResult = $('.contact-result');
    contactForm.validate({
        debug: false,
        select: {
            required: function (element) {
                if ($("select").val() == '-1') {
                    return false;
                } else {
                    return true;
                }
            }
        },
        submitHandler: function (contactForm) {
            $(contactResult, contactForm).html('Please Wait...');
            $.ajax({
                type: "POST",
                url: "assets/php/contact.php",
                data: $(contactForm).serialize(),
                timeout: 20000,
                success: function (msg) {
                    $(contactResult, contactForm).html('<div class="alert alert-success" role="alert"><strong>Thank you. We will contact you shortly.</strong></div>').delay(3000).fadeOut(2000);
                },
                error: $('.thanks').show()
            });
            return false;
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    var loginLink = document.getElementById("login-link");
    var userNameSpan = document.getElementById("user-name");

    // Kiểm tra xem trong localStorage có thông tin user không
    var user = localStorage.getItem("user");

    if (user) {
        // Nếu có user, hiển thị tên người dùng và ẩn liên kết "Đăng nhập"
        userNameSpan.style.display = "inline";
        userNameSpan.textContent = user;  // Hiển thị tên người dùng
        loginLink.style.display = "none"; // Ẩn nút Đăng nhập

        // Thêm sự kiện cho tên người dùng (khi click sẽ đăng xuất)
        userNameSpan.addEventListener("click", function() {
            // Xóa toàn bộ localStorage khi click vào tên người dùng
            localStorage.clear(); // Xóa tất cả dữ liệu trong localStorage
            location.reload(); // Tải lại trang để cập nhật giao diện
        });

    } else {
        // Nếu không có user, hiển thị nút Đăng nhập và ẩn tên người dùng
        userNameSpan.style.display = "none";
        loginLink.style.display = "inline"; // Hiển thị nút Đăng nhập
    }

    // Sự kiện khi nhấn vào link Đăng nhập (hiển thị modal đăng nhập)
    loginLink.addEventListener('click', function() {
        document.getElementById("loginModal").style.display = "block";
    });

    const loginModal = document.getElementById('loginModal');
    const showLoginModalBtn = document.querySelector('.js-show-login-modal');
    const closeLoginModalBtn = document.getElementById('closeLoginModal');

    // Hiển thị modal
    showLoginModalBtn.addEventListener('click', () => {
        loginModal.style.display = 'flex';
    });

    // Ẩn modal
    closeLoginModalBtn.addEventListener('click', () => {
        loginModal.style.display = 'none';
    });

    // Đóng modal khi nhấn ra ngoài modal container
    loginModal.addEventListener('click', (e) => {
        if (e.target === loginModal) {
            loginModal.style.display = 'none';
        }
    });

    // Xử lý form đăng nhập
    const loginForm = document.getElementById('btn-login');
    loginForm.addEventListener('click', (e) => {
        e.preventDefault(); // Ngăn hành động mặc định

        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;

        const myHeaders = new Headers();
        myHeaders.append("Content-Type", "application/json");

        const raw = JSON.stringify({
            "username": username,
            "password": password
        });

        const requestOptions = {
            method: "POST",
            headers: myHeaders,
            body: raw,
            redirect: "follow"
        };

        fetch("/api/v1/auth/login", requestOptions)
            .then((response) => response.json())
            .then((result) => {
                if (result.message === "Agency is not found!" || result.message === "Invalid username or password") {
                    displayToast(result.message, "error");
                } else {
                    displayToast("Đăng nhập thành công!", "success");
                    localStorage.setItem("user", result.message);
                    loginModal.style.display = 'none';
                }
            })
            .catch((error) => console.error(error));
    });

    function displayToast(message, type) {
        const main = document.getElementById("toast-list");
        if (main) {

            const toastElement = document.createElement("div");

            const title = "Notification!";

            const icon = "fas fa-check-circle";

            toastElement.classList.add("toast", `toast--${type}`);
            toastElement.style.animation = `slideInLeft ease .3s forwards`;

            const autoRemoveId = setTimeout(function () {
                if (type === "success") window.location.reload();
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

});

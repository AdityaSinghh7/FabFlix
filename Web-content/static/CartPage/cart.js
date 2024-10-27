$(document).ready(function() {
    loadCart();


    function loadCart() {
        let cart = JSON.parse(sessionStorage.getItem('cart')) || {};
        let cartItemsContainer = $('#cart-items-container');
        cartItemsContainer.empty();
        let totalPrice = 0;
        Object.keys(cart).forEach(movieId => {
            let movie = cart[movieId];
            let itemTotal = movie.price * movie.quantity;
            totalPrice += itemTotal;

            cartItemsContainer.append(`
                <div class="cart-item">
                    <span>${movie.title} - $${movie.price.toFixed(2)} x ${movie.quantity}</span>
                    <div>
                        <button onclick="modifyQuantity('${movieId}', 1)">+</button>
                        <button onclick="modifyQuantity('${movieId}', -1)">-</button>
                        <button onclick="removeFromCart('${movieId}')">Remove</button>
                    </div>
                    <span>Total: $${itemTotal.toFixed(2)}</span>
                </div>
            `);
        });

        $('#total-price').text(totalPrice.toFixed(2));
    }


    window.modifyQuantity = function(movieId, change) {
        let cart = JSON.parse(sessionStorage.getItem('cart')) || {};
        if (cart[movieId]) {
            cart[movieId].quantity += change;
            if (cart[movieId].quantity <= 0) {
                delete cart[movieId];
            }
        }
        sessionStorage.setItem('cart', JSON.stringify(cart));
        loadCart();
    }


    window.removeFromCart = function(movieId) {
        let cart = JSON.parse(sessionStorage.getItem('cart')) || {};
        delete cart[movieId];
        sessionStorage.setItem('cart', JSON.stringify(cart));
        loadCart();
    }


    $('#proceed-to-payment').click(function() {
        window.location.href = 'paymentPage.html';
    });
});

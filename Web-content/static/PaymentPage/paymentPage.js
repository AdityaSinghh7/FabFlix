$(document).ready(function() {
    const totalPrice = sessionStorage.getItem('totalPrice');
    console.log(sessionStorage.getItem('totalPrice'))
    $('#total-price').text(totalPrice);

    $('#payment-form').on('submit', function(event) {
        event.preventDefault();

        const firstName = $('#first-name').val().trim();
        const lastName = $('#last-name').val().trim();
        const cardNumber = $('#card-number').val().trim();
        const expirationDate = $('#expiration-date').val();
        const cart = JSON.parse(sessionStorage.getItem('cart')) || {};
        const customerId = sessionStorage.getItem('customerId');

        if (!firstName || !lastName || !expirationDate) {
            showErrorPopup('Please fill out all fields correctly.');
            return;
        }



        if (firstName && lastName && expirationDate) {
            $.ajax({
                url: '../../api/place-order',
                method: 'POST',
                contentType: 'application/x-www-form-urlencoded',
                data: {
                    firstName: firstName,
                    lastName: lastName,
                    cardNumber: cardNumber,
                    expirationDate: expirationDate,
                    cart: JSON.stringify(cart),
                    customerId: customerId,
                    totalPrice: totalPrice
                },
                success: function(response) {
                    if (response.status === 'success') {
                        showSuccessPopup();
                        sessionStorage.removeItem('cart');
                        sessionStorage.removeItem('totalPrice');
                    } else {
                        showErrorPopup(response.message);
                    }
                },
                error: function() {
                    showErrorPopup('Transaction failed. Please try again.');
                }
            });
        } else {
            alert('Please fill out all fields correctly.');
        }
    });

    function showSuccessPopup() {
        $('#success-popup').fadeIn();
    }

    function showErrorPopup(message) {
        $('#error-message').text(message);
        $('#error-popup').fadeIn();
    }


    $('#close-popup, #close-error-popup').on('click', function() {
        $(this).closest('.popup-container').fadeOut(function() {
            if ($(this).attr('id') === 'success-popup') {
                window.location.href = '../MainPage/mainPage.html';
            }
        });
    });
});

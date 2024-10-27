function getParameterByName(name) {
    name = name.replace(/[\[\]]/g, '\\$&');
    let url = window.location.href;
    let results = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)').exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

let movieId = getParameterByName('movieId');

console.log(movieId);

if (movieId) {
    fetch('/FabFlix_war/api/movie?movieId=' + encodeURIComponent(movieId))
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                document.body.innerHTML = '<p>' + data.error + '</p>';
            } else {
                document.getElementById('movie-title').textContent = data.title;
                document.getElementById('movie-year').textContent = data.year;
                document.getElementById('movie-director').textContent = data.director;
                document.getElementById('movie-genres').textContent = data.genres;
                document.getElementById('movie-rating').textContent = data.rating;
                document.getElementById('movie-price').textContent = data.price.toFixed(2);

                let starsList = document.getElementById('movie-stars');
                data.stars.forEach(star => {
                    let li = document.createElement('li');
                    let a = document.createElement('a');
                    a.href = '../SingleStar/singleStar.html?starId=' + encodeURIComponent(star.id);
                    a.textContent = star.name;
                    li.appendChild(a);
                    starsList.appendChild(li);
                });
                let genresHTML = '';
                data.genres.split(', ').forEach(genre => {
                    genresHTML += `<a href='../Browse/browseByGenre.html?genre=${encodeURIComponent(genre)}'>${genre}</a>, `;
                });
                genresHTML = genresHTML.slice(0, -2);
                document.getElementById('movie-genres').innerHTML = genresHTML;

                document.getElementById('add-to-cart-button').addEventListener('click', function() {
                    addToCart(data.movieId, data.title, data.price);
                });
            }
        })
        .catch(error => {
            document.body.innerHTML = '<p>Error fetching movie data: ' + error + '</p>';
        });
} else {
    document.body.innerHTML = '<p>No movieId specified in URL.</p>';
}

function addToCart(movieId, title, price) {
    let cart = JSON.parse(sessionStorage.getItem('cart')) || {};
    if (!cart[movieId]) {
        cart[movieId] = { title: title, price: price, quantity: 1 };
    } else {
        cart[movieId].quantity++;
    }
    sessionStorage.setItem('cart', JSON.stringify(cart));
    alert(`${title} added to cart!`);
}
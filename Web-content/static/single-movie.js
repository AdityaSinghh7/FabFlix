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
    fetch('../api/movie?movieId=' + encodeURIComponent(movieId))
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

                let starsList = document.getElementById('movie-stars');
                data.stars.forEach(star => {
                    let li = document.createElement('li');
                    let a = document.createElement('a');
                    a.href = 'single-star.html?starId=' + encodeURIComponent(star.id);
                    a.textContent = star.name;
                    li.appendChild(a);
                    starsList.appendChild(li);
                });
            }
        })
        .catch(error => {
            document.body.innerHTML = '<p>Error fetching movie data: ' + error + '</p>';
        });
} else {
    document.body.innerHTML = '<p>No movieId specified in URL.</p>';
}
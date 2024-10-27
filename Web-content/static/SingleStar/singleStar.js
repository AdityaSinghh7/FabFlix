function getParameterByName(name) {
    name = name.replace(/[\[\]]/g, '\\$&');
    let url = window.location.href;
    let results = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)').exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

let starId = getParameterByName('starId');

if (starId) {
    // Adjust the fetch URL according to your application's context path
    fetch('/FabFlix_war/api/star?starId=' + encodeURIComponent(starId))
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json();
        })
        .then(data => {
            if (data.error) {
                document.body.innerHTML = '<p>' + data.error + '</p>';
            } else {
                document.getElementById('star-name').textContent = data.name;
                document.getElementById('star-birthYear').textContent = data.birthYear || 'N/A';

                let moviesList = document.getElementById('star-movies');
                data.movies.forEach(movie => {
                    let li = document.createElement('li');
                    let a = document.createElement('a');
                    a.href = '../SingleMovie/singleMovie.html?movieId=' + encodeURIComponent(movie.id);
                    a.textContent = movie.title;
                    li.appendChild(a);
                    moviesList.appendChild(li);
                });
            }
        })
        .catch(error => {
            console.error('Error fetching star data:', error);
            document.body.innerHTML = '<p>Error fetching star data: ' + error + '</p>';
        });
} else {
    document.body.innerHTML = '<p>No starId specified in URL.</p>';
}
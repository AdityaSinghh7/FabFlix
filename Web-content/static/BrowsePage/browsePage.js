$(document).ready(function() {
    $.ajax({
        url: '../../api/genres',
        type: 'GET',
        dataType: 'json',
        success: function(genres) {
            const genreList = $('#genre-list');
            genres.forEach(genre => {
                const li = $('<li></li>');
                const a = $('<a></a>')
                    .text(genre.name)
                    .attr('href', '#')
                    .click(function() {

                        sessionStorage.setItem('browseFlag', 'genre');
                        sessionStorage.setItem('genre', genre.name);
                        sessionStorage.removeItem('titleStart');
                        sessionStorage.setItem('pageNumber', '1');
                        sessionStorage.setItem('sortBy', 'titleThenRatingAsc');


                        window.location.href = '../MovieList/movieList.html';
                    });
                li.append(a);
                genreList.append(li);
            });
        },
        error: function() {
            console.error('Error fetching genres');
        }
    });

    const titleList = $('#title-list');
    const characters = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*'.split('');
    characters.forEach(char => {
        const li = $('<li></li>');
        const a = $('<a></a>')
            .text(char)
            .attr('href', '#')
            .click(function() {
                sessionStorage.setItem('browseFlag', 'title');
                sessionStorage.setItem('titleStart', char);
                sessionStorage.removeItem('genre');
                sessionStorage.setItem('pageNumber', '1');
                sessionStorage.setItem('sortBy', 'titleThenRatingAsc');

                window.location.href = '../MovieList/movieList.html';
            });
        li.append(a);
        titleList.append(li);
    });
});
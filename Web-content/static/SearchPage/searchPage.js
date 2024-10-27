$(document).ready(function() {
    $('#logout-button').on('click', function() {
        $.ajax({
            url: '/FabFlix_war/api/logout',
            type: 'POST',
            success: function() {
                window.location.href = '../LoginPage/loginPage.html';
            },
            error: function() {
                alert('Failed to log out. Please try again.');
            }
        });
    });
});

function redirectToMovieList() {

    const title = $('#title').val().trim();
    const year = $('#year').val().trim();
    const director = $('#director').val().trim();
    const star = $('#star').val().trim();


    if (title !== "") {
        sessionStorage.setItem('title', title);
    } else {
        sessionStorage.removeItem('title');
    }

    if (year !== "" && !isNaN(year) && parseInt(year) > 0) {
        sessionStorage.setItem('year', year);
    } else {
        sessionStorage.removeItem('year');
    }

    if (director !== "") {
        sessionStorage.setItem('director', director);
    } else {
        sessionStorage.removeItem('director');
    }

    if (star !== "") {
        sessionStorage.setItem('star', star);
    } else {
        sessionStorage.removeItem('star');
    }


    const pageNumber = 1;
    const sortBy = 'titleThenRatingAsc';

    sessionStorage.setItem('pageNumber', pageNumber.toString());
    sessionStorage.setItem('sortBy', sortBy);


    window.location.href = '../MovieList/movieList.html';
}

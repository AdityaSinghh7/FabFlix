let currentPage = parseInt(sessionStorage.getItem('pageNumber')) || 1;
let sortBy = sessionStorage.getItem('sortBy') || 'titleThenRatingAsc';
const pageSize = 15;

const title = sessionStorage.getItem('title') || "";
const year = sessionStorage.getItem('year') || "";
const director = sessionStorage.getItem('director') || "";
const star = sessionStorage.getItem('star') || "";
const yearInt = year ? parseInt(year) : "";

function fetchMovies(){
    const url = '/FabFlix_war/api/movies';

    jQuery.ajax({
        url: url,
        dataType: "json",
        method: "GET",
        data: {
            title: title,
            year: yearInt,
            director: director,
            star: star,
            page: currentPage,
            sort: sortBy,
            queriesPerPage: pageSize
        },
        success: (resultData) => {
            handleMovieListResult(resultData);
            updatePaginationControls(resultData.length);
        },
        error: () => {
            alert("Error fetching movies. Please try again!");
        }
    });
}

function handleMovieListResult(resultData){
    console.log("handleMovieListResult: populating...")

    let movieTableBody = jQuery("#movie_table_body");
    movieTableBody.empty();

    if (resultData.length === 0) {
        movieTableBody.append("<tr><td colspan='6'>0 valid results found</td></tr>");
        return;
    }

    for(let i = 0; i < resultData.length; i++){
        let movie = resultData[i];
        let movieId = movie["movieId"];
        let title = movie["title"];
        let year = movie["year"];
        let director = movie["director"];
        let genres = movie["genres"].split(', ');
        let starsArray = movie["stars"];

        let genresHTML = genres.slice(0, 3).join(', ');


        let starsHTML = '';
        starsArray.slice(0, 3).forEach(star => {
            starsHTML += `<a href='../singleStar.html?starId=${encodeURIComponent(star.id)}'>${star.name}</a>, `;
        });

        starsHTML = starsHTML.slice(0, -2);


        let rowHTML = `
            <tr>
                <td><a href='../singleMovie.html?movieId=${encodeURIComponent(movieId)}'>${title}</a></td>
                <td>${year}</td>
                <td>${director}</td>
                <td>${genresHTML}</td>
                <td>${starsHTML}</td>
                <td>${movie["rating"]}</td>
            </tr>
        `;

        movieTableBody.append(rowHTML);
    }
}

function handleSortChange(event) {
    sortBy = event.target.value;
    sessionStorage.setItem('sortBy', sortBy);
    currentPage = 1;
    sessionStorage.setItem('pageNumber', currentPage);
    fetchMovies();
}

function handlePageChange(nextPage) {
    if (nextPage >= 1) {
        currentPage = nextPage;
        sessionStorage.setItem('pageNumber', currentPage);
        fetchMovies();
    }
}

function updatePaginationControls(resultCount){
    const paginationControls = jQuery('.pagination-controls');
    paginationControls.empty();

    let prevButton = `<button id="prev-button" ${currentPage === 1 ? 'disabled' : ''}>Previous</button>`;
    let nextButton = `<button id="next-button" ${resultCount < pageSize ? 'disabled' : ''}>Next</button>`;

    paginationControls.append(prevButton);
    paginationControls.append(`<span>Page ${currentPage}</span>`);
    paginationControls.append(nextButton);

    jQuery('#prev-button').click(() => handlePageChange(currentPage - 1));
    jQuery('#next-button').click(() => handlePageChange(currentPage + 1));
}

function init(){
    let sortDropdown = `
        <select id="sort-dropdown">
            <option value="titleAscThenRatingAsc">Title Asc, Rating Asc</option>
            <option value="titleAscThenRatingDesc">Title Asc, Rating Desc</option>
            <option value="titleDescThenRatingAsc">Title Desc, Rating Asc</option>
            <option value="titleDescThenRatingDesc">Title Desc, Rating Desc</option>
            <option value="ratingAscThenTitleAsc">Rating Asc, Title Asc</option>
            <option value="ratingAscThenTitleDesc">Rating Asc, Title Desc</option>
            <option value="ratingDescThenTitleAsc">Rating Desc, Title Asc</option>
            <option value="ratingDescThenTitleDesc">Rating Desc, Title Desc</option>
        </select>
    `;
    jQuery('body').prepend(sortDropdown);
    jQuery('#sort-dropdown').val(sortBy).change(handleSortChange);

    let paginationControls = `<div class="pagination-controls"></div>`;
    jQuery('body').append(paginationControls);
    jQuery('#prev-button').click(() => handlePageChange(currentPage - 1));
    jQuery('#next-button').click(() => handlePageChange(currentPage + 1));
    fetchMovies();
}

$(document).ready(function() {
    init();
});
let currentPage = parseInt(sessionStorage.getItem('pageNumber')) || 1;
let sortBy = sessionStorage.getItem('sortBy') || 'titleThenRatingAsc';
let pageSize = parseInt(sessionStorage.getItem('pageSize')) || 10;
const title = sessionStorage.getItem('title') || "";
const year = sessionStorage.getItem('year') || "";
const director = sessionStorage.getItem('director') || "";
const star = sessionStorage.getItem('star') || "";
const yearInt = year ? parseInt(year) : "";
const browseFlag = sessionStorage.getItem('browseFlag') || "";
const genre = sessionStorage.getItem('genre') || "";
const titleStart = sessionStorage.getItem('titleStart') || "";
const fullTextSearch = sessionStorage.getItem('fullTextSearch') === 'true';


function fetchMovies(){
    const url = '../../api/movies';

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
            queriesPerPage: pageSize,
            browseFlag: browseFlag,
            genre: genre,
            titleStart: titleStart,
            fullTextSearch: fullTextSearch
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

function handlePageSizeChange() {
    const selectedPageSize = parseInt($('#page-size-dropdown').val());
    if (selectedPageSize !== pageSize) {
        pageSize = selectedPageSize;
        sessionStorage.setItem('pageSize', pageSize);
        currentPage = 1;
        sessionStorage.setItem('pageNumber', currentPage);
        fetchMovies();
    }
}

function handleMovieListResult(resultData){
    // console.log("handleMovieListResult: populating...")

    let movieTableBody = jQuery("#movie_table_body");
    movieTableBody.empty();

    if (resultData.length === 0) {
        movieTableBody.append("<tr><td colspan='7'>0 valid results found</td></tr>");
        return;
    }

    for(let i = 0; i < resultData.length; i++){
        let movie = resultData[i];
        let movieId = movie["movieId"];
        let title = movie["title"];
        let year = movie["year"];
        let director = movie["director"];
        let genres = movie["genres"] ? movie["genres"].split(', ') : [];
        let starsArray = movie["stars"];
        let price = movie["price"].toFixed(2);

        let genresHTML = '';
        genres.slice(0, 3).forEach(genre => {
            genresHTML += `<a href='#' onclick='browseByGenre("${genre}")'>${genre}</a>, `;
        });
        genresHTML = genresHTML.slice(0, -2);


        let starsHTML = '';
        starsArray.slice(0, 3).forEach(star => {
            starsHTML += `<a href='../SingleStar/singleStar.html?starId=${encodeURIComponent(star.id)}'>${star.name}</a>, `;
        });

        starsHTML = starsHTML.slice(0, -2);


        let rowHTML = `
            <tr>
                <td><a href='../SingleMovie/singleMovie.html?movieId=${encodeURIComponent(movieId)}'>${title}</a></td>
                <td>${year}</td>
                <td>${director}</td>
                <td>${genresHTML}</td>
                <td>${starsHTML}</td>
                <td>${movie["rating"]}</td>
                <td><button class="add-to-cart" onclick='addToCart("${movieId}", "${title}", ${price})'>Add to Cart</button></td>
            </tr>
        `;

        movieTableBody.append(rowHTML);
    }
}

function browseByGenre(genre) {
    sessionStorage.setItem('browseFlag', 'genre');
    sessionStorage.setItem('genre', genre);
    sessionStorage.removeItem('title');
    sessionStorage.removeItem('year');
    sessionStorage.removeItem('director');
    sessionStorage.removeItem('star');
    sessionStorage.setItem('pageNumber', '1');
    sessionStorage.setItem('sortBy', 'titleThenRatingAsc');
    window.location.href = '../MovieList/movieList.html';
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
        sessionStorage.setItem('fullTextSearch', fullTextSearch);
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

    $('#page-size-dropdown').val(pageSize);
    $('#page-size-dropdown').change(handlePageSizeChange);
    fetchMovies();
}

$(document).ready(function() {
    init();
});
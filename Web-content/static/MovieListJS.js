function handleMovieListResult(resultData){
    console.log("handleMovieListResult: populating...")

    let movieTableBody = jQuery("#movie_table_body");

    console.log("Result Data:", resultData);

    for(let i = 0; i < Math.min(20, resultData.length); i++){
        console.log("Movie Data:", resultData[i]);
        let movieId = resultData[i]["movieId"];
        let title = resultData[i]["title"];
        let starsArray = resultData[i]["stars"];
        let starsHTML = '';

        starsArray.forEach(star => {
            starsHTML += "<a href='static/single-star.html?starId=" + encodeURIComponent(star.id) + "'>" + star.name + "</a>, ";
        });



        console.log("Movie ID:", movieId);
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<td><a href='static/SingleMovie.html?movieId=" + encodeURIComponent(movieId) + "'>" + title + "</a></td>";
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";
        rowHTML += "<td>" + resultData[i]["genres"] + "</td>";
        rowHTML += "<td>" + starsHTML + "</td>";
        rowHTML += "<td>" + resultData[i]["rating"] + "</td>";
        rowHTML += "</tr>";

        movieTableBody.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    success: (resultData) => {
        console.log(resultData);
        handleMovieListResult(resultData);
    }
});
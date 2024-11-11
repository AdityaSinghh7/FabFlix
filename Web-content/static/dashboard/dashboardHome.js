document.addEventListener('DOMContentLoaded', function(){
    const insertStarButton = document.getElementById('insert-star-button');
    const insertStarPopup = document.getElementById('insert-star-popup');
    const addStarForm = document.getElementById('add-star-form');
    const confirmationPopup = document.getElementById('confirmation-popup');
    const confirmationMessage = document.getElementById('confirmation-message');
    const cancelButtons = document.querySelectorAll('.cancel-button');

    insertStarButton.addEventListener('click', function() {
        insertStarPopup.style.display = 'flex';
    });

    addStarForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const starName = document.getElementById('new-star-name').value;
        const birthYear = document.getElementById('birth-year').value;

        $.ajax({
            type: 'POST',
            url: '../../api/insertStar',
            data: { starName: starName, birthYear: birthYear },
            success: function(response) {
                if (response.status === 'success') {
                    confirmationMessage.innerHTML = `
                        <p>Star added successfully!</p>
                        <p><strong>Name:</strong> ${starName}</p>
                        ${birthYear ? `<p><strong>Birth Year:</strong> ${birthYear}</p>` : ''}
                        <p><strong>ID:</strong> ${response.starId}</p>
                    `;
                } else {
                    confirmationMessage.innerHTML = `<p>Error: ${response.message}</p>`;
                }
                insertStarPopup.style.display = 'none';
                confirmationPopup.style.display = 'flex';
            },
            error: function() {
                confirmationMessage.innerHTML = `<p>An error occurred while adding the star.</p>`;
                insertStarPopup.style.display = 'none';
                confirmationPopup.style.display = 'flex';
            }
        });
    });

    const viewMetadataButton = document.getElementById('view-metadata-button');
    const metadataPopup = document.getElementById('metadata-popup');
    const metadataContent = document.getElementById('metadata-content');

    viewMetadataButton.addEventListener('click', function() {
        $.ajax({
            type: 'GET',
            url: '../../api/fetchMeta',
            success: function(response) {
                console.log("Response received:", response); // Add this line
                let htmlContent = '';
                if (response.tables && Array.isArray(response.tables)) {
                    response.tables.forEach(table => {
                        htmlContent += `<h3>Table: ${table.name}</h3>`;
                        htmlContent += '<table><tr><th>Column</th><th>Type</th></tr>';
                        table.columns.forEach(column => {
                            htmlContent += `<tr><td>${column.name}</td><td>${column.type}</td></tr>`;
                        });
                        htmlContent += '</table>';
                    });
                } else {
                    htmlContent = "<p>No tables found or invalid response structure.</p>";
                }
                metadataContent.innerHTML = htmlContent;
                metadataPopup.style.display = 'flex';
            },
            error: function() {
                metadataContent.innerHTML = `<p>An error occurred while retrieving metadata.</p>`;
                metadataPopup.style.display = 'flex';
            }
        });
    });

    const addMovieButton = document.getElementById('add-movie-button');
    const addMoviePopup = document.getElementById('add-movie-popup');
    const addMovieForm = document.getElementById('add-movie-form');

    addMovieButton.addEventListener('click', function() {
        addMoviePopup.style.display = 'flex';
    });

    addMovieForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const movieTitle = document.getElementById('movie-title').value.trim();
        const movieYear = document.getElementById('movie-year').value.trim();
        const movieDirector = document.getElementById('movie-director').value.trim();
        const starName = document.getElementById('star-name').value.trim();
        const starBirthYear = document.getElementById('star-birth-year').value.trim();
        const genreName = document.getElementById('genre-name').value.trim();

        // Validate inputs
        if (!movieTitle || !movieYear || !movieDirector || !starName || !genreName) {
            confirmationMessage.innerHTML = `<p>Error: All required fields must be filled out.</p>`;
            confirmationPopup.style.display = 'flex';
            return;
        }

        $.ajax({
            type: 'POST',
            url: '../../api/addMovie',
            data: {
                movieTitle: movieTitle,
                movieYear: movieYear,
                movieDirector: movieDirector,
                starName: starName,
                starBirthYear: starBirthYear,
                genreName: genreName
            },
            success: function(response) {
                if (response.status === 'success') {
                    confirmationMessage.innerHTML = `
                        <p>${response.message}</p>
                        <p><strong>Movie ID:</strong> ${response.movieId}</p>
                        <p><strong>Star ID:</strong> ${response.starId}</p>
                        <p><strong>Genre ID:</strong> ${response.genreId}</p>
                    `;
                    addMovieForm.reset();
                } else {
                    confirmationMessage.innerHTML = `<p>Error: ${response.message}</p>`;
                }
                addMoviePopup.style.display = 'none';
                confirmationPopup.style.display = 'flex';
            },
            error: function() {
                confirmationMessage.innerHTML = `<p>An error occurred while adding the movie.</p>`;
                addMoviePopup.style.display = 'none';
                confirmationPopup.style.display = 'flex';
            }
        });
    });

    cancelButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            insertStarPopup.style.display = 'none';
            metadataPopup.style.display = 'none';
            confirmationPopup.style.display = 'none';
            addMoviePopup.style.display = 'none';
        });
    });

    window.addEventListener('click', function(event) {
        if (event.target === insertStarPopup) {
            insertStarPopup.style.display = 'none';
        }
        if (event.target === metadataPopup) {
            metadataPopup.style.display = 'none';
        }
        if (event.target === confirmationPopup) {
            confirmationPopup.style.display = 'none';
        }
        if (event.target === addMoviePopup) {
            addMoviePopup.style.display = 'none';
        }
    });
})
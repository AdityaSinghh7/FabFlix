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
        const starName = document.getElementById('star-name').value;
        const birthYear = document.getElementById('birth-year').value;

        $.ajax({
            type: 'POST',
            url: '../../_dashboard/insertStarServlet',
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
            url: '../../_dashboard/metadataServlet',
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

    cancelButtons.forEach(function(button) {
        button.addEventListener('click', function() {
            insertStarPopup.style.display = 'none';
            metadataPopup.style.display = 'none';
            confirmationPopup.style.display = 'none';
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
    });
})
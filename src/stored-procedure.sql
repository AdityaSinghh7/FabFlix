DELIMITER $$

DROP PROCEDURE IF EXISTS add_movie $$
CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_star_birth_year INT,
    IN p_genre_name VARCHAR(32),
    OUT o_movie_id VARCHAR(10),
    OUT o_star_id VARCHAR(10),
    OUT o_genre_id INT,
    OUT o_message VARCHAR(255)
)
BEGIN
    DECLARE v_existing_movie_count INT;
    DECLARE v_existing_star_id VARCHAR(10);
    DECLARE v_existing_genre_id INT;

    SET o_message = '';

    -- Check if the movie already exists
    SELECT COUNT(*) INTO v_existing_movie_count
    FROM movies
    WHERE title = p_title AND year = p_year AND director = p_director;

    IF v_existing_movie_count > 0 THEN
        SET o_message = 'Movie already exists. No changes made.';
        SELECT o_message AS message;
        SET o_movie_id = NULL;
        SET o_star_id = NULL;
        SET o_genre_id = NULL;
    ELSE
        -- Generate new movie ID
        SELECT CONCAT('tt', LPAD(CONVERT(SUBSTRING(MAX(id), 3), UNSIGNED) + 1, 7, '0')) INTO o_movie_id FROM movies;

        -- Insert new movie
        INSERT INTO movies (id, title, year, director) VALUES (o_movie_id, p_title, p_year, p_director);

        -- Handle star
        -- Try to find an existing star
        SELECT id INTO v_existing_star_id
        FROM stars
        WHERE name = p_star_name
        LIMIT 1;

        IF v_existing_star_id IS NOT NULL THEN
            SET o_star_id = v_existing_star_id;
            -- Optionally, update birth year if provided and not null
            IF p_star_birth_year IS NOT NULL THEN
                UPDATE stars SET birthYear = p_star_birth_year WHERE id = o_star_id;
            END IF;
        ELSE
            -- Generate new star ID
            SELECT CONCAT('nm', LPAD(CONVERT(SUBSTRING(MAX(id), 3), UNSIGNED) + 1, 7, '0')) INTO o_star_id FROM stars;

            -- Insert new star
            INSERT INTO stars (id, name, birthYear) VALUES (o_star_id, p_star_name, p_star_birth_year);
        END IF;


        INSERT INTO stars_in_movies (starId, movieId) VALUES (o_star_id, o_movie_id);


        SELECT id INTO v_existing_genre_id
        FROM genres
        WHERE name = p_genre_name
        LIMIT 1;

        IF v_existing_genre_id IS NOT NULL THEN
            SET o_genre_id = v_existing_genre_id;
        ELSE

            SELECT MAX(id) + 1 INTO o_genre_id FROM genres;


            INSERT INTO genres (id, name) VALUES (o_genre_id, p_genre_name);
        END IF;


        INSERT INTO genres_in_movies (genreId, movieId) VALUES (o_genre_id, o_movie_id);

        SET o_message = 'Movie added successfully.';
        SELECT o_message AS message;
    END IF;
END $$
DELIMITER ;

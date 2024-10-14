CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;

DROP TABLE IF EXISTS stars_in_movies;
DROP TABLE IF EXISTS genres_in_movies;
DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS creditcards;
DROP TABLE IF EXISTS stars;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS movies;

CREATE TABLE movies (
                        id VARCHAR(10) NOT NULL,
                        title VARCHAR(100) NOT NULL DEFAULT '',
                        year INT NOT NULL,
                        director VARCHAR(100) NOT NULL,
                        PRIMARY KEY (id)
);

CREATE TABLE creditcards (
                             id VARCHAR(20) NOT NULL,
                             firstName VARCHAR(50) NOT NULL DEFAULT '',
                             lastName VARCHAR(50) NOT NULL DEFAULT '',
                             expiration DATE NOT NULL,
                             PRIMARY KEY (id)
);

CREATE TABLE stars (
                       id VARCHAR(10) NOT NULL,
                       name VARCHAR(100) NOT NULL DEFAULT '',
                       birthYear INT DEFAULT NULL,
                       PRIMARY KEY (id)
);

CREATE TABLE stars_in_movies (
                                 starId VARCHAR(10) NOT NULL,
                                 movieId VARCHAR(10) NOT NULL,
                                 PRIMARY KEY (starId, movieId),
                                 FOREIGN KEY (starId) REFERENCES stars(id),
                                 FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE genres (
                        id INT AUTO_INCREMENT NOT NULL,
                        name VARCHAR(32) NOT NULL,
                        PRIMARY KEY (id)
);

CREATE TABLE genres_in_movies (
                                  genreId INT NOT NULL,
                                  movieId VARCHAR(10) NOT NULL,
                                  PRIMARY KEY (genreId, movieId),
                                  FOREIGN KEY (genreId) REFERENCES genres(id),
                                  FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE customers (
                           id INT AUTO_INCREMENT NOT NULL,
                           firstName VARCHAR(50) NOT NULL DEFAULT '',
                           lastName VARCHAR(50) NOT NULL DEFAULT '',
                           ccId VARCHAR(20) NOT NULL,
                           address VARCHAR(200) NOT NULL DEFAULT '',
                           email VARCHAR(50) NOT NULL DEFAULT '',
                           password VARCHAR(20) NOT NULL DEFAULT '',
                           PRIMARY KEY (id),
                           FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

CREATE TABLE sales (
                       id INT AUTO_INCREMENT NOT NULL,
                       customerId INT NOT NULL,
                       movieId VARCHAR(10) NOT NULL,
                       saleDate DATE NOT NULL,
                       PRIMARY KEY (id),
                       FOREIGN KEY (customerId) REFERENCES customers(id),
                       FOREIGN KEY (movieId) REFERENCES movies(id)
);



CREATE TABLE ratings (
                         movieId VARCHAR(10) NOT NULL,
                         rating FLOAT NOT NULL,
                         numVotes INT NOT NULL,
                         PRIMARY KEY (movieId),
                         FOREIGN KEY (movieId) REFERENCES movies(id)
);
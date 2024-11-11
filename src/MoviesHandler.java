import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MoviesHandler extends DefaultHandler {
    private final Connection dbConnection;
    private final FileWriter inconsistencyWriter;

    private StringBuilder tempValue;

    // Movie data
    private String filmTitle;
    private String filmId;
    private int filmYear;
    private final Set<String> genres;
    private String currentDirector;

    private PreparedStatement moviePs;
    private int movieBatchCount = 0;

    private PreparedStatement genrePs;
    private int genreBatchCount = 0;

    private static final int BATCH_SIZE = 500;

    // Existing data
    private final Set<String> existingMovies;
    private final HashMap<String, Integer> genreMap; // genre name to id

    public MoviesHandler(Connection dbConnection, FileWriter inconsistencyWriter) throws SQLException {
        this.dbConnection = dbConnection;
        this.inconsistencyWriter = inconsistencyWriter;
        this.dbConnection.setAutoCommit(true);
        existingMovies = new HashSet<>();
        genres = new HashSet<>();
        genreMap = loadGenres();
        String movieQuery = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
        moviePs = dbConnection.prepareStatement(movieQuery);

        String genreQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
        genrePs = dbConnection.prepareStatement(genreQuery);
    }

    private HashMap<String, Integer> loadGenres() throws SQLException {
        HashMap<String, Integer> map = new HashMap<>();
        String query = "SELECT id, name FROM genres";
        Statement statement = dbConnection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            map.put(rs.getString("name"), rs.getInt("id"));
        }
        rs.close();
        statement.close();
        return map;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempValue = new StringBuilder();
        if (qName.equals("directorfilms")) {
            currentDirector = null;
        } else if (qName.equals("film")) {
            // Reset film data
            filmTitle = null;
            filmId = null;
            filmYear = -1;
            genres.clear();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            if (movieBatchCount > 0) {
                moviePs.executeBatch();
            }
            if (genreBatchCount > 0) {
                genrePs.executeBatch();
            }
            moviePs.close();
            genrePs.close();
        } catch (SQLException e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = tempValue.toString().trim();
        switch (qName) {
            case "dirname":
                currentDirector = value;
                break;
            case "t":
                filmTitle = value;
                break;
            case "fid":
                filmId = value;
                break;
            case "year":
                filmYear = parseYear(value);
                break;
            case "cat":
                genres.add(value);
                break;
            case "film":
                try {
                    if (filmTitle != null && filmYear != -1 && currentDirector != null && !genres.isEmpty()) {
                        String movieKey = filmTitle + "_" + filmYear + "_" + currentDirector;
                        if (!existingMovies.contains(movieKey)) {
                            String newMovieId = generateMovieId(filmId);
                            insertMovie(newMovieId, filmTitle, filmYear, currentDirector);
                            existingMovies.add(movieKey);
                        }
                    } else {
                        logInconsistency("Movie missing required fields: " + filmId);
                    }
                } catch (SQLException e) {
                    System.err.println("SQLException in endElement: " + e.getMessage());
//                    e.printStackTrace();
                }
                break;
        }
    }

    private int parseYear(String yearStr) {
        try {
            return Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            logInconsistency("Invalid year: " + yearStr);
            return -1;
        }
    }

    private void insertMovie(String movieId, String title, int year, String director) throws SQLException {
        moviePs.setString(1, movieId);
        moviePs.setString(2, title);
        moviePs.setInt(3, year);
        moviePs.setString(4, director);
        moviePs.executeUpdate();


        for (String genreName : genres) {
            genreName = standardizeGenreName(genreName);
            int genreId = getGenreId(genreName);
            insertGenreInMovie(genreId, movieId);
        }
    }

    private void insertGenreInMovie(int genreId, String movieId) throws SQLException {
        genrePs.setInt(1, genreId);
        genrePs.setString(2, movieId);
        try {
            genrePs.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting into genres_in_movies: " + e.getMessage());
            throw e;
        }
    }

    private int getGenreId(String genreName) throws SQLException {
        if (genreMap.containsKey(genreName)) {
            return genreMap.get(genreName);
        } else {

            String selectQuery = "SELECT id FROM genres WHERE name = ?";
            PreparedStatement selectPs = dbConnection.prepareStatement(selectQuery);
            selectPs.setString(1, genreName);
            ResultSet rs = selectPs.executeQuery();
            int genreId = -1;
            if (rs.next()) {
                genreId = rs.getInt("id");
                genreMap.put(genreName, genreId);
                System.out.println("Found existing genre: " + genreName + " with ID: " + genreId);
            } else {

                String insertQuery = "INSERT INTO genres (name) VALUES (?)";
                PreparedStatement insertPs = dbConnection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
                insertPs.setString(1, genreName);
                insertPs.executeUpdate();
                ResultSet generatedKeys = insertPs.getGeneratedKeys();
                if (generatedKeys.next()) {
                    genreId = generatedKeys.getInt(1);
                    genreMap.put(genreName, genreId);
                    System.out.println("Inserted new genre: " + genreName + " with ID: " + genreId);
                } else {
                    System.err.println("Failed to insert genre: " + genreName);
                    throw new SQLException("Failed to insert genre: " + genreName);
                }
                generatedKeys.close();
                insertPs.close();
            }
            rs.close();
            selectPs.close();
            return genreId;
        }
    }

    private String generateMovieId(String fid) {
        return "ttf" + fid;
    }

    private void logInconsistency(String message) {
        try {
            inconsistencyWriter.write(message + "\n");
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private String standardizeGenreName(String genreName) {
        if (genreName == null || genreName.trim().isEmpty()) {
            return null;
        }
        String lowerCaseGenre = genreName.trim().toLowerCase();

        if (lowerCaseGenre.matches("actn|adct|adctx|axtn|act")) {
            return "Action";
        } else if (lowerCaseGenre.matches("advt|adventure")) {
            return "Adventure";
        } else if (lowerCaseGenre.matches("anim|cart|animation")) {
            return "Animation";
        } else if (lowerCaseGenre.matches("comd|comdx|cond|comedy|comd noir|comd west|comdx")) {
            return "Comedy";
        } else if (lowerCaseGenre.matches("crim|crime")) {
            return "Crime";
        } else if (lowerCaseGenre.matches("docu|duco|ducu|dicu|docu dram|dram docu|documentary")) {
            return "Documentary";
        } else if (lowerCaseGenre.matches("dram|draam|dramn|dram>|dramd|dram\\.actn|drama")) {
            return "Drama";
        } else if (lowerCaseGenre.matches("fant|fantasy|fantasy/horror|fanth\\*")) {
            return "Fantasy";
        } else if (lowerCaseGenre.matches("horr|hor|h|h\\*\\*|h0|weird|viol|horror")) {
            return "Horror";
        } else if (lowerCaseGenre.matches("musc|muusc|muscl|stage musical|musical")) {
            return "Musical";
        } else if (lowerCaseGenre.matches("myst|mystp|mystery")) {
            return "Mystery";
        } else if (lowerCaseGenre.matches("romt|ront|romt comd|romt advt|romt fant|romtx|romt\\. comd|romt dram|romance")) {
            return "Romance";
        } else if (lowerCaseGenre.matches("scfi|sci[- ]?fi|s\\.f\\.|sxfi")) {
            return "Sci-Fi";
        } else if (lowerCaseGenre.matches("susp|thriller")) {
            return "Thriller";
        } else if (lowerCaseGenre.matches("west|west1|western")) {
            return "Western";
        } else if (lowerCaseGenre.matches("noir|noir comd|noir comd romt")) {
            return "Film-Noir";
        } else if (lowerCaseGenre.matches("psy(c|h)|psych dram|psychological")) {
            return "Psychological";
        } else if (lowerCaseGenre.matches("cult")) {
            return "Cult";
        } else if (lowerCaseGenre.matches("surr|surl|surreal|weird")) {
            return "Fantasy"; // Or "Surreal"
        } else if (lowerCaseGenre.matches("avant garde|avga|experimental|expm")) {
            return "Experimental";
        } else if (lowerCaseGenre.matches("satire|sati")) {
            return "Satire";
        } else if (lowerCaseGenre.matches("war")) {
            return "War";
        } else if (lowerCaseGenre.matches("bio(p|g|px|b)?|biography")) {
            return "Biography";
        } else if (lowerCaseGenre.matches("hist|history")) {
            return "History";
        } else if (lowerCaseGenre.matches("porn|porb|adult")) {
            return "Adult";
        } else if (lowerCaseGenre.matches("tv|tvmini")) {
            return "TV";
        } else if (lowerCaseGenre.matches("sport|sports")) {
            return "Sport";
        } else if (lowerCaseGenre.matches("road")) {
            return "Road";
        } else if (lowerCaseGenre.matches("family|faml")) {
            return "Family";
        } else if (lowerCaseGenre.matches("music")) {
            return "Music";
        } else {
            return genreName;
        }
    }
}

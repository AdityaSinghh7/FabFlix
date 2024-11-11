import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CastsHandler extends DefaultHandler {
    private final Connection dbConnection;
    private final FileWriter inconsistencyWriter;

    private StringBuilder tempValue;

    // Cast data
    private String filmId;
    private String actorName;

    // Existing data
    private final HashMap<String, String> starIdMap; // name to starId
    private final Set<String> existingSIM; // stars_in_movies entries

    private PreparedStatement ps;
    private int batchCount = 0;
    private static final int BATCH_SIZE = 500;

    public CastsHandler(Connection dbConnection, FileWriter inconsistencyWriter) throws SQLException {
        this.dbConnection = dbConnection;
        this.inconsistencyWriter = inconsistencyWriter;
        starIdMap = loadStars();
        existingSIM = new HashSet<>();
        String query = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
        ps = dbConnection.prepareStatement(query);
    }


    private HashMap<String, String> loadStars() throws SQLException {
        HashMap<String, String> map = new HashMap<>();
        String query = "SELECT id, name FROM stars";
        Statement statement = dbConnection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        while (rs.next()) {
            String id = rs.getString("id");
            String name = rs.getString("name");
            map.put(name, id);
        }
        rs.close();
        statement.close();
        return map;
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            if (batchCount > 0) {
                ps.executeBatch();
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempValue = new StringBuilder();
        if (qName.equals("m")) {
            // Reset data
            filmId = null;
            actorName = null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = tempValue.toString().trim();
        switch (qName) {
            case "f":
                filmId = value;
                break;
            case "a":
                actorName = value;
                break;
            case "m":
                try {
                    if (filmId != null && actorName != null) {
                        String movieId = generateMovieId(filmId);

                        if (!movieExists(movieId)) {
                            logInconsistency("Movie not found for fid: " + filmId);
                            return;
                        }

                        String starId = starIdMap.get(actorName);
                        if (starId == null) {
                            logInconsistency("Star not found for name: " + actorName);
                            return;
                        }

                        String simKey = starId + "_" + movieId;
                        if (!existingSIM.contains(simKey)) {
                            insertStarInMovie(starId, movieId);
                            existingSIM.add(simKey);
                        }
                    } else {
                        logInconsistency("Cast missing required fields");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void insertStarInMovie(String starId, String movieId) throws SQLException {
        ps.setString(1, starId);
        ps.setString(2, movieId);
        ps.addBatch();
        batchCount++;

        if (batchCount % BATCH_SIZE == 0) {
            ps.executeBatch();
            batchCount = 0;
        }
    }

    private String generateMovieId(String fid) {
        return "ttf" + fid;
    }

    private boolean movieExists(String movieId) throws SQLException {
        String query = "SELECT 1 FROM movies WHERE id = ?";
        PreparedStatement ps = dbConnection.prepareStatement(query);
        ps.setString(1, movieId);
        ResultSet rs = ps.executeQuery();
        boolean exists = rs.next();
        rs.close();
        ps.close();
        return exists;
    }

    private void logInconsistency(String message) {
        try {
            inconsistencyWriter.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

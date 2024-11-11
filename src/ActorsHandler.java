import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ActorsHandler extends DefaultHandler {
    private final Connection dbConnection;
    private final FileWriter inconsistencyWriter;

    private List<PreparedStatement> starBatch = new ArrayList<>();
    private static final int BATCH_SIZE = 500;
    private int batchCount = 0;
    private String stageName;
    private Integer birthYear;
    private int lastNumericId;
    private StringBuilder tempValue;

    private final Set<String> existingStars;
    private PreparedStatement ps;

    public ActorsHandler(Connection dbConnection, FileWriter inconsistencyWriter) throws SQLException, IOException {
        this.dbConnection = dbConnection;
        this.inconsistencyWriter = inconsistencyWriter;
        existingStars = new HashSet<>();
        String query = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
        ps = dbConnection.prepareStatement(query);
        lastNumericId = getMaxNumericId();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempValue = new StringBuilder();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempValue.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String value = tempValue.toString().trim();
        switch (qName) {
            case "stagename":
                stageName = value;
                break;
            case "dob":
                birthYear = parseBirthYear(value);
                break;
            case "actor":
                try {
                    if (stageName != null && !stageName.isEmpty()) {
                        if (!existingStars.contains(stageName)) {
                            insertStar(stageName, birthYear);
                            existingStars.add(stageName);
                        }
                    } else {
                        logInconsistency("Actor missing stagename");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                stageName = null;
                birthYear = null;
                break;
        }
    }

    private Integer parseBirthYear(String dob) {
        try {
            if (dob.isEmpty()) return null;
            return Integer.parseInt(dob);
        } catch (NumberFormatException e) {
            logInconsistency("Invalid birth year: " + dob);
            return null;
        }
    }

    private void insertStar(String name, Integer birthYear) throws SQLException {
        String newId = generateStarId();
        ps.setString(1, newId);
        ps.setString(2, name);
        if (birthYear != null) {
            ps.setInt(3, birthYear);
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);
        }
        ps.addBatch();
        batchCount++;

        if (batchCount % BATCH_SIZE == 0) {
            executeStarBatch();
        }
    }

    private void executeStarBatch() throws SQLException {
        ps.executeBatch();
        batchCount = 0;
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            if (batchCount > 0) {
                executeStarBatch();
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateStarId() throws SQLException {
        lastNumericId++;
        return "nm" + String.format("%07d", lastNumericId);
    }

    private int getMaxNumericId() throws SQLException {
        String query = "SELECT MAX(CONVERT(SUBSTRING(id, 3), UNSIGNED INTEGER)) AS maxNumericId FROM stars";
        Statement statement = dbConnection.createStatement();
        ResultSet rs = statement.executeQuery(query);
        int maxNumericId = 0;
        if (rs.next()) {
            maxNumericId = rs.getInt("maxNumericId");
        }
        rs.close();
        statement.close();
        return maxNumericId;
    }

    private void logInconsistency(String message) {
        try {
            inconsistencyWriter.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

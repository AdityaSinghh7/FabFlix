import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;

public class ParseAll {
    public static void main(String[] args) {
        System.out.println("Current Working Directory: " + new File(".").getAbsolutePath());
        String actorsXmlFile = "../stanford-movies/actors63.xml";
        String moviesXmlFile = "../stanford-movies/mains243.xml";
        String castsXmlFile = "../stanford-movies/casts124.xml";

        String actorsInconsistencyFile = "actors_inconsistencies.txt";
        String moviesInconsistencyFile = "movies_inconsistencies.txt";
        String castsInconsistencyFile = "casts_inconsistencies.txt";


        String dbUrl = "jdbc:mysql://localhost:3306/moviedb";
        String dbUser = "root";
        String dbPassword = "mypassword";


        ExecutorService executorService = Executors.newFixedThreadPool(2);


        Future<?> actorsFuture = executorService.submit(() -> {
            parseActors(actorsXmlFile, actorsInconsistencyFile, dbUrl, dbUser, dbPassword);
        });

        Future<?> moviesFuture = executorService.submit(() -> {
            parseMovies(moviesXmlFile, moviesInconsistencyFile, dbUrl, dbUser, dbPassword);
        });


        try {
            actorsFuture.get();
            moviesFuture.get();
            System.out.println("Actors and Movies parsing completed.");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            executorService.shutdownNow();
            return;
        }

        // Parse casts after actors and movies parsing is done
        parseCasts(castsXmlFile, castsInconsistencyFile, dbUrl, dbUser, dbPassword);

        // Shutdown the executor service
        executorService.shutdown();
    }

    private static void parseActors(String xmlFile, String inconsistencyFile, String dbUrl, String dbUser, String dbPassword) {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             FileWriter inconsistencyWriter = new FileWriter(inconsistencyFile)) {

            dbConnection.setAutoCommit(false);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            ActorsHandler handler = new ActorsHandler(dbConnection, inconsistencyWriter);
            InputStream inputStream = new FileInputStream(xmlFile);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

            InputSource is = new InputSource(reader);
            is.setEncoding("ISO-8859-1");

            saxParser.parse(is, handler);

            dbConnection.commit();
            System.out.println("Actors parsing completed.");

        } catch (SQLException | IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private static void parseMovies(String xmlFile, String inconsistencyFile, String dbUrl, String dbUser, String dbPassword) {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             FileWriter inconsistencyWriter = new FileWriter(inconsistencyFile)) {

            dbConnection.setAutoCommit(false);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            MoviesHandler handler = new MoviesHandler(dbConnection, inconsistencyWriter);
            InputStream inputStream = new FileInputStream(xmlFile);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

            InputSource is = new InputSource(reader);
            is.setEncoding("ISO-8859-1");

            saxParser.parse(is, handler);

            dbConnection.commit();
            System.out.println("Movies parsing completed.");

        } catch (SQLException | IOException | SAXException | ParserConfigurationException e) {
//            e.printStackTrace();
        }
    }

    private static void parseCasts(String xmlFile, String inconsistencyFile, String dbUrl, String dbUser, String dbPassword) {
        try (Connection dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
             FileWriter inconsistencyWriter = new FileWriter(inconsistencyFile)) {

            dbConnection.setAutoCommit(false);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            CastsHandler handler = new CastsHandler(dbConnection, inconsistencyWriter);
            InputStream inputStream = new FileInputStream(xmlFile);
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1);

            InputSource is = new InputSource(reader);
            is.setEncoding("ISO-8859-1");

            saxParser.parse(is, handler);

            dbConnection.commit();
            System.out.println("Casts parsing completed.");

        } catch (SQLException | IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}

import java.sql.*;
import java.util.ArrayList;

public class Tester {

            private static final String connectionSting = "jdbc:sqlite:testdb.sqlite";
//    private static String connectionSting = "jdbc:sqlite::memory:";
    private static Connection connection = null;

    public static void checkDBSchema() {
        ArrayList<String> tablenames = new ArrayList<>();
        //<editor-fold desc="TableCreated">
        try {
            String statement =  "SELECT name FROM sqlite_master " +
                    "WHERE type='table' " +
                    "ORDER BY name;";

            Connection conn = getConnection();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(statement);

            while (rs.next()) {
                tablenames.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //</editor-fold>

        // Tests
        assert (tablenames.size() == 1);
        assert (tablenames.get(0).equals("TestClassA"));
    }

    public static void checkDBInsert() {
       String name = "";

        //<editor-fold desc="get data">
        try {
            String statement =  "select Name from TestClassA";

            Connection conn = getConnection();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(statement);

            while (rs.next()) {
                name = rs.getString("Name");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        //</editor-fold>

        assert(name.equals("testString"));
    }

    public static void connect() {
        try {
            // create a connection to the database
            connection = DriverManager.getConnection(connectionSting);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {

        }
    }

    public static Connection getConnection() {
        if (connection == null)
            connect();
        return connection;
    }
}

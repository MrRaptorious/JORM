import jormCore.JormApplication;
import jormCore.ApplicationSubManager;

import jormCore.ObjectSpace;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.tracing.LogLevel;
import jormCore.wrapping.WrappingHandler;
import jormSQLite.DBConnection.FieldTypeParserSQLite;
import jormSQLite.DBConnection.SQLiteConnection;
import jormSQLite.DependencyConfigurationSQLite;
import jormSQLite.criteria.SQLiteStatementBuilder;

import java.sql.SQLException;

public class MainClass {
    public static void main(String[] args) throws SQLException {

        String connectionSting = "jdbc:sqlite:testdb.sqlite";

        JormApplication app = JormApplication.getApplication();

        ApplicationSubManager localManager = new ApplicationSubManager(connectionSting, new DependencyConfigurationSQLite(), LogLevel.Error);

        localManager.registerType(TestClassA.class);

        app.registerApplicationSubManager("localManager", localManager, true);

        app.start();

        ObjectSpace os = localManager.createObjectSpace();

        var obejcts = os.getObjects(TestClassA.class);

        for (var obj : obejcts) {
            System.out.println(obj.getMeinTestString());
        }
    }
}

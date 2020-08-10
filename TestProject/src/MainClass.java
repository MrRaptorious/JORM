import jormCore.JormApplication;
import jormCore.ApplicationSubManager;

import jormCore.ObjectSpace;
import jormCore.tracing.LogLevel;
import jormSQLite.DependencyConfigurationSQLite;

import jormMySQL.DependencyConfigurationMySQL;

import java.sql.SQLException;

public class MainClass {
    public static void main(String[] args) throws SQLException {
        mysqlConnection();
    }
    private static void mysqlConnection() {
        String connectionSting = "jdbc:mysql://localhost:3306/MapperTest?user=root&password=";
        //String connectionSting = "jdbc:sqlite::memory:";

        //<editor-fold desc="SetUp">
        // create application
        JormApplication app = JormApplication.getApplication();

        // create submanager
        ApplicationSubManager localManager = new ApplicationSubManager(connectionSting, new DependencyConfigurationMySQL(), LogLevel.Error);

        // register types to store in db via submanager
        localManager.registerType(TestClassA.class);
        localManager.registerType(TestClassB.class);


        // register submanager in application
        app.registerApplicationSubManager("localManager", localManager, true);

        // start the application
        app.start();
        //</editor-fold>

//        // test table creation
//        Tester.checkDBSchema();


        ObjectSpace os = localManager.createObjectSpace();

        // insert data
        TestClassA objA = os.createObject(TestClassA.class);
        objA.setMeinTestString("testString");

        TestClassB objB = os.createObject(TestClassB.class);
        objB.setTestClassA(objA);
        os.commitChanges();

//        // test table content
//        Tester.checkDBInsert();

        var obejcts = os.getObjects(TestClassA.class);

        for (var obj : obejcts) {
            System.out.println(obj.getMeinTestString());
        }
    }

    private static void sqliteConnection() {
        String connectionSting = "jdbc:sqlite:testdb.sqlite";
//        String connectionSting = "jdbc:sqlite::memory:";

        //<editor-fold desc="SetUp">
        // create application
        JormApplication app = JormApplication.getApplication();

        // create submanager
        ApplicationSubManager localManager = new ApplicationSubManager(connectionSting, new DependencyConfigurationSQLite(), LogLevel.Error);

        // register types to store in db via submanager
        localManager.registerType(TestClassA.class);
        localManager.registerType(TestClassB.class);

        // register submanager in application
        app.registerApplicationSubManager("localManager", localManager, true);

        // start the application
        app.start();
        //</editor-fold>

        // test table creation
        Tester.checkDBSchema();

        ObjectSpace os = localManager.createObjectSpace();

        // insert data
        TestClassA objA = os.createObject(TestClassA.class);
        objA.setMeinTestString("With B Object");
        TestClassB objB = os.createObject(TestClassB.class);
        objB.setTestClassA(objA);
        os.commitChanges();


        // test table content
        Tester.checkDBInsert();

//        var obejcts = os.getObjects(TestClassA.class);
//
//        for (var obj : obejcts) {
//            System.out.println(obj.getMeinTestString());
//        }
    }

}

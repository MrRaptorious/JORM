import jormCore.JormApplication;
import jormCore.ApplicationSubManager;

import jormCore.ObjectSpace;
import jormCore.tracing.LogLevel;
import jormSQLite.DependencyConfigurationSQLite;

import jormMySQL.DependencyConfigurationMySQL;

public class MainClass {
    public static void main(String[] args) {
//        mysqlConnection();
        sqliteConnection();
    }

    @SuppressWarnings("unused")
    private static void mysqlConnection() {
        String connectionSting = "jdbc:mysql://localhost:3306/MapperTest?user=root&password=";
        //String connectionSting = "jdbc:sqlite::memory:";

        //<editor-fold desc="SetUp">
        // create application
        JormApplication app = JormApplication.getApplication();

        // create subManager
        ApplicationSubManager localManager = new ApplicationSubManager(connectionSting, new DependencyConfigurationMySQL(), LogLevel.Error);

        // register types to store in db via subManager
        localManager.registerType(TestClassA.class);
        localManager.registerType(TestClassB.class);


        // register subManager in application
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

        var objects = os.getObjects(TestClassA.class);

        for (var obj : objects) {
            System.out.println(obj.getMeinTestString());
        }
    }

    private static void sqliteConnection() {
        String connectionSting = "jdbc:sqlite:testdb.sqlite";
//        String connectionSting = "jdbc:sqlite::memory:";

        //<editor-fold desc="SetUp">
        // create application
        JormApplication app = JormApplication.getApplication();

        // create subManager
        ApplicationSubManager localManager = new ApplicationSubManager(connectionSting, new DependencyConfigurationSQLite(), LogLevel.Error);

        // register types to store in db via subManager
        localManager.registerType(TestClassA.class);
        localManager.registerType(TestClassB.class);

        // register subManager in application
        app.registerApplicationSubManager("localManager", localManager, true);

        // start the application
        app.start();
        //</editor-fold>

        // test table creation
        Tester.checkDBSchema();

        ObjectSpace os = localManager.createObjectSpace();

        // <editor-fold desc="Circle Test">
        // b-circle
//        TestClassB b1 = new TestClassB(os);
//        TestClassB b2 = new TestClassB(os);
//        b1.setTestClassB(b2);
//        b2.setTestClassB(b1);
//
//        // b-self
//        TestClassB b3 = new TestClassB(os);
//        b3.setTestClassB(b3);

        // </editor-fold>

        var bs = os.getObjects(TestClassB.class);

        for(var b : bs) {
            System.out.println(b);
            System.out.println(b.getTestClassB());
            System.out.println();
        }

        os.commitChanges();
    }
}

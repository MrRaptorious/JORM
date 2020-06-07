package jormCore;

import jormCore.Criteria.StatementBuilder;
import jormCore.DBConnection.DatabaseConnection;
import jormCore.DBConnection.FieldTypeParser;
import jormCore.Tracing.LogLevel;
import jormCore.Wrapping.WrappingHandler;

import java.util.List;

public class ApplicationSubManager {

    private LogLevel logLevel;
    private DatabaseConnection connection;
    private FieldTypeParser currentParser;
    private StatementBuilder statementBuilder;
    private  WrappingHandler wrappingHandler;

    private ApplicationSubManager(DatabaseConnection con, StatementBuilder builder) {
        // TODO Load from settings File
        logLevel = LogLevel.Error;
        //connectionSting = "jdbc:sqlite:testdb.sqlite";
        statementBuilder = builder;
        connection = con;
        currentParser = con;
        wrappingHandler = new WrappingHandler(con);
    }

    public StatementBuilder getStatementBuilder() {
        return statementBuilder;
    }

    public FieldTypeParser getCurrentFieldTypeParser() {
        return currentParser;
    }

    /**
     * Initializes the database (create schema then update the schema)
     */
    private void initDatabase() {
        connection.createSchema();
        connection.updateSchema();
    }

    /**
     * Starts the application, all types have to be registerd, the db schema will be
     * updated
     */
    public void start() {
        // TODO setup relations
        wrappingHandler.updateRelations();

        initDatabase();
    }

    /**
     * Creates a new ObjectSpace object for handling data and communicating with the
     * Database
     *
     * @return a newly created ObjectSpace with the programs database connection
     */
    public ObjectSpace createObjectSpace() {
        return new ObjectSpace(connection, wrappingHandler);
    }

    public ObjectSpace createObjectSpace(boolean loadOnInit) {
        return new ObjectSpace(connection, wrappingHandler, loadOnInit);
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

//    public String getConnectionString() {
//        return connectionSting;
//    }

    /**
     * Registers a subclass from PersistentObject to be able to handle it
     *
     * @param type a subclass from PersistentObject
     */
    public void registerType(Class<? extends PersistentObject> type) {
        wrappingHandler.registerType(type);
    }

    /**
     * Registers a list of subclasses from PersistentObject to be able to handle
     * them
     *
     * @param types a list of subclasses from PersistentObject
     */
    public void registerTypes(List<Class<? extends PersistentObject>> types) {
        if (types != null) {
            for (Class<? extends PersistentObject> type : types) {
                registerType(type);
            }
        }
    }

    public WrappingHandler getWrappingHandler() {
        return wrappingHandler;
    }
}

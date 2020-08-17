package jormCore;

import jormCore.criteria.StatementBuilder;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.tracing.LogLevel;
import jormCore.wrapping.WrappingHandler;
import java.sql.SQLException;
import java.util.List;

/**
 * A class to manage registered types and the syncing with one database
 */
@SuppressWarnings("unused")
public class ApplicationSubManager {

    private final LogLevel logLevel;
    private DatabaseConnection connection;
    private FieldTypeParser currentParser;
    private StatementBuilder statementBuilder;
    private WrappingHandler wrappingHandler;
    private final String connectionString;

    public ApplicationSubManager(String connectionString, DependencyConfiguration dependencyConfiguration, LogLevel lvl) {
        this.connectionString = connectionString;
        logLevel = lvl;

        try {
            statementBuilder = dependencyConfiguration.resolve(StatementBuilder.class);
            connection = dependencyConfiguration.resolve(DatabaseConnection.class);
            currentParser = dependencyConfiguration.resolve(FieldTypeParser.class);
            wrappingHandler = dependencyConfiguration.resolve(WrappingHandler.class);
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

    /**
     * Initializes the database (create schema then update the schema)
     */
    private void initDatabase() {
        connection.createSchema();
        connection.updateSchema();
    }

    /**
     * Starts the application, all types have to be registered, the db schema will be
     * updated
     */
    public void start() {

        try {
            connection.connect(connectionString);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        wrappingHandler.updateRelations();

        initDatabase();
    }

    /**
     * Creates a new ObjectSpace object for handling data and communicating with the Database
     *
     * @return a newly created ObjectSpace with the programs database connection
     */
    public ObjectSpace createObjectSpace() {
        return new ObjectSpace(connection, wrappingHandler, currentParser);
    }

    /**
     * Creates a new ObjectSpace object for handling data and communicating with the Database
     *
     * @param loadOnInit determines if the ObjectSpace should be loaded when created or not
     * @return a newly created ObjectSpace with the programs database connection
     */
    public ObjectSpace createObjectSpace(boolean loadOnInit) {
        return new ObjectSpace(connection, wrappingHandler, currentParser, loadOnInit);
    }

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

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public StatementBuilder getStatementBuilder() {
        return statementBuilder;
    }

    public FieldTypeParser getCurrentFieldTypeParser() {
        return currentParser;
    }
}

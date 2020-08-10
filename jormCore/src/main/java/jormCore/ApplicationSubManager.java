package jormCore;

import jormCore.criteria.StatementBuilder;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.tracing.LogLevel;
import jormCore.wrapping.WrappingHandler;

import java.sql.SQLException;
import java.util.List;

public class ApplicationSubManager  {

    private LogLevel logLevel;
    private DatabaseConnection connection;
    private FieldTypeParser currentParser;
    private StatementBuilder statementBuilder;
    private  WrappingHandler wrappingHandler;
    private  String connectionString;

    public ApplicationSubManager(String connectionString, DependencyConfiguration dependencyConfiguration, LogLevel lvl)
    {
        this.connectionString = connectionString;
        logLevel = lvl;

        try {
            statementBuilder = dependencyConfiguration.<StatementBuilder>resolve(StatementBuilder.class);
            connection = dependencyConfiguration.<DatabaseConnection>resolve(DatabaseConnection.class);
            currentParser = dependencyConfiguration.<FieldTypeParser>resolve(FieldTypeParser.class);
            wrappingHandler = dependencyConfiguration.<WrappingHandler>resolve(WrappingHandler.class);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

        try {
            connection.connect(connectionString);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

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
        return new ObjectSpace(connection, wrappingHandler,currentParser);
    }

    public ObjectSpace createObjectSpace(boolean loadOnInit) {
        return new ObjectSpace(connection, wrappingHandler,currentParser, loadOnInit);
    }

    public LogLevel getLogLevel() {
        return logLevel;
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
}

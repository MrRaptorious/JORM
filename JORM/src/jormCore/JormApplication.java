package jormCore;

import java.sql.SQLException;
import java.util.List;

import jormCore.DBConnection.DatabaseConnection;
import jormCore.DBConnection.FieldTypeParser;
import jormCore.DBConnection.SQLiteConnection;
import jormCore.Tracing.LogLevel;
import jormCore.Wrapping.WrappingHandler;

/**
 * The main object representing the entire program
 */
public class JormApplication {

	private static JormApplication application;
	private LogLevel logLevel;
	private String connectionSting;
	private DatabaseConnection connection;
	private FieldTypeParser currentParser;
	private WrappingHandler wrappingHandler;

	private JormApplication() {
		// TODO Load from settings File
		logLevel = LogLevel.Error;
		connectionSting = "jdbc:sqlite:D:\\Programming\\Projects\\Java\\JORM\\TestProject\\testdb.sqlite";

		try {
			connection = new SQLiteConnection(connectionSting);
			currentParser = connection;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public FieldTypeParser getCurrentFieldTypeParser() {
		return currentParser;
	}

	public static JormApplication getApplication() {
		if (application == null)
			application = new JormApplication();

		return application;
	}

	/**
	 *  Initializes the database (create schema then update the schema)
	 */
	private void initDatabase() {
		connection.createSchema();
		connection.updateSchema();
	}

	
	/**
	 *  Starts the application, all types have to be registerd, the db schema will be updated
	 */
	public void start()
	{
		// TODO setup relations
		WrappingHandler.getWrappingHandler().updateRelations();


		initDatabase();
	}

	/**
	 * Creates a new ObjectSpace object for handling data and communicating with the Database
	 * @return a newly created ObjectSpace with the programs database connection
	 */
	public ObjectSpace createObjectSpace() {
		return new ObjectSpace(connection);
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public String getConnectionString() {
		return connectionSting;
	}

	/**
	 * Registers a subclass from PersistentObject to be able to handle it
	 * @param type a subclass from PersistentObject
	 */
	public void registerType(Class<? extends PersistentObject> type) {
		WrappingHandler.getWrappingHandler().registerType(type);
	}

	/**
	 * Registers a list of subclasses from PersistentObject to be able to handle them
	 * @param types a list of subclasses from PersistentObject
	 */
	public void registerTypes(List<Class<? extends PersistentObject>> types) {
		if (types != null) {
			for (Class<? extends PersistentObject> type : types) {
				registerType(type);
			}
		}
	}
}

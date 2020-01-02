package jormCore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jormCore.DBConnection.DatabaseConnection;
import jormCore.DBConnection.FieldTypeParser;
import jormCore.DBConnection.SQLiteConnection;
import jormCore.Tracing.LogLevel;
import jormCore.Wrapping.WrappingHandler;

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
		connectionSting = "jdbc:sqlite:testdb.sqlite";

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

	public void initDatabase() {

		connection.createSchema();
		connection.updateSchema();
	}

	public ObjectSpace createObjectSpace() {
		return new ObjectSpace(connection);
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public String getConnectionString() {
		return connectionSting;
	}

	/* Type Handling */
	public void registerType(Class<? extends PersistentObject> type) {
		WrappingHandler.getWrappingHandler().registerType(type);
	}

	public void registerTypes(List<Class<? extends PersistentObject>> types) {
		if (types != null) {
			for (Class<? extends PersistentObject> type : types) {
				registerType(type);
			}
		}
	}
}

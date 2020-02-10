package jormCore.DBConnection;

import java.sql.ResultSet;

import jormCore.ChangedObject;
import jormCore.PersistentObject;
import jormCore.Wrapping.ClassWrapper;
import jormCore.Wrapping.WrappingHandler;
import java.util.UUID;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Abstract base class to represent a generic relational database connection
 */
public abstract class DatabaseConnection implements FieldTypeParser {

	protected String connectionString;

	public DatabaseConnection(String connectionSting) {
		connectionString = connectionSting;
	}

	/**
	 * Initializes database (create then update schema)
	 * 
	 * @throws SQLException
	 */
	protected void initDatabase() throws SQLException {

		LinkedList<String> createStatements = new LinkedList<>();

		for (ClassWrapper clsWrapper : WrappingHandler.getWrappingHandler().getWrapperList()) {
			String createStatement = generateCreateTypeStatement(clsWrapper);

			if (createStatement != null)
				createStatements.add(createStatement);
		}

		for (String statement : createStatements) {
			execute(statement);
		}

		updateSchema();
	}

	public abstract ResultSet getTable(String name);

	public abstract ResultSet getObject(String name, UUID id);

	public abstract void beginTransaction();
	
	public abstract void commitTransaction();
	
	public abstract void rollbackTransaction();
	
	public abstract void update(ChangedObject obj);

	public abstract void delete(PersistentObject obj);

	public abstract void create(PersistentObject obj) throws SQLException;

	public abstract void execute(String statement) throws SQLException;

	public abstract void createSchema();

	public abstract void updateSchema();

	public abstract String generateCreateTypeStatement(ClassWrapper cw);
}

package jormCore.dbConnection;

import java.sql.ResultSet;

import jormCore.ChangedObject;
import jormCore.criteria.StatementBuilder;
import jormCore.PersistentObject;
import jormCore.criteria.WhereClause;
import jormCore.wrapping.ClassWrapper;
import jormCore.wrapping.WrappingHandler;
import java.util.UUID;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Abstract base class to represent a generic relational database connection
 */
public abstract class DatabaseConnection {

	protected String connectionString;
	protected StatementBuilder statementBuilder;

	public DatabaseConnection(StatementBuilder builder) {
		statementBuilder = builder;
	}

	/**
	 * Opens the connection to the database
	 * @param connectionString the configured connectionString for the database
	 */
	public void connect(String connectionString) throws SQLException {
		this.connectionString = connectionString;
	}

	/**
	 * Initializes database (create then update schema)
	 * 
	 * @throws SQLException
	 */
	protected void initDatabase() throws SQLException {

		LinkedList<String> createStatements = new LinkedList<>();

		for (ClassWrapper clsWrapper :  statementBuilder.getAllEntities()) {
			String createStatement = statementBuilder.createEntity(clsWrapper);

			if (createStatement != null)
				createStatements.add(createStatement);
		}

		for (String statement : createStatements) {
			execute(statement);
		}

		updateSchema();
	}

	public abstract ResultSet getTable(ClassWrapper type);

	public abstract ResultSet getTable(ClassWrapper type, WhereClause clause);

	public abstract ResultSet getObject(ClassWrapper type, UUID id);

	public abstract void beginTransaction();
	
	public abstract void commitTransaction();
	
	public abstract void rollbackTransaction();
	
	public abstract void update(ChangedObject obj);

	public abstract void delete(PersistentObject obj);

	public abstract void create(PersistentObject obj) throws SQLException;

	public abstract void execute(String statement) throws SQLException;

	public abstract void createSchema();

	public abstract void updateSchema();
}

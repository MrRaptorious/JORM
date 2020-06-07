package jormSQLite.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jormCore.criteria.StatementBuilder;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.wrapping.ClassWrapper;
import jormCore.wrapping.FieldWrapper;
import jormCore.wrapping.WrappingHandler;
import jormCore.ChangedObject;
import jormCore.PersistentObject;
import jormCore.criteria.ComparisonOperator;
import jormCore.criteria.WhereClause;

public class SQLiteConnection extends DatabaseConnection {

	private Connection _connection;

	public SQLiteConnection(String connectionSting, StatementBuilder builder) throws SQLException {
		super(connectionSting,builder);
		_connection = DriverManager.getConnection(connectionSting);
	}

	@Override
	public ResultSet getTable(ClassWrapper type) {
		return getTable(type, null);
	}

	@Override
	public ResultSet getObject(ClassWrapper type, UUID id) {
		String statement = statementBuilder.createSelect(type,
				new WhereClause(type.getPrimaryKeyMember().getName(), id, ComparisonOperator.Equal));

		ResultSet set = null;

		try {
			set = _connection.createStatement().executeQuery(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return set;
	}

	public ResultSet getTable(ClassWrapper type, WhereClause clause) {
		String result = statementBuilder.createSelect(type, clause);

		ResultSet set = null;

		try {
			set = _connection.createStatement().executeQuery(result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return set;
	}

	@Override
	public void update(ChangedObject obj) {

		String result = statementBuilder.createUpdate(obj);

		try {
			_connection.prepareStatement(result).executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void delete(PersistentObject obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void create(PersistentObject obj) throws SQLException {
		String result = statementBuilder.createInsert(obj);
		execute(result);
	}

	@Override
	public void execute(String statement) throws SQLException {
		_connection.createStatement().execute(statement);
	}

	@Override
	public void createSchema() {
		try {
			execute("PRAGMA foreign_keys=off");

			List<String> allStatements = statementBuilder.createAllEntity();

			Statement statement = _connection.createStatement();

			for (String statementString : allStatements) {
				statement.addBatch(statementString);
			}

			statement.executeBatch();

			execute("PRAGMA foreign_keys=on");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateSchema() {
		ArrayList<String> updateStatements = new ArrayList<String>();

		for (ClassWrapper cl : statementBuilder.getAllEntities()) {

			String getTypeSchemaStatement = "PRAGMA table_info(" + cl.getName() + ")";
			List<String> persistentColumns = new ArrayList<String>();

			// collect persistentColumns
			try {
				ResultSet resultSet = _connection.createStatement().executeQuery(getTypeSchemaStatement);
				while (resultSet.next()) {
					persistentColumns.add(resultSet.getString("name"));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (FieldWrapper fieldWrapper : cl.getWrappedFields()) {
				if (!persistentColumns.contains(fieldWrapper.getName()))
					updateStatements.add(statementBuilder.createAddPropertyToEntity(fieldWrapper));
			}
		}

		for (String statement : updateStatements) {
			try {
				execute(statement);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void beginTransaction() {
		try {
			execute("BEGIN TRANSACTION;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void commitTransaction() {
		try {
			execute("COMMIT;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void rollbackTransaction() {
		try {
			execute("ROLLBACK;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

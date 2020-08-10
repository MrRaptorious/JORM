package jormMySQL.dbConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jormCore.criteria.StatementBuilder;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.wrapping.ClassWrapper;
import jormCore.wrapping.FieldWrapper;
import jormCore.ChangedObject;
import jormCore.PersistentObject;
import jormCore.criteria.ComparisonOperator;
import jormCore.criteria.WhereClause;

public class MySQLConnection extends DatabaseConnection {

	private Connection connection;

	public MySQLConnection(StatementBuilder builder) {
		super(builder);
	}

	@Override
	public void connect(String connectionSting) throws SQLException
	{
		super.connect(connectionString);
		connection = DriverManager.getConnection(connectionSting);
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
			set = connection.createStatement().executeQuery(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return set;
	}

	public ResultSet getTable(ClassWrapper type, WhereClause clause) {
		String result = statementBuilder.createSelect(type, clause);

		ResultSet set = null;

		try {
			set = connection.createStatement().executeQuery(result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return set;
	}

	@Override
	public void update(ChangedObject obj) throws SQLException {

		String result = statementBuilder.createUpdate(obj);
		connection.prepareStatement(result).executeUpdate();
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
		connection.createStatement().execute(statement);
	}

	@Override
	public void createSchema() {
		try {
			execute("SET FOREIGN_KEY_CHECKS=0");

			List<String> allStatements = statementBuilder.createAllEntity();

			Statement statement = connection.createStatement();

			for (String statementString : allStatements) {
				statement.addBatch(statementString);
			}

			statement.executeBatch();

			execute("SET FOREIGN_KEY_CHECKS=1");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateSchema() {
		ArrayList<String> updateStatements = new ArrayList<String>();

		for (ClassWrapper cl : statementBuilder.getAllEntities()) {

			String getTypeSchemaStatement = "";


			try {
				getTypeSchemaStatement = String.format("SELECT COLUMN_NAME FROM `INFORMATION_SCHEMA`.`COLUMNS`  WHERE `TABLE_SCHEMA`='%s' AND `TABLE_NAME`='%s';", connection.getCatalog(), cl.getName());
			} catch (SQLException throwables) {
				throwables.printStackTrace(); // TODO
			}

			List<String> persistentColumns = new ArrayList<String>();

			// collect persistentColumns
			try {
				ResultSet resultSet = connection.createStatement().executeQuery(getTypeSchemaStatement);
				while (resultSet.next()) {
					persistentColumns.add(resultSet.getString("COLUMN_NAME"));
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
			execute("START TRANSACTION;");
			execute("SET autocommit = 0;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void commitTransaction() {
		try {
			execute("COMMIT;");
			execute("SET autocommit = 1;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void rollbackTransaction() {
		try {
			execute("ROLLBACK;");
			execute("SET autocommit = 1;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

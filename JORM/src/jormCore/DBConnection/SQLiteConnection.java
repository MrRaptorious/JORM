package jormCore.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import jormCore.Wrapping.ClassWrapper;
import jormCore.Wrapping.FieldWrapper;
import jormCore.Wrapping.WrappingHandler;
import jormCore.ChangedObject;
import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Criteria.ComparisonOperator;
import jormCore.Criteria.WhereClause;

public class SQLiteConnection extends DatabaseConnection {

	private Connection _connection;

	public SQLiteConnection(String connectionSting) throws SQLException {
		super(connectionSting);
		_connection = DriverManager.getConnection(connectionSting);
	}

	@Override
	public ResultSet getTable(ClassWrapper type) {
		return getTable(type, null);
	}

	@Override
	public ResultSet getObject(ClassWrapper type, UUID id) {
		// String statement = "select * from " + name + " where id = "
		// + normalizeValueForInsertStatement(id.getClass(), id);

		String statement = JormApplication.getApplication().getStatementBuilder().createSelect(type, new WhereClause(
				type.getPrimaryKeyMember().getName(), normalizeValueForInsertStatement(id), ComparisonOperator.Equal));

		ResultSet set = null;

		try {
			set = _connection.createStatement().executeQuery(statement);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return set;
	}

	public ResultSet getTable(ClassWrapper type, WhereClause clause) {
		// String result = "SELECT * FROM " + name + " WHERE DELETED = 0";
		String result = JormApplication.getApplication().getStatementBuilder().createSelect(type,
				new WhereClause("DELETED", 0, ComparisonOperator.Equal).And(clause));

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

		ClassWrapper currentClassWrapper = WrappingHandler.getWrappingHandler()
				.getClassWrapper(obj.getRuntimeObject().getClass());

		String result = "UPDATE ";
		result += currentClassWrapper.getName();

		result += " SET ";

		String delimiter = "";

		for (Entry<String, Object> elm : obj.getChanedFields().entrySet()) {

			FieldWrapper currentFieldWrapper = currentClassWrapper.getFieldWrapper(elm.getKey());

			result += delimiter + currentFieldWrapper.getName();
			result += " = ";
			result += normalizeValueForInsertStatement(currentFieldWrapper.getOriginalField().getType(),
					elm.getValue());

			if (delimiter == "")
				delimiter = " , ";
		}

		result += " WHERE ";
		result += currentClassWrapper.getPrimaryKeyMember().getName() + " = ";
		result += "'" + obj.getRuntimeObject().getID() + "'";

		try {
			execute(result);
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

		String result = "INSERT INTO ";
		String columnPart = "(";
		String valuePart = " VALUES(";

		Map<FieldWrapper, Object> objectValues = obj.getPersistentPropertiesWithValues();

		// add tableName
		result += WrappingHandler.getWrappingHandler().getClassWrapper(obj.getClass()).getName();

		String delimiter = "";

		for (Entry<FieldWrapper, Object> elem : objectValues.entrySet()) {

			if (elem.getValue() == null)
				continue;

			columnPart += delimiter + elem.getKey().getName();
			valuePart += delimiter
					+ normalizeValueForInsertStatement(elem.getKey().getOriginalField().getType(), elem.getValue());

			if (delimiter == "")
				delimiter = " , ";
		}

		result += columnPart + ") " + valuePart + ")";

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

			for (ClassWrapper cl : WrappingHandler.getWrappingHandler().getWrapperList()) {
				execute(generateCreateTypeStatement(cl));
			}

			execute("PRAGMA foreign_keys=on");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void updateSchema() {
		ArrayList<String> updateStatements = new ArrayList<>();

		for (ClassWrapper cl : WrappingHandler.getWrappingHandler().getWrapperList()) {

			String getTypeSchemaStatement = "PRAGMA table_info(" + cl.getName() + ")";
			List<String> persistentColumns = new ArrayList<>();

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
					updateStatements.add(generateAddColumnToTableStatement(fieldWrapper));
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
	public String generateCreateTypeStatement(ClassWrapper cw) {
		List<String> fKStatements = new ArrayList<>();

		String result = "CREATE TABLE IF NOT EXISTS " + cw.getName() + " (";

		for (int i = 0; i < cw.getWrappedFields().size(); i++) {

			FieldWrapper wr = cw.getWrappedFields().get(i);

			result += generateFieldDefinition(wr);

			if (wr.isForeigenKey()) {
				fKStatements.add(generateForeignKeyDefinition(wr));
			}

			if (i < cw.getWrappedFields().size() - 1 || fKStatements.size() > 0)
				result += " ,";
		}

		// add FK definitions
		for (int i = 0; i < fKStatements.size(); i++) {

			result += fKStatements.get(i);

			if (i < fKStatements.size() - 1)
				result += " , ";

		}

		result += " )";

		return result;
	}

	public String generateFieldDefinition(FieldWrapper wr) {
		String result = "";

		result += wr.getName();
		result += " ";
		result += wr.getDBType();

		if (wr.isPrimaryKey())
			result += " PRIMARY KEY ";
		if (wr.isAutoincrement())
			result += " AUTOINCREMENT ";
		if (wr.isCanNotBeNull())
			result += " NOT NULL ";

		return result;
	}

	public String generateForeignKeyDefinition(FieldWrapper wr) {
		if (wr.isForeigenKey()) {
			return " FOREIGN KEY(" + wr.getName() + ") REFERENCES " + wr.getForeigenKey().getReferencingType().getName()
					+ "(" + wr.getForeigenKey().getReferencingPrimaryKeyName() + ") ";
		}
		return "";
	}

	public String generateAddColumnToTableStatement(FieldWrapper fw) {

		return "ALTER TABLE " + fw.getClassWrapper().getName() + " ADD " + generateFieldDefinition(fw);
	}

	@Override
	public String normalizeValueForInsertStatement(Object value) {
		if (value == null)
			return "NULL";

		return normalizeValueForInsertStatement(value.getClass(), value);
	}

	// legacy
	private String normalizeValueForInsertStatement(Class<?> type, Object value) {
		if (type == String.class)
			return "'" + (String) value + "'";

		if (PersistentObject.class.isAssignableFrom(type))
			return "'" + ((PersistentObject) value).getID() + "'";

		if (type == Date.class)
			return "" + ((Date) value).getTime();

		if (type == UUID.class)
			return "'" + ((UUID) value).toString() + "'";

		return value.toString();
	}

	@Override
	public String parseFieldType(Class<?> type) {
		if (type == String.class || type == char.class || type == UUID.class
				|| PersistentObject.class.isAssignableFrom(type))
			return "TEXT";

		if (type == int.class || type == Date.class || type == boolean.class)
			return "INTEGER";

		return "TEXT";
	}

	@Override
	public Object castValue(Class<?> type, Object value) {

		if (value == null)
			return null;

		if (type == String.class)
			return value.toString();

		if (type == int.class)
			return Integer.parseInt(value.toString());

		if (type == Date.class)
			return new Date(Long.parseLong(value.toString()));

		if (type == boolean.class)
			return !value.toString().equals("0");

		if (type == UUID.class)
			return UUID.fromString(value.toString());

		return null;

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

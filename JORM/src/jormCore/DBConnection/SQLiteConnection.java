package jormCore.DBConnection;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import jormCore.Annotaions.*;
import jormCore.Wrapping.ClassWrapper;
import jormCore.Wrapping.FieldWrapper;
import jormCore.Wrapping.WrappingHandler;
import jormCore.JormApplication;
import jormCore.PersistentObject;

public class SQLiteConnection extends DatabaseConnection {

	private Connection _connection;

	public SQLiteConnection(String connectionSting) throws SQLException {
		super(connectionSting);
		_connection = DriverManager.getConnection(connectionSting);
	}

	@Override
	public ResultSet getTable(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(PersistentObject obj) {
		// TODO Auto-generated method stub

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
			valuePart += delimiter + normalizeValueForInsertStatement(elem.getKey().getOriginalField().getType(), elem.getValue());

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
				if(!persistentColumns.contains(fieldWrapper.getName()))
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
			return " FOREIGN KEY(" + wr.getName() + ") REFERENCES " + wr.getForeigenKey().getReferencingTypeName() + "("
					+ wr.getForeigenKey().getReferencingPrimaryKeyName() + ") ";
		}
		return "";
	}

	public String generateAddColumnToTableStatement(FieldWrapper fw) {
		
		return "ALTER TABLE " + fw.getClassWrapper().getName() + " ADD " + generateFieldDefinition(fw);
	}

	private String normalizeValueForInsertStatement(Class<?> type, Object value) {

		if (type == String.class)
			return "'" + (String) value + "'";

		if (PersistentObject.class.isAssignableFrom(type))
			return "" + ((PersistentObject) value).getID();

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

}

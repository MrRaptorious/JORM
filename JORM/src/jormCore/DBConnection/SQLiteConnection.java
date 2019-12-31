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

		Map<Field, Object> objectValues = obj.getPersistentPropertiesWithValues();

		result += calculateClassName(obj.getClass());

		String delimiter = "";

		for (Entry<Field, Object> elem : objectValues.entrySet()) {

			if( elem.getValue() == null)
				continue;
			
			columnPart += delimiter + calculateFieldName(elem.getKey());
			valuePart += delimiter + normalizeValue(elem.getKey().getType(), elem.getValue());

			if (delimiter == "")
				delimiter = " , ";
		}

		 result += columnPart+ ") " + valuePart + ")";

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

			for (Class<? extends PersistentObject> cl : JormApplication.getApplication().getTypeList()) {
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

		for (Class<? extends PersistentObject> cl : JormApplication.getApplication().getTypeList()) {

			String getTypeSchemaStatement = "PRAGMA table_info(" + cl.getSimpleName() + ")";
			ArrayList<String> persistentColumns = new ArrayList<>();

			List<Field> runtimeFields = new ArrayList<>();

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

			// collect runtimeColumns (also columns which are already persistent)
			runtimeFields = PersistentObject.getPersistentProperties(cl);

			// set runtimeColumns to ONLY runtimeColumns
			runtimeFields.removeIf(field -> (persistentColumns.contains(calculateFieldName(field))));

			for (Field nonPersistentField : runtimeFields) {
				updateStatements.add(generateAddColumnToTableStatement(cl, nonPersistentField));
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
	public String generateCreateTypeStatement(Class<? extends PersistentObject> type) {
		List<Field> props = PersistentObject.getPersistentProperties(type);
		List<String> fKStatements = new ArrayList<>();

		String result = "CREATE TABLE IF NOT EXISTS " + calculateClassName(type) + " (";

		for (int i = 0; i < props.size(); i++) {
			Field field = props.get(i);

			FieldWrapper wr = WrapField(field);

			result += generateFieldDefinition(wr);

			if (wr.isForeigenKey()) {
				fKStatements.add(generateForeignKeyDefinition(wr));
			}

			if (i < props.size() - 1 || fKStatements.size() > 0)
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
		result += wr.getType();

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

	@Override
	protected String ParseFieldType(Field field) {
		Class<?> type = field.getType();

		if (type == String.class || type == char.class || type == UUID.class || PersistentObject.class.isAssignableFrom(type))
			return "TEXT";
		
		if (type == int.class || type == Date.class )
			return "INTEGER";

		return "TEXT";
	}

	public String generateAddColumnToTableStatement(Class<? extends PersistentObject> cl, Field nonPersistentField) {
		FieldWrapper wr = WrapField(nonPersistentField);

		return "ALTER TABLE " + cl.getSimpleName() + " ADD " + generateFieldDefinition(wr);
	}

	private String normalizeValue(Class<?> type, Object value) {

		if (type == String.class)
			return "'" + (String) value + "'";

		if (PersistentObject.class.isAssignableFrom(type))
			return "" + ((PersistentObject) value).getID();

		if(type == Date.class)
			return "" + ((Date)value).getTime();
		
		if(type == UUID.class)
			return "'" + ((UUID)value).toString() + "'";
		
		return value.toString();
	}
}

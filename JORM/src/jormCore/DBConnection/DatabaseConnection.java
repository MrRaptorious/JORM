package jormCore.DBConnection;

import java.lang.reflect.Field;
import java.sql.ResultSet;

import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Annotaions.Autoincrement;
import jormCore.Annotaions.CanNotBeNull;
import jormCore.Annotaions.Persistent;
import jormCore.Annotaions.PrimaryKey;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public abstract class DatabaseConnection {

	protected String connectionString;

	public DatabaseConnection(String connectionSting) {
		connectionString = connectionSting;
	}

	protected void initDatabase() throws SQLException {

		LinkedList<String> createStatements = new LinkedList<>();

		List<Class<? extends PersistentObject>> types = JormApplication.getApplication().getTypeList();
		for (Class<? extends PersistentObject> type : types) {
			String createStatement = generateCreateTypeStatement(type);

			if (createStatement != null)
				createStatements.add(createStatement);
		}

		for (String statement : createStatements) {
			execute(statement);
		}

		updateSchema();
	}

	public abstract ResultSet getTable(String name);

	public abstract void update(PersistentObject obj);

	public abstract void delete(PersistentObject obj);

	public abstract void create(PersistentObject obj) throws SQLException;

	public abstract void execute(String statement) throws SQLException;

	protected FieldWrapper WrapField(Field field) {

		String name = calculateFieldName(field);
		String type = ParseFieldType(field);
		boolean isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
		boolean canNotBeNull = field.isAnnotationPresent(CanNotBeNull.class);
		boolean autoincrement = field.isAnnotationPresent(Autoincrement.class);
		ForeignKey fKey = null;

		if (PersistentObject.class.isAssignableFrom(field.getType())) {
			fKey = new ForeignKey((Class<? extends PersistentObject>) field.getType());
		}

		return new FieldWrapper(name, type, fKey, isPrimaryKey, canNotBeNull, autoincrement);
	}

	protected abstract String ParseFieldType(Field field);

	public abstract String generateCreateTypeStatement(Class<? extends PersistentObject> type);

	public abstract void createSchema();

	public abstract void updateSchema();

	protected String calculateFieldName(Field field)
	{
		String name = "";
		Persistent persistentAnnotation = field.getAnnotation(Persistent.class);
		
		if(persistentAnnotation != null)
			name = persistentAnnotation.name();
			
		if(name == null || name.equals(""))
			name = field.getName();
		
		return name;
	}
	
	protected String calculateClassName(Class<? extends PersistentObject> cls)
	{
		String name = "";
		Persistent persistentAnnotation = cls.getAnnotation(Persistent.class);
		
		if(persistentAnnotation != null)
			name = persistentAnnotation.name();
			
		if(name == null || name.equals(""))
			name = cls.getSimpleName();
		
		return name;
	}
}

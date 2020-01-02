package jormCore.Wrapping;

import java.lang.reflect.Field;
import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Annotaions.Autoincrement;
import jormCore.Annotaions.CanNotBeNull;
import jormCore.Annotaions.Persistent;
import jormCore.Annotaions.PrimaryKey;

public class FieldWrapper {
	// perhaps not needed
	private ClassWrapper declaringClassWrapper;
	private Field fieldToWrap;

	private ForeignKey foreigenKey;
	private String name;
	private String type;
	private boolean isPrimaryKey;
	private boolean canNotBeNull;
	private boolean autoincrement;

	@SuppressWarnings("unchecked")
	public FieldWrapper(ClassWrapper cw, Field field)
	{
		fieldToWrap = field;
		
		 name = calculateFieldName(field);
		 type = JormApplication.getApplication().getCurrentFieldTypeParser().parseFieldType(field.getClass());
		 isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
		 canNotBeNull = field.isAnnotationPresent(CanNotBeNull.class);
		 autoincrement = field.isAnnotationPresent(Autoincrement.class);
		 declaringClassWrapper = cw;
		 
		 foreigenKey = null;

		if (PersistentObject.class.isAssignableFrom(field.getType())) {
			foreigenKey = new ForeignKey(WrappingHandler.getWrappingHandler()
					.getClassWrapper((Class<? extends PersistentObject>)field.getType()));
		}
		
	}

	public boolean isForeigenKey() {
		return foreigenKey != null;
	}

	public ForeignKey getForeigenKey() {
		return foreigenKey;
	}

	public String getName() {
		return name;
	}

	public String getDBType() {
		return type;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public boolean isCanNotBeNull() {
		return canNotBeNull;
	}

	public boolean isAutoincrement() {
		return autoincrement;
	}

	public ClassWrapper getClassWrapper() {
		return declaringClassWrapper;
	}

	public static String calculateFieldName(Field field) {
		String name = "";
		Persistent persistentAnnotation = field.getAnnotation(Persistent.class);

		if (persistentAnnotation != null)
			name = persistentAnnotation.name();

		if (name == null || name.equals(""))
			name = field.getName();

		return name;
	}

	public Field getOriginalField() {
		return fieldToWrap;
	}
}

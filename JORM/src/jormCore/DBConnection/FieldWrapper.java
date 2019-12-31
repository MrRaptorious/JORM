package jormCore.DBConnection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import jormCore.Annotaions.Persistent;

public class FieldWrapper {

	private String name;
	private String type;
	private boolean isPrimaryKey;
	private boolean canNotBeNull;
	private boolean autoincrement;
	private ForeignKey foreigenKey;

	public FieldWrapper(String name, String type, ForeignKey foreigenKey, boolean isPrimaryKey, boolean canBeNull,
			boolean autoincrement) {
		this.foreigenKey = foreigenKey;
		this.name = name;
		this.type = type;
		this.isPrimaryKey = isPrimaryKey;
		this.canNotBeNull = canBeNull;
		this.autoincrement = autoincrement;
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

	public String getType() {
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
	
//	public static String calculateFieldName(Field field)
//	{
//		String name = "";
//		Persistent persistentAnnotation = field.getAnnotation(Persistent.class);
//		
//		if(persistentAnnotation != null)
//			name = persistentAnnotation.name();
//		
//		if(name == null || name == "")
//			name = field.getName();
//		
//		return name;
//	}
}

package jormCore.Wrapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class ClassWrapper {

	private Class<? extends PersistentObject> classToWrap;
	private String name;
	private List<FieldWrapper> wrappedFields;
	private FieldWrapper primaryKey;

	public ClassWrapper(Class<? extends PersistentObject> cls) {
		classToWrap = cls;

		initialize();
	}

	private void initialize() {
		wrappedFields = new ArrayList<>();
		
		name = calculateClassName(classToWrap);
		calculateWrappedFields();
	}
	
	public FieldWrapper getPrimaryKey()
	{
		if(primaryKey == null)
		{
			for (FieldWrapper fieldWrapper : wrappedFields) {
				if(fieldWrapper.isPrimaryKey())
				{
					primaryKey = fieldWrapper;
					return primaryKey;
				}
			}
		}
		
		return primaryKey;
	}

	public List<FieldWrapper> getWrappedFields() {
		return wrappedFields;
	}

	public String getName() {
		return name;
	}

	private void calculateWrappedFields() {
		Class<?> persistentClass = classToWrap;

		while (persistentClass != null) {
			for (Field field : persistentClass.getDeclaredFields()) {
				if (persistentClass.isAnnotationPresent(jormCore.Annotaions.Persistent.class)
						|| field.isAnnotationPresent(jormCore.Annotaions.Persistent.class)) {
					field.setAccessible(true);
					wrappedFields.add(new FieldWrapper(this, field));
				}
			}

			persistentClass = persistentClass.getSuperclass();
		}
	}

	public static String calculateClassName(Class<? extends PersistentObject> cls) {

		String name = "";
		Persistent persistentAnnotation = cls.getAnnotation(Persistent.class);

		if (persistentAnnotation != null)
			name = persistentAnnotation.name();

		if (name == null || name.equals(""))
			name = cls.getSimpleName();

		return name;
	}
}

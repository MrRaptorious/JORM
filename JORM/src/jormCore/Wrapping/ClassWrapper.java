package jormCore.Wrapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class ClassWrapper {

	private Class<? extends PersistentObject> classToWrap;
	private String name;
//	private List<FieldWrapper> wrappedFields;
	private Map<String, FieldWrapper> wrappedFields;
	private FieldWrapper primaryKey;

	public ClassWrapper(Class<? extends PersistentObject> cls) {
		classToWrap = cls;

		initialize();
	}

	private void initialize() {
//		wrappedFields = new ArrayList<>();
		wrappedFields = new HashMap<>();

		name = calculateClassName(classToWrap);
		calculateWrappedFields();
	}

	public FieldWrapper getPrimaryKey() {
		if (primaryKey == null) {
			for (Entry<String, FieldWrapper> fieldWrapper : wrappedFields.entrySet()) {
				if (fieldWrapper.getValue().isPrimaryKey()) {
					primaryKey = fieldWrapper.getValue();
					return primaryKey;
				}
			}
		}

		return primaryKey;
	}

	public List<FieldWrapper> getWrappedFields() {
		return new ArrayList<FieldWrapper>(wrappedFields.values());
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
					wrappedFields.put(field.getName(), new FieldWrapper(this, field));
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

	public FieldWrapper getFieldWrapper(String fieldName) {
		return wrappedFields.get(fieldName);
	}

	public Class<? extends PersistentObject> getClassToWrap() {
		return classToWrap;
	}
}

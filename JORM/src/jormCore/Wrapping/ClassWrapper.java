package jormCore.Wrapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jormCore.JormApplication;
import jormCore.PersistentObject;
import jormCore.Annotaions.*;

public class ClassWrapper {

	private Class<? extends PersistentObject> classToWrap;
	private String name;
	private Map<String, FieldWrapper> wrappedFields;
	private FieldWrapper primaryKey;
	private Map<String, FieldWrapper> wrappedMultipleRelations;
	private Map<String, FieldWrapper> wrappedRelations;

	public ClassWrapper(Class<? extends PersistentObject> cls) {
		classToWrap = cls;

		initialize();
	}

	private void initialize() {
//		wrappedFields = new ArrayList<>();
		wrappedFields = new HashMap<>();
		wrappedMultipleRelations = new HashMap<>();
		wrappedRelations = new HashMap<>();

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
				if ((persistentClass.isAnnotationPresent(Persistent.class)
						|| field.isAnnotationPresent(Persistent.class))
						&& !field.isAnnotationPresent(NonPersistent.class)) {
					field.setAccessible(true);
					wrappedFields.put(field.getName(), new FieldWrapper(this, field));

					Association associationAnnotation = field.getAnnotation(Association.class);

					// fill wrappedMultipleRelations 
					if (associationAnnotation != null) {
						wrappedMultipleRelations.put(associationAnnotation.name(), wrappedFields.get(field.getName()));
//						wrappedRelations.put(field.getName(),wrappedFields.get(field.getName()));
					}
					
					// fill all wrappedRelations
					if(PersistentObject.class.isAssignableFrom(field.getType()))
					{
						wrappedRelations.put(field.getName(),wrappedFields.get(field.getName()));
					}
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

	public FieldWrapper getRelationMember(String relationName) {
		return wrappedRelations.get(relationName);
	}

	public List<FieldWrapper> getSingleRelationWrapper() {
		return new ArrayList<FieldWrapper>(wrappedRelations.values());
	}
	

	public Class<? extends PersistentObject> getClassToWrap() {
		return classToWrap;
	}
}

package jormCore.Wrapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jormCore.PersistentObject;
import jormCore.Annotaions.*;

/**
 * Wraps a type at runtime
 */
public class ClassWrapper {

	private String name;
	private Class<? extends PersistentObject> classToWrap;
	private FieldWrapper primaryKey;
	private Map<String, FieldWrapper> wrappedFields;
	// private Map<String, FieldWrapper> wrappedMultipleRelations; // n:m relation
	private Map<String, FieldWrapper> wrappedRelations; // all relations
	private Map<String, FieldWrapper> wrappedValueMember; // only values (int, string, double)
	private Map<String, FieldWrapper> wrappedAnonymousAssociations;
	private Map<String, FieldWrapper> wrappedIdentifiedAssociations;

	public ClassWrapper(Class<? extends PersistentObject> cls) {
		classToWrap = cls;
		initialize();
	}

	private void initialize() {
		wrappedFields = new HashMap<>();
		// wrappedMultipleRelations = new HashMap<>();
		wrappedRelations = new HashMap<>();
		wrappedValueMember = new HashMap<>();
		wrappedAnonymousAssociations = new HashMap<>();
		wrappedIdentifiedAssociations = new HashMap<>();

		name = calculateClassName(classToWrap);
		calculateWrappedFields();
	}

	public FieldWrapper getPrimaryKeyMember() {
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
						|| field.isAnnotationPresent(Persistent.class) || field.isAnnotationPresent(Association.class))
						&& !field.isAnnotationPresent(NonPersistent.class)) {

							if(field.isAnnotationPresent(Association.class))
							{
								int i  = 1;
							}

					FieldWrapper wrapper = new FieldWrapper(this, field);

					field.setAccessible(true);
					wrappedFields.put(field.getName(), wrapper);

					if (PersistentObject.class.isAssignableFrom(field.getType()) || wrapper.isList()) {

						// add to all wrappedRelations
						wrappedRelations.put(field.getName(), wrapper);

						jormCore.Annotaions.Association associationAnnotation = field
								.getAnnotation(jormCore.Annotaions.Association.class);

						// add also to anonymous or identified relations
						if (associationAnnotation == null) {
							wrappedAnonymousAssociations.put(field.getName(), wrapper);
						} else {
							wrappedIdentifiedAssociations.put(associationAnnotation.name(), wrapper);
						}
					} else {
						// add to value relations
						wrappedValueMember.put(field.getName(), wrapper);
					}
				}
			}

			persistentClass = persistentClass.getSuperclass();
		}
	}

	public void updateRelations() {
		for (Entry<String, FieldWrapper> entry : wrappedRelations.entrySet()) {
			entry.getValue().updateAssociation();
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

	public FieldWrapper getRelationWrapper(String relationName) {
		return wrappedRelations.get(relationName);
	}

	public List<FieldWrapper> getRelationWrapper() {
		return new ArrayList<FieldWrapper>(wrappedRelations.values());
	}

	public Class<? extends PersistentObject> getClassToWrap() {
		return classToWrap;
	}

	public List<FieldWrapper> getWrappedValueMemeberWrapper() {
		return new ArrayList<FieldWrapper>(wrappedValueMember.values());
	}

	public FieldWrapper getWrappedAssociation(String associationName) {
		if (wrappedIdentifiedAssociations.containsKey(associationName))
			return wrappedIdentifiedAssociations.get(associationName);

		return null;
	}
}

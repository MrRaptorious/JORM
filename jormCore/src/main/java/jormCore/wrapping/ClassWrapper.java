package jormCore.wrapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jormCore.PersistentObject;
import jormCore.annotaions.*;

/**
 * Wraps a type at runtime
 */
public class ClassWrapper {

	private String name;
	private Class<? extends PersistentObject> classToWrap;
	private FieldWrapper primaryKey;
	private  WrappingHandler wrappingHandler;
	private Map<String, FieldWrapper> wrappedFields;
	private Map<String, FieldWrapper> wrappedPersistentFields;
	private Map<String, FieldWrapper> nonPersistentFields;
	private Map<String, FieldWrapper> wrappedRelations;
	private Map<String, FieldWrapper> wrappedValueMember;
	private Map<String, FieldWrapper> wrappedAnonymousRelations;
	private Map<String, FieldWrapper> wrappedIdentifiedAssociations;

	public ClassWrapper(Class<? extends PersistentObject> cls, WrappingHandler handler) {
		classToWrap = cls;
		wrappingHandler = handler;
		initialize();
	}

	private void initialize() {
		wrappedFields = new HashMap<>();
		wrappedPersistentFields = new HashMap<>();
		nonPersistentFields = new HashMap<>();
		wrappedRelations = new HashMap<>();
		wrappedValueMember = new HashMap<>();
		wrappedAnonymousRelations = new HashMap<>();
		wrappedIdentifiedAssociations = new HashMap<>();

		name = calculateClassName(classToWrap);
		calculateWrappedFields();
	}

	public FieldWrapper getPrimaryKeyMember() {
		if (primaryKey == null) {
			for (Entry<String, FieldWrapper> fieldWrapper : wrappedPersistentFields.entrySet()) {
				if (fieldWrapper.getValue().isPrimaryKey()) {
					primaryKey = fieldWrapper.getValue();
					return primaryKey;
				}
			}
		}

		return primaryKey;
	}

	public List<FieldWrapper> getWrappedFields() {
		return getWrappedFields(false);
	}

	public List<FieldWrapper> getWrappedFields(boolean alsoNonPersistent) {
		if (alsoNonPersistent)
			return new ArrayList<FieldWrapper>(wrappedFields.values());
		else
			return new ArrayList<FieldWrapper>(wrappedPersistentFields.values());
	}

	public String getName() {
		return name;
	}

	private void calculateWrappedFields() {
		Class<?> persistentClass = classToWrap;

		while (persistentClass != null) {
			for (Field field : persistentClass.getDeclaredFields()) {

				// wrap all member
				FieldWrapper wrapper =  wrappingHandler.createFieldWrapper(this, field);
				wrappedFields.put(field.getName(), wrapper);

				// wrap persistent member
				if ((persistentClass.isAnnotationPresent(Persistent.class)
						|| field.isAnnotationPresent(Persistent.class) || field.isAnnotationPresent(Association.class))
						&& !field.isAnnotationPresent(NonPersistent.class) && !wrapper.isList()) {
					field.setAccessible(true);
					wrappedPersistentFields.put(field.getName(), wrapper);

					// wrap reference member
					if (PersistentObject.class.isAssignableFrom(field.getType())) {

						// add to all wrappedRelations
						wrappedRelations.put(field.getName(), wrapper);

						jormCore.annotaions.Association associationAnnotation = field
								.getAnnotation(jormCore.annotaions.Association.class);

						// add also to anonymous or identified relations
						if (associationAnnotation == null) {
							wrappedAnonymousRelations.put(field.getName(), wrapper);
						} else {
							wrappedIdentifiedAssociations.put(associationAnnotation.name(), wrapper);
						}
					} else { // wrapp value Member
						wrappedValueMember.put(field.getName(), wrapper);
					}
				} else // wrap nonpersistent member
				{
					nonPersistentFields.put(field.getName(), wrapper);

					if (wrapper.isList()) {
						jormCore.annotaions.Association associationAnnotation = field
								.getAnnotation(jormCore.annotaions.Association.class);

						if (associationAnnotation != null) {
							wrappedRelations.put(field.getName(), wrapper);
							wrappedIdentifiedAssociations.put(associationAnnotation.name(), wrapper);
						}
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
		return getFieldWrapper(fieldName, false);
	}

	public FieldWrapper getFieldWrapper(String fieldName, boolean alsoNonPersistent) {
		if (alsoNonPersistent)
			return wrappedFields.get(fieldName);
		else
			return wrappedPersistentFields.get(fieldName);
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

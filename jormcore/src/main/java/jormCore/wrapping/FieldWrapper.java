package jormCore.wrapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import jormCore.JormList;
import jormCore.PersistentObject;
import jormCore.annotaions.Association;
import jormCore.annotaions.Autoincrement;
import jormCore.annotaions.CanNotBeNull;
import jormCore.annotaions.Persistent;
import jormCore.annotaions.PrimaryKey;

public class FieldWrapper {
	private ClassWrapper declaringClassWrapper;
	private Field fieldToWrap;
	private AssociationWrapper association;
	private String name;
	private String type;
	private boolean isPrimaryKey;
	private boolean canNotBeNull;
	private boolean autoincrement;
	private boolean isList;
	private WrappingHandler wrappingHandler;

	public FieldWrapper(ClassWrapper cw, Field field, WrappingHandler handler) {
		wrappingHandler = handler;
		fieldToWrap = field;
		name = calculateFieldName(field);
		type = handler.getFieldTypeParser().parseFieldType(field.getClass());
		isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
		canNotBeNull = field.isAnnotationPresent(CanNotBeNull.class);
		autoincrement = field.isAnnotationPresent(Autoincrement.class);
		isList = JormList.class.isAssignableFrom(field.getType());
		declaringClassWrapper = cw;
		association = null;
	}

	public boolean isForeigenKey() {
		return association != null;
	}

	public AssociationWrapper getForeigenKey() {
		return association;
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

	@SuppressWarnings("unchecked")
	public void updateAssociation() {
		if (PersistentObject.class.isAssignableFrom(fieldToWrap.getType()) || isList) {
			String name = null;

			if (fieldToWrap.isAnnotationPresent(Association.class))
				name = fieldToWrap.getAnnotation(Association.class).name();

			ClassWrapper foreigneClassWrapper = null;

			if (!isList) {
				foreigneClassWrapper = wrappingHandler
						.getClassWrapper((Class<? extends PersistentObject>) fieldToWrap.getType());
			} else {

				// find generic parameter
				var foreigneClass =  (Class<? extends PersistentObject>)((ParameterizedType)fieldToWrap.getGenericType()).getActualTypeArguments()[0];

				// find classwrapper
				foreigneClassWrapper = wrappingHandler.getClassWrapper(foreigneClass);
			}

			this.association = new AssociationWrapper(foreigneClassWrapper, name);
		}
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

	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		return fieldToWrap.getAnnotation(annotationType);
	}

	public boolean isList() {
		return isList;
	}
}

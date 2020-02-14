package jormCore.Wrapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import jormCore.JormApplication;
import jormCore.JormList;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;
import jormCore.Annotaions.Autoincrement;
import jormCore.Annotaions.CanNotBeNull;
import jormCore.Annotaions.Persistent;
import jormCore.Annotaions.PrimaryKey;

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

	@SuppressWarnings("unchecked")
	public FieldWrapper(ClassWrapper cw, Field field) {
		fieldToWrap = field;
		name = calculateFieldName(field);
		type = JormApplication.getApplication().getCurrentFieldTypeParser().parseFieldType(field.getClass());
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

	public void updateAssociation() {
		if (PersistentObject.class.isAssignableFrom(fieldToWrap.getType()) || isList) {
			String name = null;

			if (fieldToWrap.getAnnotation(Association.class) != null)
				name = fieldToWrap.getAnnotation(Association.class).name();

			ClassWrapper foreigneClassWrapper = null;

			if (!isList) {
				foreigneClassWrapper = WrappingHandler.getWrappingHandler()
						.getClassWrapper((Class<? extends PersistentObject>) fieldToWrap.getType());
			} else {

				// find generic parameter
				var foreigneClass =  (Class<? extends PersistentObject>)((ParameterizedType)fieldToWrap.getGenericType()).getActualTypeArguments()[0];

				// find classwrapper
				foreigneClassWrapper = WrappingHandler.getWrappingHandler().getClassWrapper(foreigneClass);
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

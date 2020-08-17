package jormCore.wrapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import jormCore.JormList;
import jormCore.PersistentObject;
import jormCore.annotaions.*;

public class FieldWrapper {
    private final ClassWrapper declaringClassWrapper;
    private final Field fieldToWrap;
    private AssociationWrapper association;
    private final String name;
    private final String type;
    private final boolean isPrimaryKey;
    private final boolean canNotBeNull;
    private final boolean autoincrement;
    private final boolean isList;
    private final int size;
    private final WrappingHandler wrappingHandler;

    public FieldWrapper(ClassWrapper cw, Field field, WrappingHandler handler) {
        wrappingHandler = handler;
        fieldToWrap = field;
        name = calculateFieldName(field);
        size = field.isAnnotationPresent(Size.class) ? field.getAnnotation(Size.class).Size() : -1;
        type = handler.getFieldTypeParser().parseFieldType(field.getType(),size);
        isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
        canNotBeNull = field.isAnnotationPresent(CanNotBeNull.class);
        autoincrement = field.isAnnotationPresent(Autoincrement.class);
        isList = JormList.class.isAssignableFrom(field.getType());
        declaringClassWrapper = cw;
        association = null;
    }

    public boolean isForeignKey() {
        return association != null;
    }

    public AssociationWrapper getForeignKey() {
        return association;
    }

    public String getName() {
        return name;
    }

    public String getDBType() {
        return type;
    }

    public int getSize() {
        return size;
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

            ClassWrapper foreignClassWrapper = null;

            if (!isList) {
                foreignClassWrapper = wrappingHandler
                        .getClassWrapper((Class<? extends PersistentObject>) fieldToWrap.getType());
            } else {

                // find generic parameter
                var foreignClass = (Class<? extends PersistentObject>) ((ParameterizedType) fieldToWrap.getGenericType()).getActualTypeArguments()[0];

                // find classWrapper
                foreignClassWrapper = wrappingHandler.getClassWrapper(foreignClass);
            }

            this.association = new AssociationWrapper(foreignClassWrapper, name);
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

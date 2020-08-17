package jormCore.wrapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.PersistentObject;

public class WrappingHandler {

	private final FieldTypeParser fieldTypeParser;
	public Map<Class<? extends PersistentObject>, ClassWrapper> classWrapper;

	public  FieldWrapper createFieldWrapper(ClassWrapper cw, Field field)
	{
		return new FieldWrapper(cw,field,this);
	}

	public  ClassWrapper createClassWrapper(Class<? extends PersistentObject> cls)
	{
		return new ClassWrapper(cls,this);
	}

	public WrappingHandler(FieldTypeParser parser) {
		classWrapper = new HashMap<>();
		fieldTypeParser = parser;
	}

	public boolean registerType(Class<? extends PersistentObject> cls) {

		if(!classWrapper.containsKey(cls))
		{
			classWrapper.put(cls, new ClassWrapper(cls,this));
			return true;
		}

		return false;
	}

	public Map<Class<? extends PersistentObject>, ClassWrapper> getWrappingMap() {
		return classWrapper;
	}

	public List<ClassWrapper> getWrapperList() {
		return new ArrayList<>(classWrapper.values());
	}

	public ArrayList<Class<? extends PersistentObject>> getRegisteredTypes() {return new ArrayList<>(classWrapper.keySet()); }

	public ClassWrapper getClassWrapper(Class<? extends PersistentObject> cls) {
		if (classWrapper.containsKey(cls))
			return classWrapper.get(cls);

		return null;
	}

	public FieldWrapper getFieldWrapper(Class<? extends PersistentObject> type, String fieldName) {
		return classWrapper.get(type).getFieldWrapper(fieldName);
	}

	public void updateRelations() {
		for (Entry<Class<? extends PersistentObject>, ClassWrapper> entry : classWrapper.entrySet()) {
			entry.getValue().updateRelations();
		}
	}

	public FieldTypeParser getFieldTypeParser() {
		return fieldTypeParser;
	}

}
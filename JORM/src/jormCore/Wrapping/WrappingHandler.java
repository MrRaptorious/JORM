package jormCore.Wrapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jormCore.PersistentObject;

public class WrappingHandler {

	public static WrappingHandler handler;
	public Map<Class<? extends PersistentObject>, ClassWrapper> classWrapper;

	public static WrappingHandler getWrappingHandler() {
		if (handler == null)
			handler = new WrappingHandler();

		return handler;
	}

	private WrappingHandler() {
		classWrapper = new HashMap<>();
	}

	public void registerType(Class<? extends PersistentObject> cls) {
		if (!classWrapper.containsKey(cls))
			classWrapper.put(cls, new ClassWrapper(cls));
	}
	
	public Map<Class<? extends PersistentObject>, ClassWrapper> getWrappingMap()
	{
		return classWrapper;
	}
	
	public List<ClassWrapper> getWrapperList()
	{
		return new ArrayList<ClassWrapper>(classWrapper.values());
	}
	
	public ClassWrapper getClassWrapper(Class<? extends PersistentObject> cls)
	{
		if(classWrapper.containsKey(cls))
			return classWrapper.get(cls);
		
		return null;
	}
	
	public FieldWrapper getFieldWrapper(Class<? extends PersistentObject> type, String fieldName)
	{
		return classWrapper.get(type).getFieldWrapper(fieldName);
	}
}
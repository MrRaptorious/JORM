package jormCore;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import jormCore.DBConnection.DatabaseConnection;

public class ObjectSpace{
	
//	private Map<Class, List<PersistentObject>> changedObjects;
	private Map<Class, List<PersistentObject>> objectCache;
	private DatabaseConnection connection;
	
	public ObjectSpace(DatabaseConnection connection)
	{
		objectCache = new HashMap<Class, List<PersistentObject>>();
		this.connection = connection;
//		RefreshCache();
	}
	
	public void SaveObject(PersistentObject obj)
	{
		try {
			connection.create(obj);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ObjectSpace(List<Class> typeList)
	{
		for(Class cls : typeList)
			refreshCache(cls);
	}

	@SuppressWarnings("unchecked")
	public <T extends PersistentObject> List<T> getObjects(Class<T> cls)
	{
		if(objectCache != null && objectCache.containsKey(cls))
		{
			List<T> castedList = new ArrayList<T>();
			
			for(PersistentObject obj : objectCache.get(cls))
			{
				castedList.add((T)obj);
			}
			
			return castedList;
		}
		
		return null;
	}
	
	private void refreshCache(Class type)
	{
		// Fill _objectCache from DB for type
	}
	
	private <T extends PersistentObject> void loadCache()
	{
		
	}

	private <T extends PersistentObject> List<T> castListToT(List<PersistentObject> pList)
	{
		List<T> castedList = new ArrayList<T>();
		
		for(PersistentObject obj : pList)
		{
			castedList.add((T)obj);
		}
		
		return castedList;
	}
	
	public <T extends PersistentObject> List<T> findObejcts(Class<T> type, Function<List<T>,List<T>> filter)
	{
		if(objectCache != null && objectCache.containsKey(type))
		{
			List<T> castedList = this.<T>castListToT(objectCache.get(type));
			
			return filter.apply(castedList);
		}
		
		return null;
	}
}
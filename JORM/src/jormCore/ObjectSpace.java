package jormCore;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.*;

import jormCore.DBConnection.DatabaseConnection;

public class ObjectSpace{
	
	// dbConnection Variable
	private Map<Class, List<PersistentObject>> _objectCache;
	private DatabaseConnection connection;
	
	public ObjectSpace(DatabaseConnection connection)
	{
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
			RefreshCache(cls);
	}

	@SuppressWarnings("unchecked")
	public <T extends PersistentObject> List<T> GetObjects(Class<T> cls)
	{
		if(_objectCache != null && _objectCache.containsKey(cls))
		{
			List<T> castedList = new ArrayList<T>();
			
			for(PersistentObject obj : _objectCache.get(cls))
			{
				castedList.add((T)obj);
			}
			
			return castedList;
		}
		
		return null;
	}
	
	private void RefreshCache(Class type)
	{
		// Fill _objectCache from DB for type
	}
	
	private <T extends PersistentObject> void LoadCache()
	{
		
	}

	private <T extends PersistentObject> List<T> CastListToT(List<PersistentObject> pList)
	{
		List<T> castedList = new ArrayList<T>();
		
		for(PersistentObject obj : pList)
		{
			castedList.add((T)obj);
		}
		
		return castedList;
	}
	
	public <T extends PersistentObject> List<T> FindObejcts(Class<T> type, Function<List<T>,List<T>> filter)
	{
		if(_objectCache != null && _objectCache.containsKey(type))
		{
			List<T> castedList = this.<T>CastListToT(_objectCache.get(type));
			
			return filter.apply(castedList);
		}
		
		return null;
	}
	
}
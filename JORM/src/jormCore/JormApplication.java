package jormCore;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jormCore.DBConnection.DatabaseConnection;
import jormCore.DBConnection.SQLiteConnection;
import jormCore.Tracing.LogLevel;

public class JormApplication {

	private static JormApplication application;
	private LogLevel logLevel;
	private String connectionSting;
	private DatabaseConnection connection; 
	
	private List<Class<? extends PersistentObject>> persistentTypeList;

	private JormApplication()
	{
		persistentTypeList = new ArrayList<>();
		//TODO Load from settings File
		logLevel = LogLevel.Error;
		connectionSting = "jdbc:sqlite:testdb.sqlite";
		
		try {
			connection = new SQLiteConnection(connectionSting);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static JormApplication getApplication()
	{
		if(application == null)
			application = new JormApplication();
			
		return application;
	}
	
	public void initDatabase()
	{	
		connection.createSchema();
		connection.updateSchema();
	}

	public ObjectSpace createObjectSpace()
	{
		return new ObjectSpace(connection);
	}

	public LogLevel getLogLevel()
	{
		return logLevel;
	}
	
	public String getConnectionString()
	{
		return connectionSting;
	}
	
    /*Type Handling*/
	public void registerType(Class<? extends PersistentObject> type)
	{
		if(!persistentTypeList.contains(type))
			persistentTypeList.add(type);
	}
	
	public void registerTypes(List<Class<? extends PersistentObject>> types)
	{
		if(types != null)
		{
			for(Class<? extends PersistentObject> type : types)
			{
				registerType(type);
			}
		}
	}
	
	public List<Class<? extends PersistentObject>> getTypeList()
	{
		return persistentTypeList;
	}
}

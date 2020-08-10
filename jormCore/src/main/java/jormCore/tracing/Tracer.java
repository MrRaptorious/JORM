package jormCore.tracing;

public class Tracer {

	private static Tracer logger;
	private LogLevel currentLevel;
	private String log;
	
	
	public static Tracer getLogger()
	{
		return logger;
	}
	
	public static void init(LogLevel level)
	{
		if(logger == null)
		{
			logger = new Tracer();
			logger.currentLevel = level;
			logger.log = "";
		}
	}
	
	private void log(LogLevel level, String message)
	{
		if(currentLevel.compareTo(level) < 1 )
		{
			log += message + System.lineSeparator();
		}
	}
	
	public void logError(String message)
	{
		log(LogLevel.Error,message);
	}
	
	public void logInfo(String message)
	{
		log(LogLevel.Info,message);
	}
	
	public void logTrace(String message)
	{
		log(LogLevel.Trace,message);
	}
	
	public String getLog()
	{
		return log;
	}
}

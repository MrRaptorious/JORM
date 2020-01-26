package testpackage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import jormCore.*;
import jormCore.Tracing.LogLevel;

public class Program {

	public static void main(String[] args) throws IOException {

		JormApplication app = JormApplication.getApplication();
		app.registerType(TestRefClass.class);
		app.registerType(MyTestClass.class);
		app.initDatabase();

		ObjectSpace os = app.createObjectSpace();
		
		
		createObjects(os);
	
//		for (MyTestClass obj : getObjects(os, MyTestClass.class)) {
//			System.out.println(obj.getText());
//			System.out.println(obj.getrefClass().getText());
//			
//		}
		
		
	}
	
	public static void createObjects(ObjectSpace os)
	{

		ArrayList<MyTestClass> tcList = new ArrayList<MyTestClass>();
		ArrayList<TestRefClass> rcList = new ArrayList<TestRefClass>();
		Random random = new Random();
		
		
		for(int i = 0; i<10; i++)
		{
			MyTestClass mt = new MyTestClass(os);
			TestRefClass rc = new TestRefClass(os);
			
			mt.setText("tc Text " + i);
			rc.setText("rc Text" + i);
			
			tcList.add(mt);
			rcList.add(rc);
		}
		
		for (MyTestClass tc : tcList) {

			TestRefClass trc = rcList.remove(random.nextInt(rcList.size()));
			
			tc.setrefClass(trc);
		}
		
	
		
		os.commitChanges();
	}

	public static <T extends PersistentObject> List<T> getObjects(ObjectSpace os, Class<T> cls)
	{
		return os.getObjects(cls);
	}

}

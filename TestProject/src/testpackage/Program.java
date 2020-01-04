package testpackage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jormCore.*;
import jormCore.Tracing.LogLevel;

public class Program {

	public static void main(String[] args) throws IOException {

		JormApplication app = JormApplication.getApplication();
		app.registerType(TestRefClass.class);
		app.registerType(MyTestClass.class);
		app.initDatabase();
		
		ObjectSpace os = app.createObjectSpace();
		
		MyTestClass tc = new MyTestClass(os);
			tc.setText("TollerText");
			
			
			MyTestClass tc1 = new MyTestClass(os);
			tc1.setText("TollerText New!");
		
			
			os.commitChanges();
		
		
//		
//			List<MyTestClass> objectList = os.getObjects(MyTestClass.class);	
//			for (MyTestClass myTestClass : objectList) {
//				System.out.println(myTestClass.getID());
//				System.out.println(myTestClass.getCreationDate());
//				System.out.println(myTestClass.getText());
//				System.out.println();
//				System.out.println();
//			}
	}
}

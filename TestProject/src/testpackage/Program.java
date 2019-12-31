package testpackage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jormCore.*;
import jormCore.Tracing.LogLevel;

public class Program {

	public static void main(String[] args) {

		JormApplication app = JormApplication.getApplication();
		app.registerType(TestRefClass.class);
		app.registerType(MyTestClass.class);
		app.initDatabase();
		
		ObjectSpace os = app.createObjectSpace();
		
		os.SaveObject(new MyTestClass("Dies ist ein Testwert"));
	}
}

package testpackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import jormCore.*;
import jormCore.Criteria.ComparisonOperator;
import jormCore.Criteria.LogicOperator;
import jormCore.Criteria.SQLiteStatementBuilder;
import jormCore.Criteria.WhereClause;
import jormCore.Wrapping.WrappingHandler;

public class Program {

	public static void main(String[] args) throws IOException {

		JormApplication app = JormApplication.getApplication();
		RegisterTypes(app);
		app.start();

		ObjectSpace os = app.createObjectSpace();
		
		WhereClause outerWhere = new WhereClause("null", 12, ComparisonOperator.Equal);
		WhereClause outerWhere3 = new WhereClause(outerWhere, outerWhere, LogicOperator.And);


		String s = app.getApplication().getStatementBuilder().createSelect(WrappingHandler.getWrappingHandler().getClassWrapper(TestA.class),outerWhere3);

		System.out.println(s);

		// createABC(os);

		// TestA a = os.createObject(TestA.class);
		// TestB b = os.createObject(TestB.class);
		// a.setText("thisIsA");
		// b.setText("thisIsB");

		// a.setTestB(b);
		// b.setTestA(a);

		// os.commitChanges();


		// for (TestA a : os.getObjects(TestA.class)) {
		// 	System.out.println(a.getText());
		// 	System.out.println(a.getTestB().getText());
		// 	System.out.println(a.getTestB().getTestA().getText());
		// 	System.out.println();
		// 	System.out.println();
		// }


		// for (TestA a : os.getObjects(TestA.class)) {
		// 	System.out.println(a.getText());
		// 	System.out.println(a.getTestB().getText());
		// 	// System.out.println(a.getTestB().getTestC().getText());
		// }
	}

	private static void createABC(ObjectSpace os) {
		TestA a = os.createObject(TestA.class);
		TestB b = os.createObject(TestB.class);
		TestC c = os.createObject(TestC.class);

		a.setText("thisIsA");
		b.setText("thisIsB");
		c.setText("thisIsC");

		a.setTestB(b);
		// b.setTestC(c);

		os.commitChanges();
	}

	private static void RegisterTypes(JormApplication app) {
		app.registerType(TestRefClass.class);
		app.registerType(MyTestClass.class);
		app.registerType(TestA.class);
		app.registerType(TestB.class);
		app.registerType(TestC.class);
	}

	private static void printMTC(ObjectSpace os) {
		for (MyTestClass mtc : os.getObjects(MyTestClass.class)) {
			System.out.println(mtc.getText());
			if(mtc.getrefClass() != null)
			System.out.println(mtc.getrefClass().getText());
			System.out.println();
			System.out.println();
		}
	}

	private static void createObjects(ObjectSpace os) {
		MyTestClass tc = os.createObject(MyTestClass.class);
		MyTestClass tc2 = os.createObject(MyTestClass.class);
		MyTestClass tc3 = os.createObject(MyTestClass.class);

		TestRefClass rc = os.createObject(TestRefClass.class);
		TestRefClass rc2 = os.createObject(TestRefClass.class);
		TestRefClass rc3 = os.createObject(TestRefClass.class);


		tc.setText("TestClass1");
		tc.setrefClass(rc);
		rc.setText("RefClass1");

		
		tc2.setText("TestClass2");
		tc2.setrefClass(rc2);
		rc2.setText("RefClass2");
		
		tc3.setText("TestClass3");
		tc3.setrefClass(rc3);
		rc3.setText("RefClass3");

		os.commitChanges();
	}

	public static <T extends PersistentObject> List<T> getObjects(ObjectSpace os, Class<T> cls)
	{
		return os.getObjects(cls);
	}
}

package testpackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

		
		TestB testb = os.createObject(TestB.class);
		os.commitChanges();

		TestA testa = null;

		var alla = os.getObjects(TestA.class);

		if (alla.size() < 1) {
			testa = new TestA(os);
			alla.add(testa);
			os.commitChanges();
		}
		
		testa = alla.get(0);

		testa.getTestBList().add(testb);

		for (TestB testbb : testa.getTestBList()) {
			System.out.println(testbb);
		}

		os.commitChanges();

		System.out.println(testa.getTestBList().size());
	}

	private static void RegisterTypes(JormApplication app) {
		// app.registerType(TestRefClass.class);
		// app.registerType(MyTestClass.class);
		app.registerType(TestA.class);
		app.registerType(TestB.class);
		// app.registerType(TestC.class);
	}

}

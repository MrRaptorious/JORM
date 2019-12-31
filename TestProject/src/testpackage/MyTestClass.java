package testpackage;

import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class MyTestClass extends PersistentObject{

	@Persistent
	private int tolleProperty;
	
	@Persistent
	private int naechsteProperty;

	@Persistent
	private TestRefClass refClass;
	
	@Persistent
	private String text;
	
	@Persistent(name = "DERZWEITETEXT")
	private String text2;
	
	public MyTestClass(String txt)
	{
		text = txt;
	}
}

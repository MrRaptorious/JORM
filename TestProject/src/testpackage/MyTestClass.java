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
	
}

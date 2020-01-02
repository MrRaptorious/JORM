package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class MyTestClass extends PersistentObject{

	@Persistent
	private TestRefClass refClass;
	
	@Persistent
	private String text;

	public MyTestClass(ObjectSpace os)
	{
		super(os);
	}
}

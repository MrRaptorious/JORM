package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

@Persistent
public class TestRefClass extends PersistentObject{

	public TestRefClass(ObjectSpace os) {
		super(os);
	}
}

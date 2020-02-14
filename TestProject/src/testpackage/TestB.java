package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;
import jormCore.Annotaions.Persistent;

public class TestB extends PersistentObject {

	@Persistent
	@Association(name = "ListAandB")
	private TestA testA;
	
	public TestB(ObjectSpace os) {
		super(os);
	}

	public TestA getTestA() {
        return testA;
    }
}
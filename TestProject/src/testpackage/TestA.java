package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;
import jormCore.Annotaions.Persistent;

public class TestA extends PersistentObject {

    @Persistent
    private String text;

    @Persistent
    @Association(name = "AandB")
    private TestB testB;

    public TestA(ObjectSpace os) {
        super(os);
    }

	public void setText(String myText) {
		setPropertyValue("text", myText);
	}
	
	public String getText() {
		return text;
	}

    public TestB getTestB() {
        return testB;
    }

    public void setTestB(TestB value) {
        setPropertyValue("testB", value);
    }

}

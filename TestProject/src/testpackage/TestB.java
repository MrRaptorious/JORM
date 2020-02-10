package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;
import jormCore.Annotaions.Persistent;

public class TestB extends PersistentObject {

	@Persistent
	private String text;

    @Persistent
    @Association(name = "AandB")
    private TestA testA;


	public TestB(ObjectSpace os) {
		super(os);
	}

	public void setText(String myText) {
		setPropertyValue("text", myText);
	}
	
	public String getText() {
		return text;
	}

    public TestA getTestA() {
        return testA;
    }

    public void setTestA(TestA value) {
        setPropertyValue("testA", value);
    }
}
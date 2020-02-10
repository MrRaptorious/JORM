package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class TestB extends PersistentObject {

	@Persistent
	private String text;

    @Persistent
    private TestC testC;

	public TestB(ObjectSpace os) {
		super(os);
	}

	public void setText(String myText) {
		setPropertyValue("text", myText);
	}
	
	public String getText() {
		return text;
	}

    public TestC getTestC() {
        return testC;
    }

    public void setTestC(TestC value) {
        setPropertyValue("testC", value);
    }
}
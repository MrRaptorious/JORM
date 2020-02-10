package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class TestC extends PersistentObject {

	@Persistent
	private String text;

	public TestC(ObjectSpace os) {
		super(os);
	}

	public void setText(String myText) {
		setPropertyValue("text", myText);
	}
	
	public String getText() {
		return text;
	}

}
package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class MyTestClass extends PersistentObject {

	@Persistent
	private String text;

	public MyTestClass(ObjectSpace os) {
		super(os);
	}

	public void setText(String myText) {
		setPropertyValue("text", myText);
	}

	public String getText() {
		return text;
	}
}

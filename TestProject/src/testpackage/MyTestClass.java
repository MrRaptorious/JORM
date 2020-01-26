package testpackage;

import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Persistent;

public class MyTestClass extends PersistentObject {

	@Persistent
	private String text;

	@Persistent
	private TestRefClass refClass;

	public MyTestClass(ObjectSpace os) {
		super(os);
	}

	public void setText(String myText) {
		setPropertyValue("text", myText);
	}
	
	public String getText() {
		return text;
	}
	
	public void setrefClass(TestRefClass refCls) {
		setPropertyValue("refClass", refCls);
	}

	public TestRefClass getrefClass() {
		return refClass;
	}
}

package testpackage;

import jormCore.JormList;
import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;
import jormCore.Annotaions.Persistent;

public class TestA extends PersistentObject {

    @Association(name = "ListAandB")
    private JormList<TestB> testBList;

    @Persistent(name = "Name")
    private String name;

    public TestA(ObjectSpace os) {
        super(os);
    }

    public JormList<TestB> getTestBList() {
        return getList("testBList");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setPropertyValue("name", name);
    }
}

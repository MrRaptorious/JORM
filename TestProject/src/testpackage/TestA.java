package testpackage;

import jormCore.JormList;
import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.Annotaions.Association;

public class TestA extends PersistentObject {

    @Association(name = "ListAandB")
    private JormList<TestB> testBList;

    public TestA(ObjectSpace os) {
        super(os);
    }

    public JormList<TestB> getTestBList() {
        return getList("testBList");
    }
}

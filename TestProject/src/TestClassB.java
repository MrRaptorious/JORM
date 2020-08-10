import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.annotaions.Persistent;

@Persistent
public class TestClassB extends PersistentObject {
    private TestClassA TestClassA;


    public TestClassB(ObjectSpace os) {
        super(os);
    }

    public void setTestClassA(TestClassA testClassB) {
        TestClassA = testClassB;
    }

    public TestClassA getTestClassA() {
        return TestClassA;
    }
}

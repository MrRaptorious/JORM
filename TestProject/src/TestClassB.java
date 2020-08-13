import jormCore.ObjectSpace;
import jormCore.PersistentObject;
import jormCore.annotaions.Persistent;

@Persistent
public class TestClassB extends PersistentObject {
    private TestClassA TestClassA;
    private TestClassB TestClassB;

    public TestClassB(ObjectSpace os) {
        super(os);
    }

    public void setTestClassA(TestClassA testClassA) {
        TestClassA = testClassA;
    }

    public TestClassA getTestClassA() {
        return TestClassA;
    }

    public TestClassB getTestClassB() {
        return TestClassB;
    }

    public void setTestClassB(TestClassB testClassB) {
        TestClassB = testClassB;
    }
}

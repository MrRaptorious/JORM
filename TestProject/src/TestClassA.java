import jormCore.annotaions.*;
import jormCore.ObjectSpace;
import jormCore.PersistentObject;

public class TestClassA extends PersistentObject{

    @Persistent(name="Name")
    private String meinTestString;

    public TestClassA(ObjectSpace os) {
        super(os);
    }

    public void setMeinTestString(String meinTestString) {
        this.meinTestString = meinTestString;
    }

    public String getMeinTestString() {
        return meinTestString;
    }
}

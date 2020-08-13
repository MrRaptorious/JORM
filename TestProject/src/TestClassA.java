import jormCore.annotaions.*;
import jormCore.ObjectSpace;
import jormCore.PersistentObject;

public class TestClassA extends PersistentObject{

    @Persistent(name="Name")
    private String meinTestString;

    @Persistent(name="isCool")
    private boolean meinTestBoolean;

    @Persistent
    private TestClassB meinB;

    public TestClassA(ObjectSpace os) {
        super(os);
    }

    public void setMeinTestString(String meinTestString) {
        this.meinTestString = meinTestString;
    }

    public String getMeinTestString() {
        return meinTestString;
    }

    public void setmeinTestBoolean(boolean meinTestBoolean) {
        this.meinTestBoolean = meinTestBoolean;
    }

    public boolean getmeinTestBoolean() {
        return meinTestBoolean;
    }

    public void setMeinB(TestClassB B) {meinB = B; }


}

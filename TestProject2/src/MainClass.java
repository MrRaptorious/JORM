import jormCore.JormApplication;
import jormCore.ObjectSpace;

public class MainClass {

    public static void main(String[] args) {

    JormApplication app = JormApplication.getApplication();

    app.registerType(TestClassA.class);

    app.start();

    ObjectSpace os = app.createObjectSpace();


    var objs = os.getObjects(TestClassA.class);

    for(var ob : objs)
    {
        System.out.println(ob.getMeinTestString());
    }

    /*
    TestClassA obj = os.createObject(TestClassA.class);

    obj.setMeinTestString("Das ist ein Test");

    os.commitChanges();
*/

    }
}

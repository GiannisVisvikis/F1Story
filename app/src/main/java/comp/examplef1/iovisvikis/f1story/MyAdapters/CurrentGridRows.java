package comp.examplef1.iovisvikis.f1story.MyAdapters;

/**
 * Created by iovisvikis on 17/5/2017.
 */

public class CurrentGridRows{


    private Driver driver;
    private Constructor constructor;

    public Driver getDriver() {
        return driver;
    }

    public Constructor getConstructor() {
        return constructor;
    }


    public CurrentGridRows(Driver driver, Constructor constructor) {
        this.driver = driver;
        this.constructor = constructor;
    }


}

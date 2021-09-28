package singleton;

public class Singleton {

    private volatile static Singleton singleton;

    public Singleton getSingleton(){
        if (singleton == null) {
            synchronized (singleton) {
                singleton = new Singleton();
            }
        }
        return singleton;
    }
}

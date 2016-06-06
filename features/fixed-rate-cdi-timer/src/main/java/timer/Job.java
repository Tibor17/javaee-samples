package timer;

import javax.enterprise.context.ApplicationScoped;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

@ApplicationScoped
public class Job implements Runnable {

    public void run() {
        out.println("time process " + currentTimeMillis());
    }
}

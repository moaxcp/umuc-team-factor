package job.server;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import job.Job;
import job.JobStatus;

/**
 * A job that simply counts to 10 every second.
 */
public class FakeJob extends Job implements Serializable {

    @Override
    public void run() {
        status = JobStatus.RUNNING;

        for (int i = 1; status == JobStatus.RUNNING && i <= 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(FakeServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println(i + "...");
        }

        status = JobStatus.COMPLETE;
    }
}

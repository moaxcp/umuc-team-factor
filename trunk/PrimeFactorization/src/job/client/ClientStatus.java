/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.client;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import job.Job;
import job.JobStatus;

/**
 *
 * @author john
 */
public class ClientStatus {
    private Map<UUID, JobStatus> jobStatus;
    private UUID session;
    
    ClientStatus(Map<UUID, Job> jobs, UUID session) {
        this.session = session;
        for(UUID id : jobs.keySet()) {
            jobStatus.put(id, jobs.get(id).getStatus());
        }
        jobStatus = Collections.unmodifiableMap(jobStatus);
    }
    
    public UUID getSessionID() {
        return session;
    }
    
    public Map<UUID, JobStatus> getJobStatus() {
        return jobStatus;
    }
}

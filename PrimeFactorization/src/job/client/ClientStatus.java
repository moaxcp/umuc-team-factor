/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job.client;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import job.Job;
import job.JobStatus;

/**
 *
 * @author john
 */
public class ClientStatus implements Serializable {
    private Map<UUID, JobStatus> jobStatus;
    private UUID session;
    
    public ClientStatus(Map<UUID, Job> jobs, UUID session) {
        this.session = session;
        jobStatus = new HashMap<UUID, JobStatus>();
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

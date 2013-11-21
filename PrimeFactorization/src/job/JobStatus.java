package job;

/**
 * Represents the JobStatus.
 * 
 * @author john
 */
public enum JobStatus {
    /**
     * Job is new and has never been run.
     */
    NEW("New"),
    
    /**
     * Job is currently being executed.
     */
    RUNNING("Running"),
    
    /**
     * Job has been stopped.
     */
    STOPPED("Stopped"),
    
    /**
     * Job is complete.
     */
    COMPLETE("Complete");
    
    private String name;
    
    private JobStatus(String name) {
        this.name = name;
    }
    
    /**
     * returns the name of this JobStatus.
     * @return 
     */
    public String toString() {
        return name;
    }
}

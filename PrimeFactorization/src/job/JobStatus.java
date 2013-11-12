/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package job;

/**
 *
 * @author john
 */
public enum JobStatus {
    NEW("New"),
    RUNNING("Running"),
    STOPPED("Stopped"),
    COMPLETE("Complete");
    
    private String name;
    
    JobStatus(String name) {
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
}

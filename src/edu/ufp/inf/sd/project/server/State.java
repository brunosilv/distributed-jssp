package edu.ufp.inf.sd.project.server;

import java.io.Serializable;

public class State implements Serializable {
    public enum JobGroupState {
        CREATED,
        DISTRIBUTION,
        RECEIVED,
        STOPPED
    }

    private String msg;
    private String id;
    private JobGroupState state;


    /**
     * @param id
     * @param m
     */
    public State(String id, String m, JobGroupState jobGroupState) {
        this.id = id;
        this.msg = m;
        this.state = jobGroupState;
    }

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @return
     */
    public String getInfo() {
        return this.msg;
    }

    /**
     * @param m
     */
    public void setInfo(String m) {
        this.msg = m;
    }

    public JobGroupState getState() {
        return state;
    }

    public void setState(JobGroupState state) {
        this.state = state;
    }



}

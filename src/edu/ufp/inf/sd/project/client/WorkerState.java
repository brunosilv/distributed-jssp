package edu.ufp.inf.sd.project.client;

import java.io.Serializable;

public class WorkerState implements Serializable {
    private String msg;
    private Boolean isAvailable;

    public WorkerState(String msg, Boolean isAvailable) {
        this.msg = msg;
        this.isAvailable = isAvailable;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getAvailable() {
        return isAvailable;
    }

    public void setAvailable(Boolean available) {
        isAvailable = available;
    }
}

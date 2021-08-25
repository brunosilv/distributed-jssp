package edu.ufp.inf.sd.project.client;

import edu.ufp.inf.sd.project.server.SubjectRI;
import edu.ufp.inf.sd.project.server.Task;
import edu.ufp.inf.sd.project.util.tabusearch.TabuSearchJSSP;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {
    public String username;

    public WorkerState lastObserverState;

    public SubjectRI subjectRI;

    private Integer credits;

    private Integer workResult;

    private Task currentTask;


    public ObserverImpl(String username, Integer credits) throws RemoteException {
        super();
        this.username = username;
        this.credits = credits;
        this.lastObserverState = new WorkerState("Created", true);
    }


    /**
     * @desc updates worker state and runs TS algorithm
     * @throws RemoteException
     */
    @Override
    public void update() throws RemoteException {
        runTS();
        System.out.println(this.lastObserverState.getMsg() + " Available: " + this.lastObserverState.getAvailable());
    }


    /**
     * @desc changes the state and notify the jobgroup about the result that it had
     * @param result
     * @throws RemoteException
     */
    @Override
    public void notifyJobGroup(Integer result) throws RemoteException {
        changeMyState(this.username + " has finished");
        System.out.println(this.lastObserverState.getMsg() + " Available: " + this.lastObserverState.getAvailable());
        this.subjectRI.update(this, result);
    }

    @Override
    public void setState(WorkerState state) throws RemoteException {
        this.lastObserverState = state;
    }

    @Override
    public WorkerState getState() throws RemoteException {
        return this.lastObserverState;
    }

    @Override
    public Integer getWorkResult() throws RemoteException {
        return this.workResult;
    }

    @Override
    public Task getCurrentTask() throws RemoteException {
        return this.currentTask;
    }

    @Override
    public void setCurrentTask(Task task) throws RemoteException {
        this.currentTask = task;
    }

    /**
     * @desc runs the TabuSearch provided algorithm
     * @return
     * @throws RemoteException
     */
    @Override
    public int runTS() throws RemoteException {
        TabuSearchJSSP ts = new TabuSearchJSSP(this.currentTask.getPath());
        int makespan = ts.run();
        System.out.println(this.username + " has returned the value: " + makespan);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "[TS] Makespan for {0} = {1}", new Object[]{this.currentTask.getPath(), String.valueOf(makespan)});
        this.workResult = makespan;
        System.out.println("The work restult was: " + this.workResult);
        notifyJobGroup(makespan);
        return makespan;
    }

    @Override
    public void setJobGroup(SubjectRI subjectRI) throws RemoteException {
        this.subjectRI = subjectRI;
    }

    /**
     * @desc Add credits to this user
     * @param credits
     * @throws RemoteException
     */
    @Override
    public void addCredits(Integer credits) throws RemoteException {
        this.credits += credits;
        System.out.println(this.username + " as now " + this.credits + " credits! " + credits + " credits have been added.");
    }

    @Override
    public SubjectRI getJobGroup() throws RemoteException {
        return this.subjectRI;

    }

    /**
     * @desc changes the state
     * @param info
     * @throws RemoteException
     */
    @Override
    public void changeMyState(String info) throws RemoteException {
        this.lastObserverState.setAvailable(!this.lastObserverState.getAvailable());
        this.lastObserverState.setMsg(info);
    }

    @Override
    public String getUsername() {
        return username;
    }

    public SubjectRI getSubjectRI() {
        return subjectRI;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "User{" + "uname=" + username + ", credits=" + credits + '}';
    }
}


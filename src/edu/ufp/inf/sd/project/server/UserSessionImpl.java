package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.ObserverRI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class UserSessionImpl extends UnicastRemoteObject implements UserSessionRI {

    UserFactoryRI userFactoryRI;

    ClientUser user;

    public UserSessionImpl(UserFactoryRI userFactoryRI, ClientUser user) throws RemoteException {
        super();
        this.userFactoryRI = userFactoryRI;
        this.user = user;
    }

    /**
     * @desc logout from current session
     * @throws RemoteException
     */
    @Override
    public void logout() throws RemoteException {
        UserFactoryImpl userFactory = ((UserFactoryImpl) userFactoryRI);
        userFactory.getDb().removeSession(this);
    }

    @Override
    public void print(String msg) throws RemoteException {
        System.out.println(msg);
    }

    @Override
    public ClientUser getUser() throws RemoteException {
        return this.user;
    }

    public void setUser(ClientUser user) {
        this.user = user;
    }

    /**
     * @desc delete a JobGroup
     * @param subjectRI
     * @throws RemoteException
     */
    @Override
    public void deleteJobGroup(SubjectRI subjectRI) throws RemoteException {
        UserFactoryImpl userFactory = ((UserFactoryImpl) userFactoryRI);
        userFactory.getDb().removeJobGroup(subjectRI);
    }

    /**
     * @desc creates a JobGroup
     * @param task
     * @return
     * @throws RemoteException
     */
    @Override
    public SubjectRI createJobGroup(String name, String owner, Task task) throws RemoteException {
        SubjectImpl subjectRI = new SubjectImpl(name, owner, task, this);
        this.userFactoryRI.createJobGroup(subjectRI);
        return subjectRI;
    }

    /**
     * @desc logs all JobGroups
     * @throws RemoteException
     */
    @Override
    public void listJobGroups() throws RemoteException {
        this.userFactoryRI.listJobGroups();
    }

    @Override
    public ArrayList<ObserverRI> getWorkers() throws RemoteException {
        return this.userFactoryRI.GetWorkers();
    }

    @Override
    public void pauseJobGroup(SubjectRI subjectRI) throws RemoteException {
        subjectRI.pause();
    }

    @Override
    public void resumeJobGroup(SubjectRI subjectRI) throws RemoteException {
        subjectRI.resume();
    }

    /**
     * @desc removes worker from job group
     * @param subjectRI
     * @param observerRI
     * @throws RemoteException
     */
    @Override
    public void removeWorkerToJobGroup(SubjectRI subjectRI, ObserverRI observerRI) throws RemoteException {
        subjectRI.detach(observerRI);
    }

    /**
     * @desc removes credit from the owner of the job group
     * @param credits
     * @throws RemoteException
     */
    @Override
    public void removeCreditsFromOwner(Integer credits) throws RemoteException {
        Integer userCredits = this.user.getCredits();
        String username = this.user.getUname();
        this.user.setCredits(userCredits - credits);
    }

    /**
     * @desc attaches a worker to JobGroup
     * @param observerRI
     * @param subjectRI
     * @throws RemoteException
     */
    @Override
    public void attachWorkerToJobGroup(ObserverRI observerRI, SubjectRI subjectRI) throws RemoteException {
        subjectRI.attach(observerRI);
    }

    /**
     * @desc notify all worker from job group to start working
     * @param subjectRI
     * @throws RemoteException
     */
    @Override
    public void notifyAllWorkersOfJobGroup(SubjectRI subjectRI) throws RemoteException {
        subjectRI.notifyAllWorkers();
    }
}

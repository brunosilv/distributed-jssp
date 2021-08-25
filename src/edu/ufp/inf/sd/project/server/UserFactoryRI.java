package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.ObserverRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface UserFactoryRI extends Remote {
    public boolean register(String uname, String pw) throws RemoteException;

    public UserSessionRI login(String uname, String pw) throws RemoteException;

    public void createJobGroup(SubjectRI subjectRI) throws RemoteException;

    public void listJobGroups() throws RemoteException;

    public ArrayList<ObserverRI> GetWorkers() throws RemoteException;

}


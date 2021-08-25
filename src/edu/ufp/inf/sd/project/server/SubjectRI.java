package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.ObserverRI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SubjectRI extends Remote {
    public void print(String msg) throws RemoteException;

    public void attach(ObserverRI obsRI) throws RemoteException;

    public void detach(ObserverRI obsRI) throws RemoteException;

    public void setState(State state) throws RemoteException;

    public State getState() throws RemoteException;

    public Task getTask() throws RemoteException;

    public void notifyAllWorkers() throws RemoteException;

    public void update(ObserverRI observerRI, Integer result) throws RemoteException;

    public void pause() throws RemoteException;

    public void stop() throws RemoteException;

    public void resume() throws RemoteException;

    public void delete() throws RemoteException;

    public String getName()  throws RemoteException;
}


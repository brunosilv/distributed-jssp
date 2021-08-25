package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.ObserverRI;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBMockup implements Serializable {

    private ArrayList<ClientUser> clientUsers;
    private HashMap<String, UserSessionRI> sessions;
    private ArrayList<SubjectRI> subjectRIS;
    private ArrayList<ObserverRI> workers;


    public DBMockup() {
        this.clientUsers = new ArrayList<>();
        sessions = new HashMap<>();
        ClientUser clientUser = new ClientUser("bruno", "xpto");
        clientUser.setCredits(123456);
        clientUsers.add(clientUser);
        this.subjectRIS = new ArrayList<>();
        this.workers = new ArrayList<>();
    }

    public void register(String u, String p) {
        if (!exists(u, p)) {
            ClientUser clientUser = new ClientUser(u, p);
            System.out.println("DbMockup: " + clientUser.toString());
            clientUsers.add(clientUser);
        }
    }

    public boolean exists(String u, String p) {
        for (ClientUser usr : this.clientUsers) {
            if (usr.getUname().compareTo(u) == 0 && usr.getPassword().compareTo(p) == 0) {
                return true;
            }
        }
        return false;
    }

    public void addJobGroup(SubjectRI subjectRI) {
        this.subjectRIS.add(subjectRI);
    }

    public void addWorker(ObserverRI observerRI) {
        this.workers.add(observerRI);
    }

    public ArrayList<ClientUser> getClientUsers() {
        return clientUsers;
    }

    public HashMap<String, UserSessionRI> getSessions() {
        return sessions;
    }

    public void addSession(String username, UserSessionRI sessionRI) {
        sessions.put(username, sessionRI);
    }

    public void removeJobGroup(SubjectRI subjectRI) {
        if (this.subjectRIS.contains(subjectRI)) {
            for (SubjectRI jobGroup : this.subjectRIS) {
                if (jobGroup.equals(subjectRI)) {
                    this.subjectRIS.remove(jobGroup);
                }
            }
        }
    }

    public void removeSession(UserSessionRI sessionRI) {
        if (this.sessions.containsValue(sessionRI)) {
            for (String key : this.sessions.keySet()) {
                if (this.sessions.get(key).equals(sessionRI)) {
                    this.sessions.remove(key);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "The User {0} Logged out", new Object[]{key});
                }
            }
        }
    }

    public ClientUser getClientUser(String uname) throws RemoteException {
        for (ClientUser ClientUser : clientUsers) {
            if (ClientUser.getUname().equals(uname)) {
                return ClientUser;
            }
        }
        return null;
    }

    public ArrayList<SubjectRI> getSubjectRIS() {
        return this.subjectRIS;
    }

    public void setSubjectRIS(ArrayList<SubjectRI> subjectRIS) {
        this.subjectRIS = subjectRIS;
    }

    public ArrayList<ObserverRI> getWorkers() {
        return workers;
    }

    public void setWorkers(ArrayList<ObserverRI> workers) {
        this.workers = workers;
    }

    public void printJobGroups() throws RemoteException {
        for (SubjectRI subjectRI : this.subjectRIS) {
            System.out.println("Name: "+subjectRI.getName());
            System.out.println("State: "+subjectRI.getState().getState());
            System.out.println("Path: "+subjectRI.getTask().getPath());
        }
    }
}

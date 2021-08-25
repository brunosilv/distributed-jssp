package edu.ufp.inf.sd.project.server;

import edu.ufp.inf.sd.project.client.ObserverRI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class UserFactoryImpl extends UnicastRemoteObject implements UserFactoryRI {

    private DBMockup db;

    protected UserFactoryImpl() throws RemoteException {
        super();
        db = new DBMockup();
    }

    /**
     * @desc add a new job group to db
     * @param uname
     * @param pw
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean register(String uname, String pw) throws RemoteException {
        if (!db.exists(uname, pw)) {
            System.out.println(uname + " " + pw);
            db.register(uname, pw);
            return true;
        }
        return false;
    }

    /**
     * @desc generates the session
     * @param uname
     * @param pw
     * @return
     * @throws RemoteException
     */
    @Override
    public UserSessionRI login(String uname, String pw) throws RemoteException {
        if (db.exists(uname, pw)) {
            if (!this.db.getSessions().containsKey(uname)) {
                for (ClientUser user : db.getClientUsers()) {
                    if (user.getUname().equals(uname)) {
                        UserSessionRI userSessionRI = new UserSessionImpl(this, new ClientUser(uname, pw));
                        this.db.addSession(uname, userSessionRI);     // insere no hashmap de sessoes
                        return userSessionRI;
                    }
                }
            } else {
                return this.db.getSessions().get(uname);
            }
        }
        return null;
    }

    @Override
    public void createJobGroup(SubjectRI subjectRI) throws RemoteException {
        this.db.addJobGroup(subjectRI);
        this.db.printJobGroups();

    }

    @Override
    public void listJobGroups() throws RemoteException {
        this.db.printJobGroups();
    }

    @Override
    public ArrayList<ObserverRI> GetWorkers() throws RemoteException {
        return this.db.getWorkers();
    }

    public DBMockup getDb() {
        return db;
    }


}

package edu.ufp.inf.sd.project.client;

import edu.ufp.inf.sd.project.server.*;
import edu.ufp.inf.sd.project.util.rmisetup.SetupContextRMI;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class JobShopClient {

    private SetupContextRMI contextRMI;
    private UserFactoryRI userFactoryRI;
    boolean workersCreated = false;
    private int numberOfWorkers;
    private UserSessionRI userSessionRI;

    public static void main(String[] args) {
        if (args != null && args.length < 2) {
            System.err.println("usage: java [options] edu.ufp.sd.inf.rmi._01_helloworld.server.HelloWorldClient <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        } else {
            //1. ============ Setup client RMI context ============
            JobShopClient hwc=new JobShopClient(args);
            //2. ============ Lookup service ============
            hwc.lookupService();
            //3. ============ Play with service ============
            hwc.playService();
        }
    }

    public JobShopClient(String args[]) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //Create a context for RMI setup
            contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (RemoteException e) {
            Logger.getLogger(JobShopClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private Remote lookupService() {
        try {
            //Get proxy MAIL_TO_ADDR rmiregistry
            Registry registry = contextRMI.getRegistry();
            //Lookup service on rmiregistry and wait for calls
            if (registry != null) {
                //Get service url (including servicename)
                String serviceUrl = contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going MAIL_TO_ADDR lookup service @ {0}", serviceUrl);
                
                //============ Get proxy MAIL_TO_ADDR HelloWorld service ============
                userFactoryRI = (UserFactoryRI) registry.lookup(serviceUrl);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
                //registry = LocateRegistry.createRegistry(1099);
            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return userFactoryRI;
    }
    
    private void playService() {

        // generate random name from an array
        Boolean isDemo = true;
        Random generate = new Random();
        String[] name = {"Jose", "Bruno", "Manel", "Rui", "Ricardo", "Maria", "Susana", "Carla", "Dinis", "Rodrigo"};

        if (isDemo) {
            try {
                this.userFactoryRI.register("bruno", "xpto");
                UserSessionRI userSessionRI = this.userFactoryRI.login("bruno", "xpto");
                ClientUser client = userSessionRI.getUser();
                client.setCredits(1000);
                Task task= new Task("edu/ufp/inf/sd/project/data/abz5.txt", new File("edu/ufp/inf/sd/project/data/abz5.txt"));
                if(userSessionRI != null){
                    userSessionRI.print("Login Succeeded");
                    SubjectRI jobGroup = userSessionRI.createJobGroup("demo", client.getUname(),task);
                    // Creates 3 workers
                    ObserverImpl observer=new ObserverImpl(name[generate.nextInt(10)], 0);
                    ObserverImpl observer1=new ObserverImpl(name[generate.nextInt(10)], 0);
                    ObserverImpl observer2=new ObserverImpl(name[generate.nextInt(10)], 0);
                    // AttachWorkersToJobGroup
                    userSessionRI.attachWorkerToJobGroup(observer, jobGroup);
                    userSessionRI.attachWorkerToJobGroup(observer1, jobGroup);
                    userSessionRI.attachWorkerToJobGroup(observer2, jobGroup);
                    userSessionRI.notifyAllWorkersOfJobGroup(jobGroup);
                }
                userSessionRI.logout();

            } catch (RemoteException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            initialMenu();
        }
    }

    private void initialMenu() {
        while (true) {
            Scanner in = new Scanner(System.in);
            System.out.println("Initial menu");
            System.out.println("1 - User registration");
            System.out.println("2 - Authentication");
            System.out.println("0 - Exit");
            int op = in.nextInt();
            try {
                switch (op) {
                    case 1: {
                        try {
                            System.out.println("User registration");
                            System.out.println("Username: ");
                            String username = in.next();
                            System.out.println("Password : ");
                            String password = in.next();
                            boolean action = this.userFactoryRI.register(username, password);
                            if (action) {
                                System.out.println("Success");
                                op = -1;
                                break;
                            } else {
                                System.out.println("Error on registration");
                                break;
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 2: {
                        System.out.println("Login:");
                        System.out.println("Insert your Username : ");
                        String username = in.next();
                        System.out.println("Insert your password : ");
                        String password = in.next();

                         this.userSessionRI = this.userFactoryRI.login(username, password);
                        if (userSessionRI != null) {
                            try {
                                System.out.println("Welcome " + username + " !");
                                operationsMenu();
                                break;
                            } catch (Exception e) {
                                System.out.println("Error");
                            }
                        } else {
                            System.out.println("Error");
                        }
                        break;
                    }
                    case 0: {
                        System.exit(0);
                        break;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }

    }

    private void operationsMenu() {
        while (true) {
            Scanner in = new Scanner(System.in);
            System.out.println("Management menu");
            System.out.println("1 - Profile"); //Done
            System.out.println("2 - Buy credits"); //Done
            System.out.println("3 - Set workers"); //Done
            System.out.println("4 - Create Job Group"); //Done
            System.out.println("5 - Assign Works to Session Job"); //WIP
            System.out.println("0 - Exit"); //Done
            int op = in.nextInt();

            try {
                switch (op) {
                    case 1: {
                        try {
                            ClientUser client = userSessionRI.getUser();
                            System.out.println("DbMockup: " + client.getUname());
                            System.out.println("My profile : " + client.toString());
                            System.out.println("My workers : " + this.userFactoryRI.GetWorkers());
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                   case 2: {
                        System.out.println("Add credits to your session:");
                        System.out.println("Insert the amount of credits: ");
                        String credits = in.next();
                        userSessionRI.getUser().setCredits(Integer.parseInt(credits));
                        System.out.println(userSessionRI.toString());
                        break;
                    }
                    case 3: {
                        if (workersCreated) {
                            System.out.println("You can't change number of your workers");
                        } else {
                            Random generate = new Random();
                            String[] name = {"Jose", "Bruno", "Manel", "Rui", "Ricardo", "Maria", "Susana", "Carla", "Dinis", "Rodrigo"};

                            System.out.println("Add workers to your session:");
                            System.out.println("Insert the amount of workers: ");
                            String workers = in.next();
                            this.numberOfWorkers = Integer.parseInt(workers);
                            for (int i = 0; i < numberOfWorkers; i++) {
                                ObserverImpl observer=new ObserverImpl(name[generate.nextInt(10)], 0);
                                System.out.println(observer.toString());
                            }
                            this.workersCreated = true;
                        }
                        break;
                    }
                    case 4: {
                        Task task= new Task("edu/ufp/inf/sd/project/data/abz5.txt", new File("edu/ufp/inf/sd/project/data/abz5.txt"));
                        System.out.println("Create a new Job Group:");
                        System.out.println("Name of JobGroup: ");
                        String name = in.next();
                        userSessionRI.createJobGroup(name,userSessionRI.getUser().getUname(), task);
                        break;
                    }
                    case 5: {
                        this.userFactoryRI.listJobGroups();
                        break;
                    }
                    case 0: {
                        userSessionRI.logout();
                        System.exit(0);
                        break;
                    }
                    default: {
                        System.out.println("Error invalid option");
                        break;
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

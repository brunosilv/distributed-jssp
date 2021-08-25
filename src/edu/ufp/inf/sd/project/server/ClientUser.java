package edu.ufp.inf.sd.project.server;

import java.io.Serializable;

public class ClientUser implements Serializable {

    private String uname;
    private String password;
    private Integer credits;

    public ClientUser(String uname, String pword) {
        this.uname = uname;
        this.password = pword;
        this.credits = 0;
        System.out.println("this.credits: " + this.credits);
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pword) {
        this.password = pword;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "User{" + "uname=" + uname + ", password=" + password + ", credits=" + credits + '}';
    }

}

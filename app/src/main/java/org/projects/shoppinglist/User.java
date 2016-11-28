package org.projects.shoppinglist;

/**
 * Created by Nelly on 11/12/16.
 */

public class User {
    private String email;

    public User (){};

    public User(String email){
        this.email=email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

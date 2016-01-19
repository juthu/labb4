package com.example.serverutveckling.labb4;

/**
 * Created by Julia on 2016-01-18.
 */
public class User {

   private String username;
    private String facebookId;

    public User(String username, String facebookId){
        this.username = username;
        this.facebookId = facebookId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}

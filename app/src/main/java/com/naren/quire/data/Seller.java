
package com.naren.quire.data;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Seller implements Serializable
{

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("username")
    @Expose
    private Object username;
    @SerializedName("fb_user_id")
    @Expose
    private String fbUserId;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("profile_picture")
    @Expose
    private String profilePicture;
    private final static long serialVersionUID = 7633435045883475320L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Seller() {
    }

    /**
     * 
     * @param id
     * @param username
     * @param fbUserId
     * @param email
     * @param profilePicture
     * @param name
     */
    public Seller(Integer id, Object username, String fbUserId, String email, String name, String profilePicture) {
        super();
        this.id = id;
        this.username = username;
        this.fbUserId = fbUserId;
        this.email = email;
        this.name = name;
        this.profilePicture = profilePicture;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getUsername() {
        return username;
    }

    public void setUsername(Object username) {
        this.username = username;
    }

    public String getFbUserId() {
        return fbUserId;
    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

}

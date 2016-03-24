package com.thoughtworks.mindit.authentication;

import android.graphics.Bitmap;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String id;
    private String name;
    private String photoUrl;
    private Bitmap profilePhoto;
    private String email;
    private String grantedScopes;
    private String serverAuthCode;
    private String idToken;
    private String profilePhotoPathInMemory;
    private JSONObject user;


    public User(String id, String name, String photoUrl, String email, String grantedScopes, String serverAuthCode, String idToken) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.email = email;
        this.grantedScopes = grantedScopes;
        this.serverAuthCode = serverAuthCode;
        this.idToken = idToken;
    }

    public User(JSONObject user) {
        this.user = user;
        try {
            this.name = user.getString("name");
            this.email = user.getString("email");
            this.id = user.getString("id");
            this.grantedScopes = user.getString("grantedScope");
            this.serverAuthCode = user.getString("serverAuthCode");
            this.idToken = user.getString("idToken");
            this.photoUrl = user.getString("photoUrl");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public User(GoogleSignInAccount account) {
        JSONObject user = new JSONObject();
        try {
            user.put("name", account.getDisplayName());
            this.name = account.getDisplayName();
            user.put("id", account.getId());
            this.id = account.getId();
            user.put("email", account.getEmail());
            this.email = account.getEmail();
            user.put("grantedScope", account.getGrantedScopes());
            this.grantedScopes = account.getGrantedScopes().toString();
            user.put("serverAuthCode", account.getServerAuthCode());
            this.serverAuthCode = account.getServerAuthCode();
            user.put("idToken", account.getIdToken());
            this.idToken = account.getIdToken();
            user.put("photoUrl", account.getPhotoUrl());
            if (account.getPhotoUrl() != null)   {
                this.photoUrl = account.getPhotoUrl().toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.user = user;
    }

    @Override
    public String toString() {
        return this.user.toString();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getGrantedScopes() {
        return grantedScopes;
    }

    public String getServerAuthCode() {
        return serverAuthCode;
    }

    public String getIdToken() {
        return idToken;
    }

    public JSONObject getJson() {
        return user;
    }

}

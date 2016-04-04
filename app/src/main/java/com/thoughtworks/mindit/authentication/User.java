package com.thoughtworks.mindit.authentication;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class User {
    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private String grantedScopes;
    private String serverAuthCode;
    private String idToken;


    public User(String id, String name, String email, String photoUrl, String grantedScopes, String serverAuthCode, String idToken) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.email = email;
        this.grantedScopes = grantedScopes;
        this.serverAuthCode = serverAuthCode;
        this.idToken = idToken;
    }


    public User(GoogleSignInAccount account) {
        this.name = account.getDisplayName();
        this.id = account.getId();
        this.email = account.getEmail();
        this.grantedScopes = account.getGrantedScopes().toString();
        this.serverAuthCode = account.getServerAuthCode();
        this.idToken = account.getIdToken();
        if (account.getPhotoUrl() != null) {
            this.photoUrl = account.getPhotoUrl().toString();
        }
    }

    @Override
    public String toString() {
        String user = "id : "+this.id+
                        "\nname : "+this.name+
                        "\nphotoUrl : "+this.photoUrl+
                        "\nemail : "+this.email+
                        "\ngrantedScopes : "+this.grantedScopes+
                        "\nserver auth code : "+this.serverAuthCode+
                        "\nidToken : "+this.idToken;
        return user;
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

}

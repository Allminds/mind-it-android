package com.thoughtworks.mindit.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thoughtworks.mindit.constant.Authentication;
import com.thoughtworks.mindit.view.HomeActivity;

public class SessionManager {
    SharedPreferences sessionSharedPreferences;
    SharedPreferences.Editor editor;
    private User currentUser;
    private Context _context;
    private int PRIVATE_MODE = 0;
    private static SessionManager instance;

    private SessionManager(Context context) {
        this._context = context;
        sessionSharedPreferences = _context.getSharedPreferences(Authentication.SESSION_PREFERENCES, PRIVATE_MODE);
        editor = sessionSharedPreferences.edit();
        instance = this;
    }

    public static SessionManager getInstance(Context context){
        if(instance == null){
            return new SessionManager(context);
        }
        else {
            return instance;
        }
    }
    public void createLoginSession(User user) {
        editor.clear();
        editor.commit();
        editor.putBoolean(Authentication.IS_LOGIN, true);
        editor.putString(Authentication.USER_ID, user.getId());
        editor.putString(Authentication.USER_NAME, user.getDisplayName());
        editor.putString(Authentication.USER_EMAIL, user.getEmail());
        editor.putString(Authentication.USER_ID_PHOTO_URL, user.getPhotoUrl());
        editor.putString(Authentication.USER_ID_SERVER_AUTH_CODE, user.getServerAuthCode());
        editor.putString(Authentication.USER_ID_TOKEN, user.getIdToken());
        editor.putString(Authentication.USER_ID_GRANTED_SCOPE, user.getGrantedScopes());
        editor.commit();
    }

    public User getUserDetails() {
        if (this.currentUser == null) {
            String id = sessionSharedPreferences.getString(Authentication.USER_ID, null);
            String name = sessionSharedPreferences.getString(Authentication.USER_NAME, null);
            String photoUrl = sessionSharedPreferences.getString(Authentication.USER_ID_PHOTO_URL, null);
            String email = sessionSharedPreferences.getString(Authentication.USER_EMAIL, null);
            String grantedScopes = sessionSharedPreferences.getString(Authentication.USER_ID_SERVER_AUTH_CODE, null);
            String serverAuthCode = sessionSharedPreferences.getString(Authentication.USER_ID_SERVER_AUTH_CODE, null);
            String idToken = sessionSharedPreferences.getString(Authentication.USER_ID_TOKEN, null);

            this.currentUser = new User(id, name, email, photoUrl, grantedScopes, serverAuthCode, idToken);
            return this.currentUser;
        }
        return this.currentUser;
    }

    public void logoutUser() {
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, HomeActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }

    public boolean isLoggedIn() {
        if (sessionSharedPreferences == null || !sessionSharedPreferences.contains(Authentication.IS_LOGIN)) {
            return false;
        }
        return sessionSharedPreferences.getBoolean(Authentication.IS_LOGIN, false);
    }

}

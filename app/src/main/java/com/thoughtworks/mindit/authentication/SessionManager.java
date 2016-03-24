package com.thoughtworks.mindit.authentication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.thoughtworks.mindit.view.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager {
    private User currentUser;
    public static final String USER = "user";
    private static final String PREF_NAME = "USER_PREFERENCE";
    private static final String IS_LOGIN = "IsLoggedIn";
    SharedPreferences sessionPreferences;
    SharedPreferences.Editor editor;
    private Context _context;
    private int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        sessionPreferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sessionPreferences.edit();
    }

    public void createLoginSession(User user) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(USER, user.getJson().toString());
        editor.commit();
    }

    public void checkLogin() {
        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, HomeActivity.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }

    public User getUserDetails() {
        if(this.currentUser == null) {
            String jsonString = sessionPreferences.getString(USER, null);
            JSONObject userJson = null;
            try {
                userJson = new JSONObject(jsonString);
                this.currentUser = new User(userJson);
                return this.currentUser;
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
        if (sessionPreferences == null || !sessionPreferences.contains(IS_LOGIN)) {
            return false;
        }
        return sessionPreferences.getBoolean(IS_LOGIN, false);
    }
}

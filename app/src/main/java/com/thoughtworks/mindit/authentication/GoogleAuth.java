package com.thoughtworks.mindit.authentication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.thoughtworks.mindit.constant.MindIt;

import org.json.JSONException;
import org.json.JSONObject;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.ResultListener;

public class GoogleAuth implements GoogleApiClient.OnConnectionFailedListener,MeteorCallback {
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    private final AppCompatActivity appCompatActivity;
    private final Context context;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount account;
    private User user;
    private SessionManager sessionManager;
    private Meteor mMeteor;

    public GoogleAuth(AppCompatActivity appCompatActivity, Context context) {
        this.appCompatActivity = appCompatActivity;
        this.context = context;
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(appCompatActivity /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        sessionManager = SessionManager.getInstance(context);
        if(sessionManager.isLoggedIn()) {
            this.user = sessionManager.getUserDetails();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Toast.makeText(context, "" + connectionResult, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    public void signIn() {
        if(sessionManager.isLoggedIn()){
            this.user = sessionManager.getUserDetails();
            OnAuthenticationChanged onAuthenticationChanged = (OnAuthenticationChanged) context;
            onAuthenticationChanged.onSignedIn(this.user);
        }
        else{
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            appCompatActivity.startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    public void signOut() {

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
//                            Toast.makeText(context, "Logged Out User : "+user.getDisplayName(), Toast.LENGTH_SHORT).show();
                            sessionManager.logoutUser();
                            OnAuthenticationChanged authenticationChanged = (OnAuthenticationChanged) context;
                            authenticationChanged.onSignedOut();
                        }
                    }
                });
    }

    public void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        Toast.makeText(context, "Revoked Access to MindIt", Toast.LENGTH_SHORT).show();
                        // [END_EXCLUDE]
                    }
                });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            account = result.getSignInAccount();
            this.user = new User(account);
            sessionManager.createLoginSession(this.user);
            mMeteor = new Meteor(context, MindIt.WEB_SOCKET_LOCAL);
            mMeteor.setCallback(this);
        } else {
            Toast.makeText(context, "Signed Error: " + result.getStatus(), Toast.LENGTH_SHORT).show();
        }
    }

    public User getUser() {
        return this.user;
    }

    public boolean isSignedIn() {
        return sessionManager.isLoggedIn();
    }

    @Override
    public void onConnect(boolean b) {
        createUser(this.user);
    }

    private void createUser(final User user) {
        String userJson = null;
        try {
             userJson = getJson(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final User loggedInUser = this.user;
        mMeteor.call("createUserFromAdmin", new Object[]{userJson}, new ResultListener() {
            @Override
            public void onSuccess(String s) {
                mMeteor.disconnect();
                OnAuthenticationChanged authenticationChanged = (OnAuthenticationChanged) context;
                authenticationChanged.onSignedIn(loggedInUser);
            }

            @Override
            public void onError(String s, String s1, String s2) {
                Toast.makeText(context,"Login Failed",Toast.LENGTH_SHORT).show();
                mMeteor.disconnect();
            }
        });
    }

    private String getJson(User user) throws JSONException {

        JSONObject name = new JSONObject();
        name.put("name", user.getDisplayName());
        JSONObject google = new JSONObject();
        google.put("id", user.getId());
        google.put("name", user.getDisplayName());
        google.put("picture", user.getPhotoUrl());
        google.put("verified_email", true);
        google.put("email", user.getEmail());
        JSONObject ggl = new JSONObject();
        ggl.put("google", google);
        ggl.put("profile", name);
        JSONObject services = new JSONObject();
        services.put("services", ggl);
        return services.toString();
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onDataAdded(String s, String s1, String s2) {

    }

    @Override
    public void onDataChanged(String s, String s1, String s2, String s3) {

    }

    @Override
    public void onDataRemoved(String s, String s1) {

    }

    @Override
    public void onException(Exception e) {

    }
}

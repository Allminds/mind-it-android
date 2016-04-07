package com.thoughtworks.mindit.authentication;

public interface OnAuthenticationChanged {

    public void onSignedIn(User user);
    public void onSignedOut();
    public void onRevokedAccess();
    public void onSignInRequest(MindmapRequest request);
}

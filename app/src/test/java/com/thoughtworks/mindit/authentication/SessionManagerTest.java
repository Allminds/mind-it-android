package com.thoughtworks.mindit.authentication;

import android.content.Context;
import android.content.SharedPreferences;

import com.thoughtworks.mindit.constant.Authentication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {
    @Mock
    Context context;
    @Mock
    SharedPreferences sharedPreferences;
    @Mock
    SharedPreferences.Editor e;


    @Test
    public void testCreateLoginSession() throws Exception {
        // Mock Object Setup
        User user = new User("_id", "mindit", "mindit@mindit.com", "www.mindit.xyz/logo.png", "google drive", "ws:/serverauthcode", "id_token_123");
        when(context.getSharedPreferences(Authentication.SESSION_PREFERENCES, 0)).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(e);

        SessionManager sessionManager = new SessionManager(context);
        sessionManager.createLoginSession(user);

        verify(e).putBoolean(Authentication.IS_LOGIN, true);
        verify(e).putString(Authentication.USER_ID, user.getId());
    }

    @Test
    public void testGetUserDetails() throws Exception {
        User user = new User("_id", "mindit", "mindit@mindit.com", "www.mindit.xyz/logo.png", "google drive", "ws:/serverauthcode", "id_token_123");

        when(context.getSharedPreferences(Authentication.SESSION_PREFERENCES, 0)).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(e);
        when(sharedPreferences.getString(Authentication.USER_ID, null)).thenReturn(user.toString());

        SessionManager sessionManager = new SessionManager(context);
        sessionManager.getUserDetails();
        verify(sharedPreferences).getString(Authentication.USER_ID,null);
    }

}

package com.thoughtworks.mindit.mindit.PublishSubscribe;

import javax.security.auth.Subject;

public interface IObserver {

    //method to update the observer, used by subject
    public void update();
}

package com.thoughtworks.mindit.PublishSubscribe;

public interface IObserver {

    //method to update the observer, used by subject
    void update(String updateOption, String updateParameter);
}

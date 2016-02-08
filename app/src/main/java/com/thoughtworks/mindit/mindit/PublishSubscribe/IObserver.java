package com.thoughtworks.mindit.mindit.PublishSubscribe;

public interface IObserver {

    //method to update the observer, used by subject
    void update(int updateOption, String updateParameter);
}

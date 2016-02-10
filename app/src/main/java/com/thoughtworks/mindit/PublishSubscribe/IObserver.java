package com.thoughtworks.mindit.PublishSubscribe;

public interface IObserver {

    //method to update the observer, used by subject
    void update(int updateOption, String updateParameter);
}

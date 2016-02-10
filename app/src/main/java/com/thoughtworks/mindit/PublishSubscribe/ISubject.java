package com.thoughtworks.mindit.PublishSubscribe;

public interface ISubject {

    //methods to register and unregister observers
    void register(IObserver obj);

    void unregister(IObserver obj);

    //method to notify observers of change
    void notifyObservers();
}
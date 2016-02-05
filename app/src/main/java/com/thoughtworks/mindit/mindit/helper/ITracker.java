package com.thoughtworks.mindit.mindit.helper;

public interface ITracker {
    void onAdded(String collectionName, String documentID, String fieldsJson);

    void onChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson);

    void onRemoved(String collectionName, String documentID);
}

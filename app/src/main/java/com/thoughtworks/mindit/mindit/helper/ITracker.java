package com.thoughtworks.mindit.mindit.helper;

public interface ITracker {
    public void onAdded (String collectionName, String documentID, String fieldsJson);
    public void onChanged (String collectionName, String documentID, String updatedValuesJson, String removedValuesJson);
    public void onRemoved (String collectionName, String documentID);
}

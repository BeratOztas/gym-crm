package com.epam.gym_crm.utils;


public enum EntityType {
 TRAINEE("trainee"),   
 TRAINER("trainer"),
 TRAINING("training");

 private final String namespace; // Optional: To store a string representation if needed elsewhere

 EntityType(String namespace) {
     this.namespace = namespace;
 }

 public String getNamespace() {
     return namespace;
 }
}

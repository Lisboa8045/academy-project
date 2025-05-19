package com.academy.exceptions;

public class ServiceTypeNotFoundException extends RuntimeException {
  public ServiceTypeNotFoundException(Long id) {
    super("Service type with ID " + id + " not found.");
  }
}

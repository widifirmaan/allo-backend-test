package com.allobank.splitbill.exception;

public class GroupNotFoundException extends RuntimeException {

  public GroupNotFoundException(String groupId) {
    super("Grup tidak ditemukan: " + groupId);
  }
}

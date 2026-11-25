package com.nexa.chat.backend.groups;

public class GroupProblem extends RuntimeException {
  public final int status;
  public final String code;

  public GroupProblem(int status, String code, String message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

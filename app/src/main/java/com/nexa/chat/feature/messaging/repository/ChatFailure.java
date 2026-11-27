package com.nexa.chat.feature.messaging.repository;
/** PROPOSED domain failure. Retry eligibility is not an automatic retry scheduler. */
public final class ChatFailure extends IllegalStateException {
 public enum Code { UNAUTHENTICATED, FORBIDDEN, CONFLICT, OFFLINE, TIMEOUT, INVALID_ARGUMENT }
 private final Code code;
 public ChatFailure(Code code){super(code.name());this.code=code;}
 public Code getCode(){return code;}
 public boolean isRetryable(){return code==Code.OFFLINE||code==Code.TIMEOUT;}
}

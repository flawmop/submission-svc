package com.insilicosoft.portal.svc.submission.persistence.entity;

import com.insilicosoft.portal.svc.submission.value.MessageLevel;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class Message {

  @Enumerated(EnumType.STRING)
  private MessageLevel level;

  private String message;

  protected Message() {}

  public Message(MessageLevel level, String message) {
    this.level = level;
    this.message = message;
  }

  /**
   * @return the level
   */
  public MessageLevel getLevel() {
    return level;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "Message [level=" + level + ", message=" + message + "]";
  }

}
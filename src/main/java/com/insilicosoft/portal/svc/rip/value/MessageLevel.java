package com.insilicosoft.portal.svc.rip.value;

public enum MessageLevel {

  TRACE(0),
  DEBUG(1),
  INFO(2),
  WARN(3),
  ERROR(4),
  FATAL(5);

  private final int order;

  MessageLevel(int order) {
    this.order = order;
  }

  /**
   * @return the order
   */
  public int getOrder() {
    return order;
  }

}
package org.beholder.events.remote;

import org.beholder.events.RemoteEvent;
import org.beholder.topology.ClusterNode;

public class HeartbeatEvent implements RemoteEvent {

  private final ClusterNode sender;

  public HeartbeatEvent(ClusterNode sender) {
    this.sender = sender;
  }

  @Override
  public ClusterNode getSender() {
    return sender;
  }
}

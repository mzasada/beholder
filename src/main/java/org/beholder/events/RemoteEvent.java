package org.beholder.events;

import org.beholder.topology.ClusterNode;

public interface RemoteEvent {

  ClusterNode getSender();

  ClusterNode getReceiver();
}

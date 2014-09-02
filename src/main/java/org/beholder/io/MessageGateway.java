package org.beholder.io;

import org.beholder.events.RemoteEvent;
import org.beholder.topology.ClusterNode;

public interface MessageGateway {

  <M extends RemoteEvent> void sendTo(ClusterNode node, M message);
}

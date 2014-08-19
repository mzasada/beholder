package org.beholder.io;

public interface TopologyAwareMessageBroker extends MessageBroker {

  <M extends Message> void broadcast(M message);
}

package org.beholder.topology;

import org.beholder.events.RemoteEvent;
import org.beholder.topology.ClusterNode;
import rx.Observable;

public interface MessageGateway {

  <M extends RemoteEvent> void sendTo(ClusterNode node, M message);

  <M extends RemoteEvent> Observable<M> remoteEventStream();
}

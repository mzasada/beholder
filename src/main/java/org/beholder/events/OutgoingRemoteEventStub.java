package org.beholder.events;

import org.beholder.topology.ClusterNode;
import rx.Observable;

public interface OutgoingRemoteEventStub {
  Observable<ClusterNode> toAllFollowers();

  Observable<ClusterNode> toLeader();

  <R extends RemoteEvent> Observable<ClusterNode> toSenderOf(R event);
}

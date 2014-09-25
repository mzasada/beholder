package org.beholder.topology.standalone;

import org.beholder.events.RemoteEvent;
import org.beholder.topology.MessageGateway;
import org.beholder.topology.ClusterNode;
import rx.Observable;

import java.util.Map;

public class LocalMessageGateway implements MessageGateway {

  private final Map<ClusterNode, EventSink> router;
  private final ClusterNode self;

  public LocalMessageGateway(Map<ClusterNode, EventSink> router, ClusterNode self) {
    this.router = router;
    this.self = self;
  }

  @Override
  public <M extends RemoteEvent> void sendTo(ClusterNode node, M message) {
    router.get(node).emit(message);
  }

  @Override
  public <M extends RemoteEvent> Observable<M> remoteEventStream() {
    @SuppressWarnings("unchecked")
    Observable.OnSubscribe<M> handler = (Observable.OnSubscribe<M>) router.get(self);
    return Observable.create(handler);
  }
}

package org.beholder.events;

import com.google.common.eventbus.EventBus;
import org.beholder.io.MessageBroker;
import org.beholder.topology.ClusterNode;
import org.beholder.topology.ClusterTopology;
import rx.Observable;
import rx.Subscriber;

public class EventBroker {
  private final EventBus eventBus = new EventBus();
  private final MessageBroker messageBroker;
  private final ClusterTopology clusterTopology;

  public EventBroker(MessageBroker messageBroker, ClusterTopology clusterTopology) {
    this.messageBroker = messageBroker;
    this.clusterTopology = clusterTopology;
  }

  public <E extends LocalEvent> void send(E event) {

  }

  public <E extends RemoteEvent> OutgoingRemoteEventStub send(E event) {
    return new OutgoingRemoteEventStub() {
      @Override
      public Observable<ClusterNode> toLeader() {
        messageBroker.sendTo(clusterTopology.getLeaderNode(), null);
        return Observable.just(clusterTopology.getLeaderNode());
      }

      @Override
      public Observable<ClusterNode> toAllFollowers() {
        return Observable.create((Subscriber<? super ClusterNode> subscriber) -> {
          if (!subscriber.isUnsubscribed()) {
            clusterTopology.getSecondaryNodes().forEach(node -> {
              messageBroker.sendTo(node, null);
              subscriber.onNext(node);
            });
            subscriber.onCompleted();
          }
        });
      }

      @Override
      public <R extends RemoteEvent> Observable<ClusterNode> toSenderOf(R event) {
        messageBroker.sendTo(event.getSender(), null);
        return Observable.just(event.getSender());
      }
    };
  }

  public void register(Object object) {
    eventBus.register(object);
  }

  public void unregister(Object object) {
    eventBus.unregister(object);
  }
}
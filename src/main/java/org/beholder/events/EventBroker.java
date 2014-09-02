package org.beholder.events;

import com.google.common.eventbus.EventBus;
import org.beholder.io.MessageGateway;
import org.beholder.topology.ClusterNode;
import org.beholder.topology.ClusterTopology;
import rx.Observable;
import rx.Subscriber;

public class EventBroker {
  private final EventBus eventBus;
  private final EventRegistry eventRegistry;
  private final MessageGateway messageGateway;
  private final ClusterTopology clusterTopology;

  public EventBroker(EventBus eventBus,
                     EventRegistry eventRegistry,
                     MessageGateway messageGateway,
                     ClusterTopology clusterTopology) {
    this.eventBus = eventBus;
    this.eventRegistry = eventRegistry;
    this.messageGateway = messageGateway;
    this.clusterTopology = clusterTopology;
  }

  public <E extends LocalEvent> void send(E event) {

  }

  public <E extends RemoteEvent> OutgoingRemoteEventStub send(Class<E> eventType) {
    return new OutgoingRemoteEventStub() {
      @Override
      public Observable<ClusterNode> toAllFollowers() {
        return Observable.create((Subscriber<? super ClusterNode> subscriber) -> {
          if (!subscriber.isUnsubscribed()) {
            clusterTopology.getSecondaryNodes().forEach(node -> {
              messageGateway.sendTo(node, eventRegistry.create(eventType));
              subscriber.onNext(node);
            });
            subscriber.onCompleted();
          }
        });
      }

      @Override
      public <R extends RemoteEvent> Observable<ClusterNode> toSenderOf(R event) {
        messageGateway.sendTo(event.getSender(), eventRegistry.create(eventType));
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
package org.beholder.events;

import com.google.common.eventbus.EventBus;
import org.beholder.io.MessageGateway;
import org.beholder.topology.ClusterNode;
import org.beholder.topology.ClusterTopology;
import rx.Observable;
import rx.Subscriber;

public class EventBrokerImpl implements EventBroker {
  private final EventBus eventBus;
  private final EventRegistry eventRegistry;
  private final MessageGateway messageGateway;
  private final ClusterTopology clusterTopology;

  public EventBrokerImpl(EventBus eventBus,
                         EventRegistry eventRegistry,
                         MessageGateway messageGateway,
                         ClusterTopology clusterTopology) {
    this.eventBus = eventBus;
    this.eventRegistry = eventRegistry;
    this.messageGateway = messageGateway;
    this.clusterTopology = clusterTopology;

    this.messageGateway.remoteEventStream().subscribe(eventBus::post);
  }

  @Override
  public <E extends LocalEvent> void sendLocalEvent(Class<E> eventType) {
    eventBus.post(eventRegistry.localEvent(eventType));
  }

  @Override
  public <E extends RemoteEvent> OutgoingRemoteEventStub sendRemoteEvent(Class<E> eventType) {
    return new OutgoingRemoteEventStub() {
      @Override
      public Observable<ClusterNode> toAllFollowers() {
        return Observable.create((Subscriber<? super ClusterNode> subscriber) -> {
          if (!subscriber.isUnsubscribed()) {
            clusterTopology.getSecondaryNodes().forEach(node -> {
              messageGateway.sendTo(node, eventRegistry.remoteEvent(eventType));
              subscriber.onNext(node);
            });
            subscriber.onCompleted();
          }
        });
      }

      @Override
      public <R extends RemoteEvent> Observable<ClusterNode> toSenderOf(R event) {
        messageGateway.sendTo(event.getSender(), eventRegistry.remoteEvent(eventType));
        return Observable.just(event.getSender());
      }
    };
  }

  @Override
  public void register(Object object) {
    eventBus.register(object);
  }

  @Override
  public void unregister(Object object) {
    eventBus.unregister(object);
  }
}
package org.beholder.events;

public interface EventBroker {
  <E extends LocalEvent> void sendLocalEvent(Class<E> eventType);

  <E extends RemoteEvent> OutgoingRemoteEventStub sendRemoteEvent(Class<E> eventType);

  void register(Object object);

  void unregister(Object object);
}

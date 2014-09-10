package org.beholder.events;

import com.google.common.collect.ImmutableMap;
import org.beholder.events.local.StartElectionEvent;
import org.beholder.events.remote.HeartbeatEvent;
import org.beholder.topology.ClusterTopology;

import java.util.function.Supplier;

public class EventRegistry {

  private final ImmutableMap<Class<?>, Supplier<? extends RemoteEvent>> remoteEvents;
  private final ImmutableMap<Class<?>, Supplier<? extends LocalEvent>> localEvents;

  public EventRegistry(ClusterTopology clusterTopology) {
    this.remoteEvents = ImmutableMap.<Class<?>, Supplier<? extends RemoteEvent>>builder()
        .put(HeartbeatEvent.class, () -> new HeartbeatEvent(clusterTopology.self()))
        .build();

    this.localEvents = ImmutableMap.<Class<?>, Supplier<? extends LocalEvent>>builder()
        .put(StartElectionEvent.class, StartElectionEvent::new)
        .build();
  }

  public RemoteEvent remoteEvent(Class<? extends RemoteEvent> type) {
    return remoteEvents.get(type).get();
  }

  public LocalEvent localEvent(Class<? extends LocalEvent> type) {
    return localEvents.get(type).get();
  }
}

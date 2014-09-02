package org.beholder.events;

import com.google.common.collect.ImmutableMap;
import org.beholder.events.remote.HeartbeatEvent;
import org.beholder.topology.ClusterTopology;

import java.util.function.Supplier;

public class EventRegistry {

  private final ImmutableMap<Class<?>, Supplier<? extends RemoteEvent>> remoteEvents;

  public EventRegistry(ClusterTopology clusterTopology) {
    this.remoteEvents = ImmutableMap.<Class<?>, Supplier<? extends RemoteEvent>>builder()
        .put(HeartbeatEvent.class, () -> new HeartbeatEvent(clusterTopology.self()))
        .build();
  }

  public RemoteEvent create(Class<? extends RemoteEvent> type) {
    return remoteEvents.get(type).get();
  }
}

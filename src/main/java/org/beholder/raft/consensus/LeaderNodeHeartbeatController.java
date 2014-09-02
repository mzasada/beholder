package org.beholder.raft.consensus;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.beholder.events.EventBroker;
import org.beholder.events.RemoteEvent;
import org.beholder.time.TimeService;
import org.beholder.topology.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class LeaderNodeHeartbeatController {
  private static final Logger LOGGER = LoggerFactory.getLogger(LeaderNodeHeartbeatController.class);
  private static final long DEFAULT_HEARTBEAT_TIMEOUT_MILLIS = 200; //TODO: random?

  private final Cache<ClusterNode, LocalDateTime> heartbeatCache = CacheBuilder.newBuilder()
      .removalListener(this::handleHeartbeatTimeout)
      .expireAfterWrite(DEFAULT_HEARTBEAT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
      .build();

  private final EventBroker eventBroker;
  private final TimeService timeService;

  public LeaderNodeHeartbeatController(EventBroker eventBroker,
                                       TimeService timeService) {
    this.eventBroker = eventBroker;
    this.timeService = timeService;

    beginHeartbeatWithAllFollowers();
  }

  private void beginHeartbeatWithAllFollowers() {
    eventBroker.send(new RemoteEvent() {
      @Override
      public ClusterNode getSender() {
        return null;
      }

      @Override
      public ClusterNode getReceiver() {
        return null;
      }
    }).toAllFollowers().subscribe(follower -> heartbeatCache.put(follower, timeService.now()));
  }

  private void handleHeartbeatTimeout(RemovalNotification<ClusterNode, LocalDateTime> notification) {
    LOGGER.info("Leader node didn't receive a heartbeat from {}", notification.getKey());
  }

}

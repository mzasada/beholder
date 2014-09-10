package org.beholder.consensus.election;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.eventbus.Subscribe;
import org.beholder.events.EventBroker;
import org.beholder.events.remote.HeartbeatEvent;
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

    eventBroker.register(this);
    beginHeartbeatWithAllFollowers();
  }

  private void beginHeartbeatWithAllFollowers() {
    eventBroker
        .sendRemoteEvent(HeartbeatEvent.class)
        .toAllFollowers()
        .subscribe(follower -> heartbeatCache.put(follower, timeService.now()));
  }

  @Subscribe
  public void handleHeartbeatEvent(HeartbeatEvent event) {
    LOGGER.info("Leader node received a heartbeat from {}", event.getSender());
    eventBroker
        .sendRemoteEvent(HeartbeatEvent.class)
        .toSenderOf(event)
        .subscribe(follower -> heartbeatCache.put(follower, timeService.now()));
  }

  private void handleHeartbeatTimeout(RemovalNotification<ClusterNode, LocalDateTime> notification) {
    LOGGER.info("Leader node didn't receive a heartbeat from {} since {}", notification.getKey(), notification.getValue());
  }

}

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

public class FollowerHeartbeatController {
  private static final Logger LOGGER = LoggerFactory.getLogger(FollowerHeartbeatController.class);

  private final Cache<ClusterNode, LocalDateTime> heartbeatCache;
  private final EventBroker eventBroker;
  private final TimeService timeService;

  public FollowerHeartbeatController(
      TimeService timeService,
      long electionTimeoutInMillis, EventBroker eventBroker) {
    this.timeService = timeService;
    this.eventBroker = eventBroker;
    this.heartbeatCache = CacheBuilder.newBuilder()
        .removalListener(this::handleHeartbeatTimeout)
        .expireAfterWrite(electionTimeoutInMillis, TimeUnit.MILLISECONDS)
        .build();

    eventBroker.register(this);
  }

  @Subscribe
  public void handleHeartbeatRequest(HeartbeatEvent event) {
    LOGGER.info("Follower node received a heartbeat from the leader {}", event.getSender());
    eventBroker
        .send(HeartbeatEvent.class)
        .toSenderOf(event)
        .subscribe(leader -> heartbeatCache.put(leader, timeService.now()));
  }

  private void handleHeartbeatTimeout(RemovalNotification<ClusterNode, LocalDateTime> notification) {
    LOGGER.info("Follower didn't receive a heartbeat from the leader [{}] after the election timeout. " +
        "Last point of contact: {} (local time)", notification.getKey(), notification.getValue());
  }
}

package org.beholder.raft.consensus;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.beholder.events.EventBroker;
import org.beholder.events.RemoteEvent;
import org.beholder.raft.messages.HeartbeatRequest;
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
  }

  private void handleHeartbeatRequest(HeartbeatRequest heartbeatRequest) {
    eventBroker.send(new RemoteEvent() {
      @Override
      public ClusterNode getSender() {
        return null;
      }

      @Override
      public ClusterNode getReceiver() {
        return null;
      }
    }).toSenderOf(null).subscribe(leader -> heartbeatCache.put(leader, timeService.now()));
  }

  private void handleHeartbeatTimeout(RemovalNotification<ClusterNode, LocalDateTime> notification) {
    LOGGER.info("Follower didn't receive a heartbeat from the leader [{}] after the election timeout. " +
        "Last point of contact: {} (local time)", notification.getKey(), notification.getValue());
  }
}

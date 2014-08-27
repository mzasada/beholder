package org.beholder.raft.consensus;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.beholder.io.TopologyAwareMessageBroker;
import org.beholder.raft.messages.HeartbeatRequest;
import org.beholder.raft.messages.HeartbeatResponse;
import org.beholder.time.TimeService;
import org.beholder.topology.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class FollowerHeartbeatController {
  private static final Logger LOGGER = LoggerFactory.getLogger(FollowerHeartbeatController.class);

  private final Cache<ClusterNode, LocalDateTime> heartbeatCache;
  private final TopologyAwareMessageBroker topologyAwareMessageBroker;
  private final TimeService timeService;

  public FollowerHeartbeatController(
      TopologyAwareMessageBroker topologyAwareMessageBroker,
      TimeService timeService,
      long electionTimeoutInMillis) {
    this.topologyAwareMessageBroker = topologyAwareMessageBroker;
    this.timeService = timeService;
    this.heartbeatCache = CacheBuilder.newBuilder()
        .removalListener(this::handleHeartbeatTimeout)
        .expireAfterWrite(electionTimeoutInMillis, TimeUnit.MILLISECONDS)
        .build();
  }

  private void handleHeartbeatRequest(HeartbeatRequest heartbeatRequest) {

    topologyAwareMessageBroker.sendToLeader(new HeartbeatResponse());
    heartbeatCache.put(null, timeService.now());
  }

  private void handleHeartbeatTimeout(RemovalNotification<ClusterNode, LocalDateTime> notification) {
    LOGGER.info("Follower didn't receive a heartbeat from the leader [{}] after the election timeout. " +
        "Last point of contact: {} (local time)", notification.getKey(), notification.getValue());
  }
}

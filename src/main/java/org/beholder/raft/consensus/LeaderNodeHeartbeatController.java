package org.beholder.raft.consensus;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import org.beholder.io.TopologyAwareMessageBroker;
import org.beholder.raft.messages.HeartbeatRequest;
import org.beholder.time.TimeService;
import org.beholder.topology.ClusterNode;
import org.beholder.topology.ClusterTopology;
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

  private final TopologyAwareMessageBroker topologyAwareMessageBroker;
  private final TimeService timeService;

  public LeaderNodeHeartbeatController(TopologyAwareMessageBroker topologyAwareMessageBroker,
                                       ClusterTopology clusterTopology,
                                       TimeService timeService) {
    this.topologyAwareMessageBroker = topologyAwareMessageBroker;
    this.timeService = timeService;

    clusterTopology.getSecondaryNodes().forEach(this::beginHeartbeatWith);
  }

  private void beginHeartbeatWith(ClusterNode clusterNode) {
    topologyAwareMessageBroker.sendTo(clusterNode, new HeartbeatRequest());
    heartbeatCache.put(clusterNode, timeService.now());
  }

  private void handleHeartbeatTimeout(RemovalNotification<ClusterNode, LocalDateTime> notification) {
    LOGGER.info("Leader node didn't receive a heartbeat from {}", notification.getKey());
  }

}

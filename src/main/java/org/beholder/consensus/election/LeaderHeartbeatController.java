package org.beholder.consensus.election;

import com.google.common.eventbus.Subscribe;
import org.beholder.events.EventBroker;
import org.beholder.events.remote.HeartbeatEvent;
import org.beholder.time.AlarmClock;
import org.beholder.topology.ClusterNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedHashMap;
import java.util.Map;

public class LeaderHeartbeatController {
  private static final Logger LOGGER = LoggerFactory.getLogger(LeaderHeartbeatController.class);
  private static final long DEFAULT_HEARTBEAT_TIMEOUT_MILLIS = 200; //TODO: random?

  private final Map<ClusterNode, AlarmClock> heartbeatCache = new LinkedHashMap<>();

  private final EventBroker eventBroker;

  public LeaderHeartbeatController(EventBroker eventBroker) {
    this.eventBroker = eventBroker;
  }

  private void beginHeartbeatWithAllFollowers() {
    eventBroker
        .sendRemoteEvent(HeartbeatEvent.class)
        .toAllFollowers()
        .subscribe(follower ->
            heartbeatCache.put(follower, new AlarmClock(() ->
                handleHeartbeatTimeout(follower),
                DEFAULT_HEARTBEAT_TIMEOUT_MILLIS).start()));
  }

  @Subscribe
  public void handleHeartbeatEvent(HeartbeatEvent event) {
    LOGGER.info("Leader node received a heartbeat from {}", event.getSender());
    eventBroker
        .sendRemoteEvent(HeartbeatEvent.class)
        .toSenderOf(event)
        .subscribe(follower -> heartbeatCache.get(follower).snooze());
  }

  private void handleHeartbeatTimeout(ClusterNode clusterNode) {
    LOGGER.info("Leader node didn't receive a heartbeat from {}", clusterNode);
  }

  @PostConstruct
  public void start() {
    eventBroker.register(this);
    beginHeartbeatWithAllFollowers();
  }

  @PreDestroy
  public void stop() {
    eventBroker.unregister(this);
    heartbeatCache.forEach((node, ticker) -> ticker.stop());
    heartbeatCache.clear();
  }

}

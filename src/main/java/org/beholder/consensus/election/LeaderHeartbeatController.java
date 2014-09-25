package org.beholder.consensus.election;

import com.google.common.eventbus.Subscribe;
import org.beholder.events.EventBroker;
import org.beholder.events.remote.HeartbeatEvent;
import org.beholder.time.AlarmClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class LeaderHeartbeatController {
  private static final Logger LOGGER = LoggerFactory.getLogger(LeaderHeartbeatController.class);

  private final EventBroker eventBroker;
  private final AlarmClock heartbeatBroadcastClock;

  public LeaderHeartbeatController(long broadcastTimeInMillis, EventBroker eventBroker) {
    this.eventBroker = eventBroker;
    this.heartbeatBroadcastClock = new AlarmClock(this::beginHeartbeatWithAllFollowers, broadcastTimeInMillis);
  }

  private void beginHeartbeatWithAllFollowers() {
    eventBroker
        .sendRemoteEvent(HeartbeatEvent.class)
        .toAllFollowers()
        .subscribe(follower -> LOGGER.info("Heartbeat sent to {}", follower));
  }

  @Subscribe
  public void handleHeartbeatEvent(HeartbeatEvent event) {
    LOGGER.info("Leader node received a heartbeat from {}", event.getSender());
  }

  @PostConstruct
  public void start() {
    eventBroker.register(this);
    heartbeatBroadcastClock.start();
  }

  @PreDestroy
  public void stop() {
    eventBroker.unregister(this);
    heartbeatBroadcastClock.stop();
  }

}

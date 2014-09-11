package org.beholder.consensus.election;

import com.google.common.eventbus.Subscribe;
import org.beholder.events.EventBroker;
import org.beholder.events.local.StartElectionEvent;
import org.beholder.events.remote.HeartbeatEvent;
import org.beholder.time.AlarmClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class FollowerHeartbeatController {
  private static final Logger LOGGER = LoggerFactory.getLogger(FollowerHeartbeatController.class);

  private final AlarmClock heartbeatTrace;
  private final EventBroker eventBroker;

  public FollowerHeartbeatController(
      long electionTimeoutInMillis, EventBroker eventBroker) {
    this.eventBroker = eventBroker;
    this.heartbeatTrace = new AlarmClock(this::handleHeartbeatTimeout, electionTimeoutInMillis);
  }

  @Subscribe
  public void handleHeartbeatEvent(HeartbeatEvent event) {
    LOGGER.info("Follower node received a heartbeat from the leader {}", event.getSender());
    eventBroker
        .sendRemoteEvent(HeartbeatEvent.class)
        .toSenderOf(event)
        .subscribe(leader -> heartbeatTrace.snooze());
  }

  private void handleHeartbeatTimeout() {
    LOGGER.info("Follower didn't receive a heartbeat from the leader after the election timeout.");
    eventBroker.sendLocalEvent(StartElectionEvent.class);
  }

  @PostConstruct
  public void start() {
    eventBroker.register(this);
    heartbeatTrace.start();
  }

  @PreDestroy
  public void stop() {
    eventBroker.unregister(this);
    heartbeatTrace.stop();
  }
}

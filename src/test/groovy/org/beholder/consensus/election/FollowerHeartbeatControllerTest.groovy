package org.beholder.consensus.election

import org.beholder.events.EventBroker
import org.beholder.events.local.StartElectionEvent
import spock.lang.Specification

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.timeout
import static org.mockito.Mockito.verify

class FollowerHeartbeatControllerTest extends Specification {

  FollowerHeartbeatController controller

  def cleanup() {
    controller?.stop()
  }

  def "should start the election when no heartbeat was received on startup"() {
    given:
    EventBroker eventBroker = mock(EventBroker)
    controller = new FollowerHeartbeatController(50, eventBroker)

    when:
    controller.start()

    then:
    verify(eventBroker, timeout(200)).sendLocalEvent(StartElectionEvent)
  }

  def "should not start the election before first timeout"() {
    given:
    EventBroker eventBroker = mock(EventBroker)
    controller = new FollowerHeartbeatController(100, eventBroker)

    when:
    controller.start()

    then:
    verify(eventBroker, timeout(80).never()).sendLocalEvent(StartElectionEvent)
  }
}

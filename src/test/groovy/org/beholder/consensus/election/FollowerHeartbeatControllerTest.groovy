package org.beholder.consensus.election

import com.google.common.eventbus.EventBus
import org.beholder.events.EventBroker
import org.beholder.events.EventBrokerImpl
import org.beholder.events.EventRegistry
import org.beholder.events.local.StartElectionEvent
import org.beholder.events.remote.HeartbeatEvent
import org.beholder.io.MessageGateway
import org.beholder.topology.ClusterNode
import org.beholder.topology.ClusterTopology
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class FollowerHeartbeatControllerTest extends Specification {

  def "should start the election when no heartbeat was received on startup"() {
    given:
    EventBroker eventBroker = Mock(EventBroker)
    def condition = new PollingConditions(timeout: 50, initialDelay: 1.5, factor: 1.25)
    FollowerHeartbeatController controller = new FollowerHeartbeatController(25, eventBroker)

    when:
    controller.start()

    then:
    condition.eventually {
      assert { 1 * eventBroker.sendLocalEvent(StartElectionEvent) }
    }
  }

  def "should not start the election when heartbeat was received before timeout"() {
    given:
    def condition = new PollingConditions(timeout: 100, initialDelay: 1.5, factor: 1.25)
    EventBus eventBus = new EventBus()
    MessageGateway messageGateway = Mock(MessageGateway)
    messageGateway.remoteEventStream() >> { rx.Observable.empty() }

    EventBroker eventBroker = new EventBrokerImpl(
        eventBus,
        new EventRegistry(Mock(ClusterTopology)),
        messageGateway,
        Mock(ClusterTopology))

    FollowerHeartbeatController controller = new FollowerHeartbeatController(75, eventBroker)

    when:
    controller.start()
    sleep(50)
    eventBus.post(new HeartbeatEvent(Stub(ClusterNode)))

    then:
    condition.eventually {
      assert { 0 * eventBroker.sendLocalEvent(StartElectionEvent) }
    }
  }

}

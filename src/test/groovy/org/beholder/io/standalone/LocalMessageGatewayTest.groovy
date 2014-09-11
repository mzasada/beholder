package org.beholder.io.standalone

import org.beholder.events.RemoteEvent
import org.beholder.events.remote.EmptyRemoteEvent
import org.beholder.topology.ClusterNode
import rx.functions.Action1
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LocalMessageGatewayTest extends Specification {

  ExecutorService executorService = Executors.newSingleThreadExecutor();

  def "should deliver all messages to the second node"() {
    given:
    def condition = new PollingConditions(timeout: 0.5, initialDelay: 0.01, factor: 0.02)

    def nodeOne = new TestClusterNode()
    def nodeTwo = new TestClusterNode()

    def router = [(nodeOne): new EventSink(executorService), (nodeTwo): new EventSink(executorService)]
    LocalMessageGateway nodeOneGateway = new LocalMessageGateway(router, nodeOne)
    LocalMessageGateway nodeTwoGateway = new LocalMessageGateway(router, nodeTwo)

    def events = (1..10).collect { new EmptyRemoteEvent(it) }
    def receivedEvents = []

    when:
    nodeOneGateway.remoteEventStream().subscribe({ RemoteEvent event -> receivedEvents << event } as Action1)
    events.each { nodeTwoGateway.sendTo(nodeOne, it) }

    then:
    condition.eventually {
      assert receivedEvents.equals(events)
    }
  }

  def "should deliver 1 message to 2nd node, 0 messages to 3rd node"() {
    given:
    def condition = new PollingConditions(timeout: 0.5, initialDelay: 0.01, factor: 0.02)

    def nodeOne = new TestClusterNode()
    def nodeTwo = new TestClusterNode()
    def nodeThree = new TestClusterNode()

    def router = [
        (nodeOne)  : new EventSink(executorService),
        (nodeTwo)  : new EventSink(executorService),
        (nodeThree): new EventSink(executorService)
    ]
    LocalMessageGateway nodeOneGateway = new LocalMessageGateway(router, nodeOne)
    LocalMessageGateway nodeTwoGateway = new LocalMessageGateway(router, nodeTwo)
    LocalMessageGateway nodeThreeGateway = new LocalMessageGateway(router, nodeThree)

    def receivedEvents = [(nodeTwo): [], (nodeThree): []]
    def event = new EmptyRemoteEvent(1)

    when:
    nodeTwoGateway.remoteEventStream().subscribe({ RemoteEvent e -> receivedEvents[nodeTwo] << e } as Action1)
    nodeThreeGateway.remoteEventStream().subscribe({ RemoteEvent e -> receivedEvents[nodeThree] << e } as Action1)

    nodeOneGateway.sendTo(nodeTwo, event)

    then:
    condition.eventually {
      assert receivedEvents[nodeTwo].equals([event])
      assert receivedEvents[nodeThree] == []
    }
  }

  private static class TestClusterNode implements ClusterNode {
  }
}

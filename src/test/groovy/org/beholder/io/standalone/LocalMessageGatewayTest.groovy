package org.beholder.io.standalone

import com.google.common.util.concurrent.MoreExecutors
import org.beholder.events.RemoteEvent
import org.beholder.events.remote.EmptyRemoteEvent
import org.beholder.topology.ClusterNode
import rx.functions.Action1
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class LocalMessageGatewayTest extends Specification {

  ExecutorService executorService = MoreExecutors.getExitingExecutorService(
      new ThreadPoolExecutor(5, 10, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
      250, TimeUnit.MILLISECONDS)

  def "should deliver all messages to the second node"() {
    given:
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
    receivedEvents.equals(events)
  }

  def "should deliver 1 message to 2nd node, 0 messages to 3rd node"() {
    given:
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
    receivedEvents[nodeTwo].equals([event])
    receivedEvents[nodeThree] == []
  }

  private static class TestClusterNode implements ClusterNode {
  }
}

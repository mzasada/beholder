package org.beholder.topology.singleprocess

import org.beholder.events.remote.EmptyRemoteEvent
import org.beholder.topology.standalone.SingleProcessClusterNode
import org.beholder.topology.standalone.LocalClusterFactory
import rx.observers.TestObserver
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class LocalClusterFactoryTest extends Specification {

  LocalClusterFactory factory = new LocalClusterFactory()

  def "should create cluster with given size"() {
    when:
    def cluster = factory.newFixedLocalCluster(9)

    then:
    cluster.size() == 9
  }

  def "should create cluster from the node set"() {
    given:
    def nodes = [new SingleProcessClusterNode(0), new SingleProcessClusterNode(1)] as Set

    when:
    def cluster = factory.newFixedLocalCluster(nodes)

    then:
    cluster.keySet() == nodes
  }

  def "should deliver a message from node A to node B"() {
    given:
    def condition = new PollingConditions(timeout: 0.5, initialDelay: 0.01, factor: 0.02)
    def cluster = factory.newFixedLocalCluster(2)
    def nodeA = cluster.keySet().toList()[0]
    def nodeB = cluster.keySet().toList()[1]
    def observer = new TestObserver()
    def event = new EmptyRemoteEvent(0)

    when:
    cluster[nodeB].remoteEventStream().subscribe(observer)
    cluster[nodeA].sendTo(nodeB, event)

    then:
    condition.eventually {
      assert observer.onNextEvents == [event]
    }
  }
}

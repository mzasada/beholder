package org.beholder.consensus.election

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import org.beholder.events.EventBroker
import org.beholder.events.EventBrokerImpl
import org.beholder.events.EventRegistry
import org.beholder.events.LocalEvent
import org.beholder.events.local.StartElectionEvent
import org.beholder.topology.ClusterNode
import org.beholder.topology.ClusterState
import org.beholder.topology.MessageGateway
import org.beholder.topology.standalone.LocalClusterFactory
import spock.lang.Specification

class HeartbeatTest extends Specification {

  def clusterFactory = new LocalClusterFactory()

  def "should keep heartbeat between 3 nodes without starting the election"() {
    given:
    def cluster = clusterFactory.newFixedLocalCluster(3)
    def nodes = cluster.keySet().asList()
    def leader = nodes[0]
    def followerA = nodes[1]
    def followerB = nodes[2]
    LocalEventSubscriber<StartElectionEvent> subscriber = new LocalEventSubscriber<>()

    FollowerHeartbeatController followerAController = new FollowerHeartbeatController(110, eventBroker(
        subscriber,
        followerA,
        [followerA, followerB],
        cluster[followerA]))

    FollowerHeartbeatController followerBController = new FollowerHeartbeatController(120, eventBroker(
        subscriber,
        followerB,
        [followerA, followerB],
        cluster[followerB]))

    LeaderHeartbeatController leaderController = new LeaderHeartbeatController(50,
        eventBroker(subscriber, leader, [followerA, followerB], cluster[leader]))

    when:
    followerAController.start()
    followerBController.start()
    leaderController.start()

    sleep(350)

    then:
    subscriber.events == []

    cleanup:
    followerAController.stop()
    followerBController.stop()
    leaderController.stop()
  }

  private EventBroker eventBroker(LocalEventSubscriber<StartElectionEvent> subscriber,
                                  ClusterNode node, Iterable<ClusterNode> followers, MessageGateway gateway) {
    ClusterState clusterTopology = new ClusterState(node)
    followers.each { clusterTopology.addFollower(it) }
    def eventBus = new EventBus("$node")
    eventBus.register(subscriber)
    new EventBrokerImpl(eventBus, new EventRegistry(clusterTopology), gateway, clusterTopology)
  }

  private static class LocalEventSubscriber<T extends LocalEvent> {

    def events = []

    @Subscribe
    public void recieve(T event) {
      events << event
    }

  }
}

package org.beholder.topology.singleprocess

import org.beholder.topology.standalone.SingleProcessClusterNode
import spock.lang.Specification

class SingleProcessClusterNodeTest extends Specification {

  def "should consider two nodes with the same ID equal"() {
    given:
    def nodeA = new SingleProcessClusterNode(0)
    def nodeB = new SingleProcessClusterNode(0)

    expect:
    nodeA.hashCode() == nodeB.hashCode()
    nodeA.equals(nodeB)
  }

  def "should consider two nodes with different IDs unequal"() {
    given:
    def nodeA = new SingleProcessClusterNode(0)
    def nodeB = new SingleProcessClusterNode(1)

    expect:
    nodeA.hashCode() != nodeB.hashCode()
    !nodeA.equals(nodeB)
  }

}

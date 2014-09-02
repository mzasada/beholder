package org.beholder.topology;

import java.util.List;

public interface ClusterTopology {

  boolean isLeader();

  List<ClusterNode> getSecondaryNodes();

  ClusterNode getLeaderNode();

  ClusterNode self();
}

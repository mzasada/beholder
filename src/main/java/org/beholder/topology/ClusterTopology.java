package org.beholder.topology;

import java.util.Set;

public interface ClusterTopology {

  Set<ClusterNode> getFollowers();

  ClusterNode self();
}

package org.beholder.topology;

import java.util.List;

public interface ClusterTopology {

  List<ClusterNode> getSecondaryNodes();

  ClusterNode self();
}

package org.beholder.topology.standalone;

import org.beholder.topology.MessageGateway;
import org.beholder.topology.ClusterNode;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ContiguousSet.create;
import static com.google.common.collect.DiscreteDomain.integers;
import static com.google.common.collect.Range.closedOpen;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class LocalClusterFactory {

  public Map<ClusterNode, MessageGateway> newFixedLocalCluster(int nodesCount) {
    Set<ClusterNode> clusterNodes = create(closedOpen(0, nodesCount), integers())
        .stream()
        .map(SingleProcessClusterNode::new)
        .collect(toSet());
    return newFixedLocalCluster(clusterNodes);
  }

  public Map<ClusterNode, MessageGateway> newFixedLocalCluster(Set<ClusterNode> nodes) {
    Map<ClusterNode, EventSink> router = Collections.unmodifiableMap(nodes
        .stream()
        .collect(toMap(identity(), (node) -> new EventSink())));
    return Collections.unmodifiableMap(nodes
        .stream()
        .collect(toMap(identity(), (node) -> new LocalMessageGateway(router, node))));
  }
}

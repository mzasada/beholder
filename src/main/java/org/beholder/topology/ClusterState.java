package org.beholder.topology;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClusterState implements ClusterTopology {

  private final Set<ClusterNode> followers = new HashSet<>();

  private final ClusterNode self;

  public ClusterState(ClusterNode self) {
    this.self = self;
  }

  public void addFollower(ClusterNode clusterNode) {
    followers.add(clusterNode);
  }

  @Override
  public Set<ClusterNode> getFollowers() {
    return Collections.unmodifiableSet(followers);
  }

  @Override
  public ClusterNode self() {
    return self;
  }
}

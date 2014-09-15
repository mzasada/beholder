package org.beholder.topology.singleprocess;

import org.beholder.topology.ClusterNode;

public class SingleProcessClusterNode implements ClusterNode {
  private final int id;

  public SingleProcessClusterNode(int id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SingleProcessClusterNode that = (SingleProcessClusterNode) o;
    return id == that.id;

  }

  @Override
  public int hashCode() {
    return Integer.hashCode(id);
  }
}

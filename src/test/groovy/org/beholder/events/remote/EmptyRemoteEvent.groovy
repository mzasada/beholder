package org.beholder.events.remote

import org.beholder.events.RemoteEvent
import org.beholder.topology.ClusterNode

class EmptyRemoteEvent implements RemoteEvent {
  private final int id

  EmptyRemoteEvent(int id) {
    this.id = id
  }

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    EmptyRemoteEvent that = (EmptyRemoteEvent) o

    if (id != that.id) return false

    return true
  }

  int hashCode() {
    id
  }

  @Override
  public String toString() {
    "EmptyRemoteEvent($id)";
  }

  @Override
  ClusterNode getSender() {
    return null
  }
}

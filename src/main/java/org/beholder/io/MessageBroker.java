package org.beholder.io;

import org.beholder.topology.ClusterNode;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface MessageBroker {

  <M extends Message> void sendTo(ClusterNode node, M message);

  <M extends Message> void subscribe(Predicate<M> selector, Function<M, M> handler);

  <M extends Message> void subscribe(Predicate<M> selector, Consumer<M> handler);
}

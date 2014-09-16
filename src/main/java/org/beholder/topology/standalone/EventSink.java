package org.beholder.topology.standalone;

import org.beholder.events.RemoteEvent;
import org.beholder.topology.ClusterNode;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class EventSink implements Observable.OnSubscribe<RemoteEvent> {

  private final BlockingQueue<RemoteEvent> eventQueue = new ArrayBlockingQueue<>(256);

  public void emit(RemoteEvent event) {
    eventQueue.add(event);
  }

  public void complete() {
    eventQueue.add(new CompleteStreamEvent());
  }

  @Override
  public void call(Subscriber<? super RemoteEvent> subscriber) {
    Scheduler.Worker worker = Schedulers.newThread().createWorker();
    subscriber.add(worker);

    worker.schedule(() -> {
      while (true) {
        try {
          RemoteEvent event = eventQueue.take();
          if (event instanceof CompleteStreamEvent) {
            subscriber.onCompleted();
            return;
          } else if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(event);
          }
        } catch (InterruptedException e) {
          subscriber.onError(e);
        }
      }

    });
  }

  private static final class CompleteStreamEvent implements RemoteEvent {
    @Override
    public ClusterNode getSender() {
      return null;
    }
  }

}

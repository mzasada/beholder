package org.beholder.io.standalone

import com.google.common.util.concurrent.MoreExecutors
import org.beholder.events.RemoteEvent
import org.beholder.topology.ClusterNode
import rx.functions.Action1
import rx.observers.TestObserver
import spock.lang.Specification

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class EventSinkTest extends Specification {

  ExecutorService executorService = MoreExecutors.getExitingExecutorService(
      new ThreadPoolExecutor(5, 10, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()))

  EventSink eventSink = new EventSink(executorService);

  def "should emit all the events"() {
    given:
    def events = (1..10).collect { new EmptyRemoteEvent(id: it) }
    def receivedEvents = []
    rx.Observable.create(eventSink).subscribe(
        { RemoteEvent event -> receivedEvents << event } as Action1<? super RemoteEvent>)

    when:
    events.each { eventSink.emit(it) }

    then:
    receivedEvents == events
  }

  def "should complete the stream after closing sink"() {
    given:
    TestObserver<RemoteEvent> observer = new TestObserver<>()
    rx.Observable.create(eventSink).subscribe(observer)

    when:
    eventSink.complete()

    then:
    observer.assertTerminalEvent()
    observer.getOnCompletedEvents().every { it.onCompleted }
  }

  private static class EmptyRemoteEvent implements RemoteEvent {

    int id

    @Override
    ClusterNode getSender() {
      return null
    }
  }
}

package org.beholder.topology.singleprocess

import org.beholder.events.RemoteEvent
import org.beholder.events.remote.EmptyRemoteEvent
import org.beholder.topology.standalone.EventSink
import rx.observers.TestObserver
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class EventSinkTest extends Specification {

  EventSink eventSink = new EventSink();

  def "should emit all the events"() {
    given:
    def condition = new PollingConditions(timeout: 0.5, initialDelay: 0.01, factor: 0.02)
    def events = (1..10).collect { new EmptyRemoteEvent(it) }
    TestObserver<RemoteEvent> observer = new TestObserver<>()
    rx.Observable.create(eventSink).subscribe(observer)

    when:
    events.each { eventSink.emit(it) }

    then:
    condition.eventually {
      assert observer.onNextEvents == events
    }
  }

  def "should complete the stream after closing sink"() {
    given:
    def condition = new PollingConditions(timeout: 0.5, initialDelay: 0.01, factor: 0.02)
    TestObserver<RemoteEvent> observer = new TestObserver<>()
    rx.Observable.create(eventSink).subscribe(observer)

    when:
    eventSink.complete()

    then:
    condition.eventually {
      assert observer.getOnCompletedEvents().every { it.onCompleted }
    }
  }
}

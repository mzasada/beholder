package org.beholder.time

import spock.lang.Specification

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.timeout
import static org.mockito.Mockito.verify

class AlarmClockTest extends Specification {

  AlarmClock alarmClock

  def cleanup() {
    alarmClock?.stop()
  }

  def "should call the callback after timeout"() {
    given:
    def callback = mock(Runnable)
    alarmClock = new AlarmClock(callback, 50)

    when:
    alarmClock.start()

    then:
    verify(callback, timeout(80)).run()
  }

  def "should call the callback 2 times after 2 timeouts"() {
    given:
    def callback = mock(Runnable)
    alarmClock = new AlarmClock(callback, 50)

    when:
    alarmClock.start()

    then:
    verify(callback, timeout(130).times(2)).run()
  }

  def "should call the callback 1 time after 1 snooze "() {
    given:
    def callback = mock(Runnable)
    alarmClock = new AlarmClock(callback, 50)

    when:
    alarmClock.start()
    sleep(40)
    alarmClock.snooze()

    then:
    verify(callback, timeout(130)).run()
  }

  def "should not call the callback if snoozed before timeout"() {
    given:
    def callback = mock(Runnable)
    alarmClock = new AlarmClock(callback, 100)

    when:
    alarmClock.start()
    sleep(70)
    alarmClock.snooze()

    then:
    verify(callback, timeout(120).never()).run()
  }

  def "should not call the callback if stopped before timeout"() {
    given:
    def callback = mock(Runnable)
    alarmClock = new AlarmClock(callback, 70)

    when:
    alarmClock.start()
    sleep(50)
    alarmClock.stop()

    then:
    verify(callback, timeout(100).never()).run()
  }
}

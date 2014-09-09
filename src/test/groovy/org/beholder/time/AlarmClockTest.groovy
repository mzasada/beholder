package org.beholder.time

import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class AlarmClockTest extends Specification {

  def callback = Mock(Runnable)
  AlarmClock alarmClock

  def setup() {
    alarmClock = new AlarmClock(callback, 200)
  }

  def cleanup() {
    alarmClock.stop()
  }

  def "should call the callback after timeout"() {
    given:
    def conditions = new PollingConditions(timeout: 0.25, initialDelay: 1.5, factor: 1.25)

    when:
    alarmClock.start()

    then:
    conditions.eventually {
      assert { 1 * callback.run() }
    }
  }

  def "should not call the callback if snoozed once before timeout"() {
    given:
    def conditions = new PollingConditions(timeout: 0.25, initialDelay: 1.5, factor: 1.25)

    when:
    alarmClock.start()
    sleep(100)
    alarmClock.snooze()

    then:
    conditions.eventually {
      assert { 0 * callback.run() }
    }
  }

  def "should call the callback 2 times after 2 timeouts"() {
    given:
    def conditions = new PollingConditions(timeout: 0.45, initialDelay: 1.5, factor: 1.25)

    when:
    alarmClock.start()

    then:
    conditions.eventually {
      assert { 2 * callback.run() }
    }
  }

  def "should not call the callback if snoozed and stopped before timeout"() {
    given:
    def conditions = new PollingConditions(timeout: 0.2, initialDelay: 1.5, factor: 1.25)

    when:
    alarmClock.start()
    sleep(100)
    alarmClock.snooze()

    then:
    conditions.eventually {
      assert { 0 * callback.run() }
    }
  }

}

package org.beholder.time;

import com.google.common.base.Throwables;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Allows delayed function invocation after a given timeout. If the clock is snoozed, the
 * countdown is reset.
 */
public class AlarmClock {
  private static final String SNOOZE_COMMAND = "snooze";
  private static final String STOP_COMMAND = "stop";

  private final Thread tickingThread;
  private final BlockingQueue<String> blockingQueue = new ArrayBlockingQueue<>(10);

  public AlarmClock(Runnable alarmCallback, long timeoutInMillis) {
    this.tickingThread = new Thread(() -> {
      while (true) {
        try {
          String cmd = blockingQueue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
          if (cmd == null) {
            alarmCallback.run();
          } else if (STOP_COMMAND.equalsIgnoreCase(cmd)) {
            return;
          }
        } catch (InterruptedException e) {
          Throwables.propagate(e);
        }
      }
    });
  }

  public AlarmClock start() {
    if (!tickingThread.isAlive()) {
      tickingThread.start();
    }
    return this;
  }

  public AlarmClock snooze() {
    blockingQueue.add(SNOOZE_COMMAND);
    return this;
  }

  public AlarmClock stop() {
    blockingQueue.add(STOP_COMMAND);
    return this;
  }
}

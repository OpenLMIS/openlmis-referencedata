/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

public class RightAssignmentExecutorTest {

  @Test
  public void shouldReturnProperThreadPoolTaskExecutor() {
    ThreadPoolTaskExecutor threadPoolTaskExecutor = createExecutor();

    assertEquals(1, threadPoolTaskExecutor.getCorePoolSize());
    assertEquals(1, threadPoolTaskExecutor.getMaxPoolSize());
    assertEquals(1, ReflectionTestUtils.getField(threadPoolTaskExecutor, "queueCapacity"));
    assertEquals(60, threadPoolTaskExecutor.getKeepAliveSeconds());
  }

  @Test
  public void shouldSkipTaskAndLogAtInfoWhenRegenerationIsAlreadyQueued() throws Exception {
    ILoggingEvent event = submitPastCapacity(createExecutor(1));

    assertEquals(Level.INFO, event.getLevel());
    assertEquals(RightAssignmentExecutor.REGENERATION_ALREADY_QUEUED,
        event.getFormattedMessage());
  }

  @Test
  public void shouldLogAtWarnWhenTaskIsRejectedWithNoRegenerationQueued() throws Exception {
    ILoggingEvent event = submitPastCapacity(createExecutor(0));

    assertEquals(Level.WARN, event.getLevel());
    assertEquals(RightAssignmentExecutor.REGENERATION_REJECTED,
        event.getFormattedMessage());
  }

  /**
   * Blocks the only thread, fills the queue, submits one more task, and returns the single log
   * event of its rejection after checking that the rejected task never ran.
   */
  private ILoggingEvent submitPastCapacity(ThreadPoolTaskExecutor threadPoolTaskExecutor)
      throws InterruptedException {
    threadPoolTaskExecutor.initialize();
    int queueCapacity = (int) ReflectionTestUtils.getField(threadPoolTaskExecutor, "queueCapacity");

    Logger logger = (Logger) LoggerFactory.getLogger(RightAssignmentService.class);
    Level previousLevel = logger.getLevel();
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(Level.INFO);

    CountDownLatch running = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    AtomicBoolean rejectedTaskRan = new AtomicBoolean(false);

    try {
      threadPoolTaskExecutor.execute(() -> {
        running.countDown();
        awaitQuietly(release);
      });
      assertTrue(running.await(5, TimeUnit.SECONDS));
      for (int i = 0; i < queueCapacity; i++) {
        threadPoolTaskExecutor.execute(() -> { });
      }
      threadPoolTaskExecutor.execute(() -> rejectedTaskRan.set(true));

      release.countDown();
      threadPoolTaskExecutor.getThreadPoolExecutor().shutdown();
      assertTrue(threadPoolTaskExecutor.getThreadPoolExecutor()
          .awaitTermination(5, TimeUnit.SECONDS));

      assertFalse(rejectedTaskRan.get());
      assertEquals(1, appender.list.size());
      return appender.list.get(0);
    } finally {
      release.countDown();
      logger.detachAppender(appender);
      logger.setLevel(previousLevel);
      threadPoolTaskExecutor.shutdown();
    }
  }

  private ThreadPoolTaskExecutor createExecutor() {
    return createExecutor(1);
  }

  private ThreadPoolTaskExecutor createExecutor(int queueCapacity) {
    RightAssignmentExecutor executor = new RightAssignmentExecutor();
    ReflectionTestUtils.setField(executor, "corePoolSize", 1);
    ReflectionTestUtils.setField(executor, "maxPoolSize", 1);
    ReflectionTestUtils.setField(executor, "queueCapacity", queueCapacity);
    ReflectionTestUtils.setField(executor, "threadTimeout", 60);
    return executor.rightAssignmentTaskExecutor();
  }

  private static void awaitQuietly(CountDownLatch latch) {
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
  }
}

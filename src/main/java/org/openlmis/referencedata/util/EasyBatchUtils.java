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

package org.openlmis.referencedata.util;

import static java.util.stream.Collectors.toList;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.EasyBatchMessageKeys;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class EasyBatchUtils {
  public static final int DEFAULT_BATCH_SIZE = 1000;
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(EasyBatchUtils.class);

  private final ExecutorService executorService;

  public EasyBatchUtils(ExecutorService executorService) {
    this.executorService = executorService;
  }

  /**
   * Execute {@code processBatch} on batches of {@code allItems}. Batches are created by splitting
   * the list in equal parts of {@link #DEFAULT_BATCH_SIZE} items.
   *
   * @param allItems all items to process
   * @param processBatch the batch processor
   * @param <T> the type of item to process
   * @param <R> the type of processed item
   * @return the list of processed items, never null
   * @throws InterruptedException this blocking operation was interrupted
   */
  public <T, R> List<R> processInBatches(List<T> allItems, Function<List<T>, List<R>> processBatch)
      throws InterruptedException {
    return processInBatches(allItems, processBatch, l -> Lists.partition(l, DEFAULT_BATCH_SIZE));
  }

  /**
   * Execute {@code processBatch} on batches of {@code allItems}. Batches are created by {@code
   * splitter}.
   *
   * @param allItems all items to process
   * @param processBatch the batch processor
   * @param splitter the batch creator
   * @param <T> the type of item to process
   * @param <R> the type of processed item
   * @return the list of processed items, never null
   * @throws InterruptedException this blocking operation was interrupted
   */
  public <T, R> List<R> processInBatches(
      List<T> allItems,
      Function<List<T>, List<R>> processBatch,
      Function<List<T>, List<List<T>>> splitter)
      throws InterruptedException {

    final List<Callable<List<R>>> toExecute =
        splitter.apply(allItems).stream()
            .map(batch -> (Callable<List<R>>) () -> processBatch.apply(batch))
            .collect(toList());

    final List<R> result = new ArrayList<>();
    for (Future<List<R>> invokedTask : executorService.invokeAll(toExecute)) {
      try {
        result.addAll(invokedTask.get());
      } catch (ExecutionException ee) {
        XLOGGER.error("Failed to run batch in EasyBatchUtils", ee);
        throw new ValidationMessageException(
            ee, EasyBatchMessageKeys.ERROR_FAILED_TO_PROCESS_BATCH, ee.getMessage());
      }
    }

    return result;
  }
}

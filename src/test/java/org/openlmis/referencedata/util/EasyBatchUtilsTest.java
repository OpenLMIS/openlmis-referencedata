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

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.messagekeys.EasyBatchMessageKeys;
import org.openlmis.referencedata.util.messagekeys.TradeItemMessageKeys;
import org.springframework.dao.DataIntegrityViolationException;

public class EasyBatchUtilsTest {

  private static final String ORDERABLE_NOT_FOUND = "Orderable with code: NO-SUCH-CODE not found!";

  @Rule
  public final ExpectedException expectedEx = ExpectedException.none();

  private ExecutorService executorService;
  private EasyBatchUtils easyBatchUtils;

  @Before
  public void setUp() {
    executorService = Executors.newSingleThreadExecutor();
    easyBatchUtils = new EasyBatchUtils(executorService);
  }

  @After
  public void tearDown() {
    executorService.shutdownNow();
  }

  @Test
  public void shouldProcessAllBatches() throws InterruptedException {
    List<String> result = easyBatchUtils.processInBatches(
        Arrays.asList("a", "b", "c"), batch -> batch, list -> Lists.partition(list, 1));

    assertEquals(Arrays.asList("a", "b", "c"), result);
  }

  @Test
  public void shouldRethrowValidationMessageExceptionFromBatch() throws InterruptedException {
    expectedEx.expect(ValidationMessageException.class);
    // startsWith, not a plain substring: the wrapped message also contains this key
    expectedEx.expectMessage(startsWith(TradeItemMessageKeys.ERROR_GTIN_INVALID_CHECK_DIGIT));

    easyBatchUtils.processInBatches(
        Collections.singletonList("a"),
        batch -> {
          throw new ValidationMessageException(
              new Message(TradeItemMessageKeys.ERROR_GTIN_INVALID_CHECK_DIGIT));
        });
  }

  @Test
  public void shouldKeepTheMessageOfOtherMessageExceptionsFromBatch() throws InterruptedException {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(startsWith(ORDERABLE_NOT_FOUND));

    easyBatchUtils.processInBatches(
        Collections.singletonList("a"),
        batch -> {
          throw new NotFoundException(new Message(ORDERABLE_NOT_FOUND));
        });
  }

  @Test
  public void shouldRethrowDataIntegrityViolationFromBatch() throws InterruptedException {
    DataIntegrityViolationException thrown = new DataIntegrityViolationException("constraint");

    expectedEx.expect(DataIntegrityViolationException.class);

    easyBatchUtils.processInBatches(
        Collections.singletonList("a"),
        batch -> {
          throw thrown;
        });
  }

  @Test
  public void shouldWrapExceptionsWithoutMessageKey() throws InterruptedException {
    expectedEx.expect(ValidationMessageException.class);
    expectedEx.expectMessage(startsWith(EasyBatchMessageKeys.ERROR_FAILED_TO_PROCESS_BATCH));

    easyBatchUtils.processInBatches(
        Collections.singletonList("a"),
        batch -> {
          throw new IllegalStateException("boom");
        });
  }
}

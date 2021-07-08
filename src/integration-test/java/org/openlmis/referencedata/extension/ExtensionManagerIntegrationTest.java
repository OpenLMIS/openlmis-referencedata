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

package org.openlmis.referencedata.extension;

import java.util.HashMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.extension.point.ExtensionPointId;
import org.openlmis.referencedata.extension.point.OrderableCreatePostProcessor;
import org.openlmis.referencedata.service.DefaultOrderableCreatePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.UnusedLocalVariable")
public class ExtensionManagerIntegrationTest {

  private static final String invalidPointId = "InvalidExtensionPoint";
  private static final String invalidExtensionId = "InvalidExtension";
  private static final String extensionId = "DefaultAdjustmentReasonValidator";

  private HashMap<String, String> extensions;

  @Autowired
  private ExtensionManager extensionManager;

  /**
   * Prepare the test environment - add extensions for test purposes.
   */
  @Before
  public void setUp() {
    extensions = new HashMap<>();
    extensions.put(ExtensionPointId.ORDERABLE_CREATE_POST_POINT_ID, extensionId);
    extensions.put(invalidPointId, invalidExtensionId);
    extensionManager.setExtensions(extensions);
  }

  @Test
  public void testShouldReturnExtensionWithGivenIdAndClass() {
    Orderable orderable = (Orderable) extensionManager
        .getExtension(ExtensionPointId.ORDERABLE_CREATE_POST_POINT_ID,
                OrderableCreatePostProcessor.class);
    Assert.assertEquals(orderable.getClass(), DefaultOrderableCreatePostProcessor.class);
  }

  @Test
  public void testShouldReturnExtensionWithGivenClassIfMappingDoesNotExist() {
    Orderable orderable = (Orderable) extensionManager
        .getExtension("test", OrderableCreatePostProcessor.class);
    Assert.assertEquals(orderable.getClass(), DefaultOrderableCreatePostProcessor.class);
  }
}

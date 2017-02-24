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

package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.springframework.beans.factory.annotation.Autowired;

public class GeographicLevelRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<GeographicLevel> {

  @Autowired
  GeographicLevelRepository repository;

  GeographicLevelRepository getRepository() {
    return this.repository;
  }

  GeographicLevel generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    GeographicLevel level = new GeographicLevel();
    level.setCode(String.valueOf(instanceNumber));
    level.setLevelNumber(instanceNumber);
    return level;
  }

  @Test
  public void shouldByLevelNumber() {
    // given
    GeographicLevel level = repository.save(generateInstance());

    // when
    GeographicLevel result = repository.findByLevelNumber(level.getLevelNumber());

    // then
    assertNotNull(result);
    assertEquals(level.getId(), result.getId());
  }
}

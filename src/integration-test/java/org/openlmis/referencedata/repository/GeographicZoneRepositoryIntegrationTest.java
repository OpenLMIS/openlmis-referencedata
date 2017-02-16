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

import org.junit.Before;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;

public class GeographicZoneRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<GeographicZone> {

  @Autowired
  GeographicLevelRepository geographicLevelRepository;

  @Autowired
  GeographicZoneRepository repository;

  GeographicZoneRepository getRepository() {
    return this.repository;
  }

  private GeographicLevel level = new GeographicLevel();

  @Before
  public void setUp() {
    level.setCode("GeographicZoneRepositoryIntegrationTest");
    level.setLevelNumber(1);
    geographicLevelRepository.save(level);
  }

  GeographicZone generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    GeographicZone zone = new GeographicZone();
    zone.setCode(String.valueOf(instanceNumber));
    zone.setLevel(level);
    return zone;
  }
}

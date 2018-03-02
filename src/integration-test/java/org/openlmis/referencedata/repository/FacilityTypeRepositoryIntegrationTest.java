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

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class FacilityTypeRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<FacilityType> {

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Override
  FacilityTypeRepository getRepository() {
    return facilityTypeRepository;
  }

  @Override
  FacilityType generateInstance() {
    return new FacilityTypeDataBuilder().buildAsNew();
  }

  @Test
  public void shouldFindFacilityTypesByIds() {
    Pageable pageable = new PageRequest(0, 10);

    FacilityType type1 = facilityTypeRepository.save(generateInstance());
    FacilityType type2 = facilityTypeRepository.save(generateInstance());
    FacilityType type3 = facilityTypeRepository.save(generateInstance());

    facilityTypeRepository.save(generateInstance());
    facilityTypeRepository.save(generateInstance());
    facilityTypeRepository.save(generateInstance());

    Page<FacilityType> facilityTypePage = facilityTypeRepository
        .findByIdIn(asList(type1.getId(), type2.getId(), type3.getId()), pageable);

    assertEquals(3, facilityTypePage.getContent().size());
    assertThat(facilityTypePage.getContent(), hasItems(type1, type2, type3));
  }

  @Test
  public void shouldPaginateResultWhileSearchingByIds() {
    Pageable pageable = new PageRequest(1, 1);

    FacilityType type1 = facilityTypeRepository.save(generateInstance());
    FacilityType type2 = facilityTypeRepository.save(generateInstance());
    FacilityType type3 = facilityTypeRepository.save(generateInstance());

    facilityTypeRepository.save(generateInstance());
    facilityTypeRepository.save(generateInstance());
    facilityTypeRepository.save(generateInstance());

    Page<FacilityType> facilityTypePage = facilityTypeRepository
        .findByIdIn(asList(type1.getId(), type2.getId(), type3.getId()), pageable);

    assertEquals(1, facilityTypePage.getContent().size());
    assertEquals(3, facilityTypePage.getTotalElements());
  }
}

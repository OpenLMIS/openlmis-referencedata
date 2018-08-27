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

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class FacilityTypeApprovedProductServiceTest {

  private static final String CENTRAL_HOSPITAL = "CentralHospital";
  private static final String ESSENTIAL_MEDS = "Essential Meds";

  @Mock
  private FacilityTypeApprovedProductRepository repository;

  @InjectMocks
  private FacilityTypeApprovedProductService service = new FacilityTypeApprovedProductService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfLacksRequiredParams() {
    service.search(new HashMap<>(), null);
  }

  @Test
  public void shouldExtractParametersFromInputMapAndSearchForProducts() {
    Map<String, String> inputMap = new HashMap<>();
    inputMap.put("facilityType", CENTRAL_HOSPITAL);
    inputMap.put("program", ESSENTIAL_MEDS);

    service.search(inputMap, null);

    verify(repository)
        .searchProducts(eq(CENTRAL_HOSPITAL), eq(ESSENTIAL_MEDS), isNull(Pageable.class));
  }

  @Test
  public void shouldFindAPageOfProducts() {
    Map<String, String> inputMap = new HashMap<>();
    inputMap.put("facilityType", CENTRAL_HOSPITAL);
    inputMap.put("program", ESSENTIAL_MEDS);

    FacilityTypeApprovedProduct mock = mock(FacilityTypeApprovedProduct.class);
    when(repository
        .searchProducts(eq(CENTRAL_HOSPITAL), eq(ESSENTIAL_MEDS), isNull(Pageable.class)))
        .thenReturn(Pagination.getPage(
            Collections.singletonList(mock)));

    Page<FacilityTypeApprovedProduct> actual = service.search(inputMap, null);

    assertEquals(1, actual.getContent().size());
    assertThat(actual, hasItem(mock));
  }
}

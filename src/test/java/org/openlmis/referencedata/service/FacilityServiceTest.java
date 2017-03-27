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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FacilityServiceTest {

  @Mock
  private GeographicZoneRepository geographicZoneRepository;

  @Mock
  private GeographicZoneService geographicZoneService;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private GeographicZone parent;

  @Mock
  private GeographicZone child1;

  @Mock
  private GeographicZone child2;

  @Mock
  private Facility facility;

  @Mock
  private Facility facility2;

  private UUID facilityUuid = UUID.randomUUID();
  private UUID zoneUuid = UUID.randomUUID();
  private List<Facility> facilityList;

  @InjectMocks
  private FacilityService facilityService = new FacilityService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    facilityList = Lists.newArrayList(facility, facility2);
    when(facility.getId()).thenReturn(facilityUuid);
    when(parent.getId()).thenReturn(zoneUuid);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfZoneCodeAndNameNotProvidedForSearch() {
    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put("recurse", false);
    facilityService.searchFacilities(searchParams);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfGeographicZoneDoesntExist() {
    when(geographicZoneRepository.findOne(any(UUID.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put("zoneId", UUID.randomUUID());
    facilityService.searchFacilities(searchParams);
  }

  @Test
  public void shouldReturnAllElementsIfNoSearchCriteriaProvided() {
    when(facilityRepository.findAll()).thenReturn(facilityList);

    List<Facility> actual = facilityService.searchFacilities(new HashMap<>());
    verify(facilityRepository).findAll();
    assertEquals(facilityList, actual);
  }

  @Test
  public void shouldSearchForFacilitiesInChildZonesIfRecurseOptionProvided() {
    final String code = "FAC1";
    final String name = "Facility";

    when(geographicZoneRepository.findOne(zoneUuid)).thenReturn(parent);
    when(geographicZoneService.getAllZonesInHierarchy(parent)).thenReturn(Lists.newArrayList(
        child1, child2));

    when(facilityRepository.search(code, name, parent)).thenReturn(Lists.newArrayList(facility));
    when(facilityRepository.search(code, name, child2)).thenReturn(Lists.newArrayList(facility2));

    Map<String, Object> params = new HashMap<>();
    params.put("recurse", true);
    params.put("code", code);
    params.put("name", name);
    params.put("zoneId", zoneUuid);

    final List<Facility> actual = facilityService.searchFacilities(params);

    verify(facilityRepository).search(eq(code), eq(name), eq(parent));
    verify(facilityRepository).search(eq(code), eq(name), eq(child1));
    verify(facilityRepository).search(eq(code), eq(name), eq(child2));

    assertEquals(2, actual.size());
    assertThat(actual, hasItem(facility));
    assertThat(actual, hasItem(facility2));
  }

  @Test
  public void shouldSearchForFacilitiesInParentZoneOnlyIfRecurseOptionIsOff() {
    final String code = "FAC1";
    final String name = "Facility";

    when(geographicZoneRepository.findOne(zoneUuid)).thenReturn(parent);

    when(facilityRepository.search(code, name, parent)).thenReturn(Lists.newArrayList(facility));

    Map<String, Object> params = new HashMap<>();
    params.put("recurse", false);
    params.put("code", code);
    params.put("name", name);
    params.put("zoneId", zoneUuid);

    List<Facility> actual = facilityService.searchFacilities(params);

    verify(facilityRepository).search(eq(code), eq(name), eq(parent));
    verifyNoMoreInteractions(facilityRepository);

    assertEquals(1, actual.size());
    assertThat(actual, hasItem(facility));
  }

  @Test
  public void shouldSearchForFacilitiesWithExtraData() {
    final String code = "FAC1";
    final String name = "Facility";

    when(geographicZoneRepository.findOne(zoneUuid)).thenReturn(parent);
    when(geographicZoneService.getAllZonesInHierarchy(parent)).thenReturn(Lists.newArrayList(
        child1, child2));

    when(facilityRepository.search(code, name, parent)).thenReturn(Lists.newArrayList(facility));
    when(facilityRepository.search(code, name, child2)).thenReturn(Lists.newArrayList(facility2));

    when(facilityRepository.findByExtraData(anyString())).thenReturn(Lists.newArrayList(facility2));

    Map<String, String> extraData = new HashMap<>();
    extraData.put("type", "rural");

    Map<String, Object> params = new HashMap<>();
    params.put("recurse", true);
    params.put("code", code);
    params.put("name", name);
    params.put("zoneId", zoneUuid);
    params.put("extraData", extraData);

    List<Facility> actual = facilityService.searchFacilities(params);

    assertEquals(1, actual.size());
    assertThat(actual, hasItem(facility2));
  }
}

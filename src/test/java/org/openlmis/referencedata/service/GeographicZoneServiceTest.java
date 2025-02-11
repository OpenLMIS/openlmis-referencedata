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

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.service.GeographicZoneService.CODE;
import static org.openlmis.referencedata.service.GeographicZoneService.LEVEL_NUMBER;
import static org.openlmis.referencedata.service.GeographicZoneService.NAME;
import static org.openlmis.referencedata.service.GeographicZoneService.PARENT;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.hamcrest.core.Every;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.dto.GeographicZoneSimpleDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class GeographicZoneServiceTest {

  @Mock
  private GeographicZoneRepository geographicZoneRepository;

  @Mock
  private GeographicLevelRepository geographicLevelRepository;

  @Mock
  private GeographicZone parent;

  @Mock
  private GeographicZone child;

  @Mock
  private GeographicZone secondChild;

  @Mock
  private GeographicLevel level;

  @Mock
  private Pageable pageable;

  @InjectMocks
  private GeographicZoneService geographicZoneService;

  private UUID parentId = UUID.randomUUID();
  private UUID childId = UUID.randomUUID();
  private UUID childOfChildId = UUID.randomUUID();
  private List<GeographicZone> geographicZones;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    geographicZones = Lists.newArrayList(child, secondChild);
  }

  @Test
  public void shouldRetrieveOneDescendantWhenParentHasOneChild() {
    mockFindIdByParent(parentId, childId);
    assertGetAllZonesInHierarchy(parentId, childId);
  }

  @Test
  public void shouldRetrieveManyDescendantsWhenTheChildHasAChild() {
    mockFindIdByParent(parentId, childId);
    mockFindIdByParent(childId, childOfChildId);

    assertGetAllZonesInHierarchy(parentId, childId, childOfChildId);
  }

  @Test
  public void shouldRetrieveManyDescendantsWhenParentHasManyChildren() {
    mockFindIdByParent(parentId, childId, childOfChildId);
    assertGetAllZonesInHierarchy(parentId, childId, childOfChildId);
  }

  @Test
  public void shouldNotRetrieveAnyDescendantsWhenParentHasNoChildren() {
    mockFindIdByParent(parentId);
    assertGetAllZonesInHierarchy(parentId);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfThereIsNoValidParameterProvidedForSearch() {
    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put("some-param", "some-value");
    geographicZoneService.search(searchParams, pageable);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfGeographicZoneDoesNotExist() {
    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(PARENT, "zone-code");
    geographicZoneService.search(searchParams, pageable);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfProgramDoesNotExist() {
    when(geographicLevelRepository.findByLevelNumber(any(Integer.class))).thenReturn(null);

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(LEVEL_NUMBER, "1");
    geographicZoneService.search(searchParams, pageable);
  }

  @Test
  public void shouldReturnAllElementsIfNoSearchCriteriaProvided() {
    when(geographicZoneRepository.findAll(eq(pageable)))
        .thenReturn(Pagination.getPage(geographicZones, PageRequest.of(0, geographicZones.size()),
            geographicZones.size()));

    Page<GeographicZone> actual = geographicZoneService.search(new HashMap<>(), pageable);
    verify(geographicZoneRepository).findAll(eq(pageable));
    assertEquals(geographicZones, actual.getContent());
  }

  @Test
  public void shouldSearchForRequisitionGroupsWithAllParametersProvided() {
    when(geographicZoneRepository.findById(parentId)).thenReturn(Optional.of(parent));
    when(geographicLevelRepository.findByLevelNumber(1)).thenReturn(level);
    when(geographicZoneRepository.search(eq("name"), eq("code"),
        eq(parent), eq(level), any(Pageable.class)))
        .thenReturn(Pagination.getPage(geographicZones, PageRequest.of(0, 2), 2));

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(NAME, "name");
    searchParams.put(CODE, "code");
    searchParams.put(PARENT, parentId.toString());
    searchParams.put(LEVEL_NUMBER, "1");

    Page<GeographicZone> actual = geographicZoneService.search(searchParams, pageable);
    verify(geographicZoneRepository).search("name", "code", parent, level, pageable);
    assertEquals(geographicZones, actual.getContent());
  }

  private void mockFindIdByParent(UUID parentId, UUID... children) {
    when(geographicZoneRepository.findIdsByParent(parentId))
        .thenReturn(Sets.newHashSet(children));
  }

  private void assertGetAllZonesInHierarchy(UUID parentId, UUID... expected) {
    Set<UUID> actual = geographicZoneService.getAllZonesInHierarchy(parentId);
    assertThat(actual, hasSize(expected.length));
    assertThat(actual, hasItems(expected));
  }

  @Test
  public void shouldReturnGeographicZonesForHighestLevel() {
    setCatchmentPopulationAutoCalc(true);
    final int expectedListSize = geographicZones.size();

    when(geographicLevelRepository.findFirstByOrderByLevelNumberDesc())
        .thenReturn(Optional.of(level));
    when(geographicZoneRepository.findByLevel(level))
        .thenReturn(geographicZones);

    List<GeographicZoneSimpleDto> result = geographicZoneService.findAllExportableItems();

    assertEquals(expectedListSize, result.size());
    verify(geographicLevelRepository).findFirstByOrderByLevelNumberDesc();
    verify(geographicZoneRepository).findByLevel(eq(level));
  }

  @Test
  public void shouldReturnAllGeographicZones() {
    setCatchmentPopulationAutoCalc(false);
    final int expectedListSize = geographicZones.size();

    when(geographicZoneRepository.findAll())
        .thenReturn(geographicZones);

    List<GeographicZoneSimpleDto> result = geographicZoneService.findAllExportableItems();

    assertEquals(expectedListSize, result.size());
    verify(geographicZoneRepository).findAll();
  }

  @Test
  public void shouldReturnCorrectTypeForExportableItems() {
    when(geographicZoneRepository.findAll())
        .thenReturn(geographicZones);

    List<GeographicZoneSimpleDto> resultList = geographicZoneService.findAllExportableItems();
    Class<?> resultType = geographicZoneService.getExportableType();

    assertThat(resultList, Every.everyItem(instanceOf(resultType)));
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenGeographicLevelNotFound() {
    setCatchmentPopulationAutoCalc(true);
    when(geographicLevelRepository.findFirstByOrderByLevelNumberDesc())
        .thenReturn(Optional.empty());

    geographicZoneService.findAllExportableItems();
  }

  private void setCatchmentPopulationAutoCalc(boolean value) {
    ReflectionTestUtils.setField(geographicZoneService, "catchmentPopulationAutoCalc", value);
  }

}

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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.service.GeographicZoneService.CODE;
import static org.openlmis.referencedata.service.GeographicZoneService.LEVEL_NUMBER;
import static org.openlmis.referencedata.service.GeographicZoneService.NAME;
import static org.openlmis.referencedata.service.GeographicZoneService.PARENT;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
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
  private GeographicZone childOfChild;

  @Mock
  private GeographicLevel level;

  @Mock
  private Pageable pageable;

  @InjectMocks
  private GeographicZoneService geographicZoneService;

  private UUID zoneId = UUID.randomUUID();
  private List<GeographicZone> geographicZones;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    when(child.getName()).thenReturn("zone-1");
    when(secondChild.getName()).thenReturn("zone-2");
    geographicZones = Lists.newArrayList(child, secondChild);

    when(pageable.getPageSize()).thenReturn(10);
    when(pageable.getPageNumber()).thenReturn(0);
  }

  @Test
  public void shouldRetrieveOneDescendantWhenParentHasOneChild() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Collections.singletonList(child));

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Collections.singletonList(child), allZonesInHierarchy);
  }

  @Test
  public void shouldRetrieveManyDescendantsWhenTheChildHasAChild() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Collections.singletonList(child));
    when(geographicZoneRepository.findByParent(child))
        .thenReturn(Collections.singletonList(childOfChild));

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Arrays.asList(child, childOfChild), allZonesInHierarchy);
  }

  @Test
  public void shouldRetrieveManyDescendantsWhenParentHasManyChildren() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Arrays.asList(child, secondChild));

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Arrays.asList(child, secondChild), allZonesInHierarchy);
  }

  @Test
  public void shouldNotRetrieveAnyDescendantsWhenParentHasNoChildren() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Collections.emptyList());

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Collections.emptyList(), allZonesInHierarchy);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfThereIsNoValidParameterProvidedForSearch() {
    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put("some-param", "some-value");
    geographicZoneService.search(searchParams, pageable);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldThrowExceptionIfGeographicZoneDoesNotExist() {
    when(geographicZoneRepository.findOne(any(UUID.class))).thenReturn(null);

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
  public void shouldReturnAllElementsSortedByNameIfNoSearchCriteriaProvided() {
    when(geographicZoneRepository.findAllByOrderByNameAsc(any(Pageable.class)))
        .thenReturn(Pagination.getPage(geographicZones, null, geographicZones.size()));

    Page<GeographicZone> actual = geographicZoneService.search(new HashMap<>(), pageable);
    verify(geographicZoneRepository).findAllByOrderByNameAsc(any(Pageable.class));
    assertEquals(geographicZones, actual.getContent());
  }

  @Test
  public void shouldSearchForRequisitionGroupsWithAllParametersProvided() {
    when(geographicZoneRepository.findOne(any(UUID.class))).thenReturn(parent);
    when(geographicLevelRepository.findByLevelNumber(any(Integer.class))).thenReturn(level);
    when(geographicZoneRepository.search(any(String.class), any(String.class),
        any(GeographicZone.class), any(GeographicLevel.class), any(Pageable.class)))
        .thenReturn(Pagination.getPage(geographicZones, null, 2));

    Map<String, Object> searchParams = new HashMap<>();
    searchParams.put(NAME, "name");
    searchParams.put(CODE, "code");
    searchParams.put(PARENT, zoneId.toString());
    searchParams.put(LEVEL_NUMBER, "1");

    Page<GeographicZone> actual = geographicZoneService.search(searchParams, pageable);
    verify(geographicZoneRepository).search("name", "code", parent, level, pageable);
    assertEquals(geographicZones, actual.getContent());
  }
}
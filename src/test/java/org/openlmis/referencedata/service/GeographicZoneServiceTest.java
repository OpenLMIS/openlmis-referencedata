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
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicZoneRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class GeographicZoneServiceTest {

  @Mock
  private GeographicZoneRepository geographicZoneRepository;

  @Mock
  private GeographicZone parent;

  @Mock
  private GeographicZone child;

  @Mock
  private GeographicZone secondChild;

  @Mock
  private GeographicZone childOfChild;

  @InjectMocks
  private GeographicZoneService geographicZoneService;

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
}
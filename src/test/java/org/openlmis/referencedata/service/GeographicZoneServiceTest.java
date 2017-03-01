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
  private GeographicZone children;

  @Mock
  private GeographicZone secondChildren;

  @Mock
  private GeographicZone childrenOfChildren;

  @InjectMocks
  private GeographicZoneService geographicZoneService;

  @Test
  public void shouldRetrieveOneDescendant() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Collections.singletonList(children));

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Collections.singletonList(children), allZonesInHierarchy);
  }

  @Test
  public void shouldRetrieveManyDescendantsWhenTheChildrenHasAChildren() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Collections.singletonList(children));
    when(geographicZoneRepository.findByParent(children))
        .thenReturn(Collections.singletonList(childrenOfChildren));

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Arrays.asList(children, childrenOfChildren), allZonesInHierarchy);
  }

  @Test
  public void shouldRetrieveManyDescendantsWhenParentHasManyChildren() {
    when(geographicZoneRepository.findByParent(parent))
        .thenReturn(Arrays.asList(children, secondChildren));

    Collection<GeographicZone> allZonesInHierarchy =
        geographicZoneService.getAllZonesInHierarchy(parent);

    assertEquals(Arrays.asList(children, secondChildren), allZonesInHierarchy);
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
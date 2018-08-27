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

package org.openlmis.referencedata.fhir;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFhirClientTest {

  private static final String SERVICE_URL = "http://localhost";

  @Mock
  private LocationFactory locationFactory;

  @Mock
  private LocationConverter locationConvert;

  @Mock
  private LocationSynchronizer locationSynchronizer;

  @Mock
  private IBaseResource resource;

  private FhirClient client;

  @Before
  public void setUp() {
    client = new DefaultFhirClient(locationFactory, locationConvert, locationSynchronizer);
  }

  @Test
  public void shouldSynchronizeFacility() {
    //given
    Facility facility = new FacilityDataBuilder().build();
    Location location = new Location(SERVICE_URL, facility);

    // when
    when(locationFactory.createFor(facility)).thenReturn(location);
    when(locationConvert.convert(location)).thenReturn(resource);
    client.synchronizeFacility(facility);

    // then
    verify(locationFactory).createFor(facility);
    verify(locationConvert).convert(location);
    verify(locationSynchronizer).synchronize(location, resource);
  }

  @Test
  public void shouldSynchronizeGeographicZone() {
    //given
    GeographicZone geographicZone = new GeographicZoneDataBuilder().build();
    Location location = new Location(SERVICE_URL, geographicZone);

    // when
    when(locationFactory.createFor(geographicZone)).thenReturn(location);
    when(locationConvert.convert(location)).thenReturn(resource);
    client.synchronizeGeographicZone(geographicZone);

    // then
    verify(locationFactory).createFor(geographicZone);
    verify(locationConvert).convert(location);
    verify(locationSynchronizer).synchronize(location, resource);
  }
}

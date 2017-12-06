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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"PMD.TooManyMethods"})
public class GeographicZoneRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<GeographicZone> {

  private static final String ZONE = "zone";

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository repository;

  private GeographicLevel countryLevel = new GeographicLevel("country", 1);
  private GeographicLevel regionLevel = new GeographicLevel("region", 2);
  private GeographicLevel districtLevel = new GeographicLevel("district", 3);

  private GeographicZone countryZone;
  private GeographicZone regionZone;
  private GeographicZone districtZone;
  
  private GeometryFactory gf;

  @Override
  GeographicZoneRepository getRepository() {
    return this.repository;
  }

  @Before
  public void setUp() {
    geographicLevelRepository.save(countryLevel);
    geographicLevelRepository.save(regionLevel);
    geographicLevelRepository.save(districtLevel);
    gf = new GeometryFactory();
  }

  @Override
  GeographicZone generateInstance() {

    countryZone = new GeographicZone();
    countryZone.setCode("C" + this.getNextInstanceNumber());
    countryZone.setLevel(countryLevel);
    Coordinate[] regionAndCountryCoords  = new Coordinate[] {
        new Coordinate(0, 0),
        new Coordinate(4, 0),
        new Coordinate(4, 2),
        new Coordinate(0, 2),
        new Coordinate(0, 0)
    };
    Polygon regionAndCountryBoundary = gf.createPolygon(regionAndCountryCoords);
    countryZone.setBoundary(regionAndCountryBoundary);

    repository.save(countryZone);

    regionZone = new GeographicZone();
    regionZone.setCode("R" + this.getNextInstanceNumber());
    regionZone.setLevel(regionLevel);
    regionZone.setParent(countryZone);
    regionZone.setBoundary(regionAndCountryBoundary);

    repository.save(regionZone);

    districtZone = new GeographicZone();
    districtZone.setCode("D" + this.getNextInstanceNumber());
    districtZone.setName(ZONE + this.getNextInstanceNumber());
    districtZone.setLevel(districtLevel);
    districtZone.setParent(regionZone);
    Coordinate[] districtCoords  = new Coordinate[] {
        new Coordinate(0, 0),
        new Coordinate(2, 0),
        new Coordinate(2, 2),
        new Coordinate(0, 2),
        new Coordinate(0, 0)
    };
    districtZone.setBoundary(gf.createPolygon(districtCoords));

    return districtZone;
  }

  @Test
  public void shouldFindByParentAndLevel() {
    // given
    generateInstance();

    // when
    List<GeographicZone> zones = repository.findByParentAndLevel(
        regionZone.getParent(), regionZone.getLevel());

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.get(0).getId());
  }

  @Test
  public void shouldFindIdByParent() {
    // given
    generateInstance();

    // when
    Set<UUID> zones = repository.findIdsByParent(regionZone.getParent().getId());

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.iterator().next());
  }

  @Test
  public void shouldFindByLevel() {
    // given
    generateInstance();

    // when
    List<GeographicZone> zones = repository.findByLevel(regionZone.getLevel());

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.get(0).getId());
  }
  
  @Test
  public void shouldFindByLocation() {
    // given
    generateInstance();

    GeographicZone district2Zone = new GeographicZone();
    district2Zone.setCode("D" + this.getNextInstanceNumber());
    district2Zone.setLevel(districtLevel);
    district2Zone.setParent(regionZone);
    Coordinate[] district2Coords  = new Coordinate[] {
        new Coordinate(2, 0),
        new Coordinate(4, 0),
        new Coordinate(4, 2),
        new Coordinate(2, 2),
        new Coordinate(2, 0)
    };
    district2Zone.setBoundary(gf.createPolygon(district2Coords));
    repository.save(district2Zone);
    
    // Location is in district2Zone, regionZone, countryZone, but not districtZone
    Point location = gf.createPoint(new Coordinate(3, 1));
    
    // when
    List<GeographicZone> zones = repository.findByLocation(location);

    // then
    assertEquals(3, zones.size());
    assertThat(zones, hasItem(district2Zone));
    assertThat(zones, hasItem(regionZone));
    assertThat(zones, hasItem(countryZone));
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCode() {
    GeographicZone geographicZone = generateInstance();
    repository.save(geographicZone);
    GeographicZone geographicZone1 = generateInstance();
    repository.save(geographicZone1);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(null, geographicZone.getCode(), null, null,
        pageable, 1, geographicZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCodeIgnoringCase() {
    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(null, geographicZone.getCode().toUpperCase(), null, null,
        pageable, 1, geographicZone);
    searchZonesAndCheckResults(null, geographicZone.getCode().toLowerCase(), null, null,
        pageable, 1, geographicZone);
    searchZonesAndCheckResults(null, "d", null, null, pageable, 1, geographicZone);
    searchZonesAndCheckResults(null, "D", null, null, pageable, 1, geographicZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarName() {
    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(ZONE, null, null, null, pageable, 1, geographicZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarNameIgnoringCase() {
    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(ZONE, null, null, null, pageable, 1, geographicZone);
    searchZonesAndCheckResults("ZONE", null, null, null, pageable, 1, geographicZone);
    searchZonesAndCheckResults("ZoNe", null, null, null, pageable, 1, geographicZone);
    searchZonesAndCheckResults("zONe", null, null, null, pageable, 1, geographicZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCodeOrName() {
    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);
    GeographicZone geographicZone1 = generateInstance();
    repository.save(geographicZone1);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(ZONE, "D", null, null,
        pageable, 2, geographicZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCodeOrNameIgnoringCase() {
    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);
    GeographicZone geographicZone1 = generateInstance();
    repository.save(geographicZone1);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults("zon", "d", null, null,
        pageable, 2, geographicZone);
    searchZonesAndCheckResults("ZONE", "D", null, null,
        pageable, 2, geographicZone);
  }

  @Test
  public void shouldNotFindAnyGeographicZoneForIncorrectCodeAndName() {
    Pageable pageable = mockPageable(0, 10);
    Page<GeographicZone> foundZones = repository.search("Cucumber", "Tomato",
        null, null, pageable);

    assertEquals(0, foundZones.getContent().size());
  }

  @Test
  public void shouldReturnGeographicZonesWithFullCount() {
    assertEquals(0, repository.count());

    for (int i = 0; i < 10; i++) {
      GeographicZone zone  = generateInstance();
      repository.save(zone);
    }

    // different code
    GeographicZone zone = generateInstance();
    zone.setCode("XXX");
    repository.save(zone);

    assertEquals(33, repository.count());

    Pageable pageable = mockPageable(3, 0);

    Page<GeographicZone> result = repository.search(null, "XXX", null, null, pageable);

    assertEquals(1, result.getContent().size());
    assertEquals(1, result.getTotalElements());

    result = repository.search(null, "D", null, null, pageable);

    assertEquals(3, result.getContent().size());
    assertEquals(10, result.getTotalElements());
  }

  @Test
  public void shouldFindGeographicZonesByParent() {

    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(null, null,
        geographicZone.getParent(),
        null, pageable, 1, geographicZone);
  }

  @Test
  public void shouldFindGeographicZonesByLevel() {

    GeographicZone geographicZone = generateInstance();
    geographicZone = repository.save(geographicZone);

    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(null, null,
        geographicZone.getParent(),
        null, pageable, 1, geographicZone);
  }

  @Test
  public void shouldSortByName() {
    GeographicZone geographicZone = generateInstance();
    geographicZone.setName("zone-a");
    geographicZone = repository.save(geographicZone);

    GeographicZone geographicZone1 = generateInstance();
    geographicZone1.setName("zone-b");
    geographicZone1 = repository.save(geographicZone1);

    Pageable pageable = mockPageable(0, 10);

    Page<GeographicZone> foundPage = repository.search("zone", null, null,
        null, pageable);
    assertEquals(2, foundPage.getContent().size());
    assertEquals(geographicZone.getName(), foundPage.getContent().get(0).getName());
    assertEquals(geographicZone1.getName(), foundPage.getContent().get(1).getName());
  }

  @Test
  public void shouldReturnEmptyListIfSearchParametersAreNotProvided() {
    Pageable pageable = mockPageable(0, 10);
    given(pageable.getSort()).willReturn(new Sort(new Sort.Order(Sort.Direction.ASC, "name")));

    Page<GeographicZone> foundPage = repository.search(null, null, null,
        null, pageable);
    assertEquals(0, foundPage.getContent().size());

    foundPage = repository.search(null, null, null,
        null, pageable);
    assertEquals(0, foundPage.getContent().size());
  }

  private void searchZonesAndCheckResults(String name, String code, GeographicZone parent,
                                          GeographicLevel geographicLevel,
                                          Pageable pageable, int expectedSize,
                                          GeographicZone geographicZone) {
    Page<GeographicZone> foundPage = repository.search(name, code, parent,
        geographicLevel, pageable);

    assertEquals(expectedSize, foundPage.getContent().size());

    assertEquals(geographicZone.getName(), foundPage.getContent().get(0).getName());
  }

  private Pageable mockPageable(int pageSize, int pageNumber) {
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(pageNumber);
    given(pageable.getPageSize()).willReturn(pageSize);
    given(pageable.getSort()).willReturn(null);
    return pageable;
  }

  @Override
  protected void assertInstance(GeographicZone district) {
    super.assertInstance(district);

    assertThat(district.getCode(), startsWith("D"));
    assertThat(district.getLevel().getLevelNumber(), is(3));
    assertThat(district.getParent().getId(), is(regionZone.getId()));

    assertThat(district.getParent().getCode(), startsWith("R"));
    assertThat(district.getParent().getLevel().getLevelNumber(), is(2));
    assertThat(district.getParent().getParent().getId(), is(countryZone.getId()));

    assertThat(district.getParent().getParent().getCode(), startsWith("C"));
    assertThat(district.getParent().getParent().getLevel().getLevelNumber(), is(1));
    assertThat(district.getParent().getParent().getParent(), is(nullValue()));
  }
}

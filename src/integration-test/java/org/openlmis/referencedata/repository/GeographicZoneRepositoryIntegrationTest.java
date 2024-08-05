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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.testbuilder.GeographicLevelDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@SuppressWarnings({"PMD.TooManyMethods"})
public class GeographicZoneRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<GeographicZone> {

  private static final String ZONE = "zone";

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository repository;

  private GeographicLevel countryLevel = new GeographicLevelDataBuilder()
      .withLevelNumber(1)
      .buildAsNew();
  private GeographicLevel regionLevel = new GeographicLevelDataBuilder()
      .withLevelNumber(2)
      .buildAsNew();
  private GeographicLevel districtLevel = new GeographicLevelDataBuilder()
      .withLevelNumber(3)
      .buildAsNew();

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
    geographicLevelRepository.deleteAll();
    repository.deleteAll();

    geographicLevelRepository.save(countryLevel);
    geographicLevelRepository.save(regionLevel);
    geographicLevelRepository.save(districtLevel);
    gf = new GeometryFactory();

    repository.save(generateInstance());
  }

  @Override
  GeographicZone generateInstance() {
    Coordinate[] regionAndCountryCoords  = new Coordinate[] {
        new Coordinate(0, 0),
        new Coordinate(4, 0),
        new Coordinate(4, 2),
        new Coordinate(0, 2),
        new Coordinate(0, 0)
    };
    Polygon regionAndCountryBoundary = gf.createPolygon(regionAndCountryCoords);

    countryZone = new GeographicZoneDataBuilder()
        .withLevel(countryLevel)
        .withBoundary(regionAndCountryBoundary)
        .buildAsNew();

    regionZone = new GeographicZoneDataBuilder()
        .withLevel(regionLevel)
        .withParent(countryZone)
        .withBoundary(regionAndCountryBoundary)
        .buildAsNew();

    Coordinate[] districtCoords  = new Coordinate[] {
        new Coordinate(0, 0),
        new Coordinate(2, 0),
        new Coordinate(2, 2),
        new Coordinate(0, 2),
        new Coordinate(0, 0)
    };

    districtZone = new GeographicZoneDataBuilder()
        .withLevel(districtLevel)
        .withParent(regionZone)
        .withBoundary(gf.createPolygon(districtCoords))
        .buildAsNew();

    repository.save(countryZone);
    repository.save(regionZone);
    return districtZone;
  }

  @Test
  public void shouldFindByParentAndLevel() {
    // when
    List<GeographicZone> zones = repository.findByParentAndLevel(countryZone, regionLevel);

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.get(0).getId());
  }

  @Test
  public void shouldFindIdByParent() {
    // when
    Set<UUID> zones = repository.findIdsByParent(countryZone.getId());

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.iterator().next());
  }

  @Test
  public void shouldFindByLevel() {
    // when
    List<GeographicZone> zones = repository.findByLevel(regionLevel);

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.get(0).getId());
  }
  
  @Test
  public void shouldFindByLocation() {
    // given
    // Location is in regionZone, countryZone, but not districtZone
    Point location = gf.createPoint(new Coordinate(3, 1));
    
    // when
    List<GeographicZone> zones = repository.findByLocation(location);

    // then
    assertEquals(2, zones.size());
    assertThat(zones, hasItem(regionZone));
    assertThat(zones, hasItem(countryZone));
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCode() {
    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(null, districtZone.getCode(), null, null,
        pageable, 1, districtZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCodeIgnoringCase() {
    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(null, districtZone.getCode().toUpperCase(), null, null,
        pageable, 1, districtZone);
    searchZonesAndCheckResults(null, districtZone.getCode().toLowerCase(), null, null,
        pageable, 1, districtZone);
    searchZonesAndCheckResults(null, "gz", null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults(null, "gZ", null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults(null, "Gz", null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults(null, "GZ", null, null, pageable, 3, districtZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarName() {
    Pageable pageable = mockPageable(0, 10);
    searchZonesAndCheckResults(ZONE, null, null, null, pageable, 3, districtZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarNameIgnoringCase() {
    Pageable pageable = mockPageable(0, 10);

    searchZonesAndCheckResults(ZONE, null, null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults("ZONE", null, null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults("ZoNe", null, null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults("zONe", null, null, null, pageable, 3, districtZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCodeOrName() {
    Pageable pageable = mockPageable(0, 10);
    searchZonesAndCheckResults(ZONE, "GZ", null, null, pageable, 3, districtZone);
  }

  @Test
  public void shouldFindGeographicZonesWithSimilarCodeOrNameIgnoringCase() {
    Pageable pageable = mockPageable(0, 10);
    searchZonesAndCheckResults("zon", "gz", null, null, pageable, 3, districtZone);
    searchZonesAndCheckResults("ZONE", "GZ", null, null, pageable, 3, districtZone);
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
    Pageable pageable = mockPageable(0, 1);

    Page<GeographicZone> result = repository.search(
        null, districtZone.getCode(), null, null, pageable
    );

    assertEquals(1, result.getContent().size());
    assertEquals(1, result.getTotalElements());

    result = repository.search(null, "GZ", null, null, pageable);

    assertEquals(1, result.getContent().size());
    assertEquals(3, result.getTotalElements());
  }

  @Test
  public void shouldFindGeographicZonesByParent() {
    Pageable pageable = mockPageable(0, 10);
    searchZonesAndCheckResults(
        null, null, regionZone, null, pageable, 1, districtZone
    );
  }

  @Test
  public void shouldFindGeographicZonesByLevel() {
    Pageable pageable = mockPageable(0, 10);
    searchZonesAndCheckResults(
        null, null, null, districtLevel, pageable, 1, districtZone
    );
  }

  @Test
  public void shouldSortByName() {
    Pageable pageable = mockPageable(0, 10);

    Page<GeographicZone> foundPage = repository.search(
        "Geographic Zone", null, null, null, pageable
    );

    assertEquals(3, foundPage.getContent().size());
    assertEquals(countryZone.getName(), foundPage.getContent().get(0).getName());
    assertEquals(regionZone.getName(), foundPage.getContent().get(1).getName());
    assertEquals(districtZone.getName(), foundPage.getContent().get(2).getName());
  }

  @Test
  public void shouldReturnEmptyListIfSearchParametersAreNotProvided() {
    Pageable pageable = mockPageable(0, 10);
    given(pageable.getSort()).willReturn(Sort.by(new Sort.Order(Sort.Direction.ASC, "name")));

    Page<GeographicZone> foundPage = repository.search(null, null, null,
        null, pageable);
    assertEquals(0, foundPage.getContent().size());

    foundPage = repository.search(null, null, null,
        null, pageable);
    assertEquals(0, foundPage.getContent().size());
  }

  @Test
  public void shouldExcludeByLevelCode() {
    Pageable pageable = mockPageable(0, 10);
    Page<GeographicZone> foundPage =
        repository.findByLevelCodeNotIn(
            Collections.singletonList(districtLevel.getCode()), pageable);
    assertNotNull(foundPage);
    assertEquals(2, foundPage.getTotalElements());
    assertFalse(foundPage.getContent().stream()
        .anyMatch(gz -> gz.getLevel().getCode().equals(districtLevel.getCode())));
  }

  private void searchZonesAndCheckResults(String name, String code, GeographicZone parent,
                                          GeographicLevel geographicLevel,
                                          Pageable pageable, int expectedSize,
                                          GeographicZone zone) {
    Page<GeographicZone> foundPage = repository.search(name, code, parent,
        geographicLevel, pageable);

    assertThat(foundPage.getContent(), hasSize(expectedSize));
    assertThat(foundPage.getContent(), hasItem(hasProperty("name", equalTo(zone.getName()))));
  }

  private Pageable mockPageable(int pageNumber, int pageSize) {
    Pageable pageable = mock(Pageable.class);
    given(pageable.getPageNumber()).willReturn(pageNumber);
    given(pageable.getPageSize()).willReturn(pageSize);
    given(pageable.getSort()).willReturn(Sort.unsorted());
    return pageable;
  }

  @Override
  protected void assertInstance(GeographicZone district) {
    super.assertInstance(district);

    assertThat(district.getCode(), startsWith("GZ"));
    assertThat(district.getLevel().getLevelNumber(), is(3));
    assertThat(district.getParent().getId(), is(regionZone.getId()));

    assertThat(district.getParent().getCode(), startsWith("GZ"));
    assertThat(district.getParent().getLevel().getLevelNumber(), is(2));
    assertThat(district.getParent().getParent().getId(), is(countryZone.getId()));

    assertThat(district.getParent().getParent().getCode(), startsWith("GZ"));
    assertThat(district.getParent().getParent().getLevel().getLevelNumber(), is(1));
    assertThat(district.getParent().getParent().getParent(), is(nullValue()));
  }
}

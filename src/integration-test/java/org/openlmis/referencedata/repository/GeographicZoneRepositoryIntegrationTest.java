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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.GeographicLevel;
import org.openlmis.referencedata.domain.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;

public class GeographicZoneRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<GeographicZone> {

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
  public void shouldFindByParent() {
    // given
    generateInstance();

    // when
    List<GeographicZone> zones = repository.findByParent(regionZone.getParent());

    // then
    assertEquals(1, zones.size());
    assertEquals(regionZone.getId(), zones.get(0).getId());
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

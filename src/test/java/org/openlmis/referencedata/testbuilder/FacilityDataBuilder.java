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

package org.openlmis.referencedata.testbuilder;

import com.vividsolutions.jts.geom.Point;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FacilityOperator;
import org.openlmis.referencedata.domain.FacilityType;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.domain.Program;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FacilityDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String name;
  private String description;
  private GeographicZone geographicZone;
  private FacilityType type;
  private FacilityOperator operator;
  private Boolean active;
  private LocalDate goLiveDate;
  private LocalDate goDownDate;
  private String comment;
  private Boolean enabled;
  private Boolean openLmisAccessible;
  private Set<Program> supportedPrograms;
  private Point location;
  private Map<String, String> extraData;

  /**
   * Returns instance of {@link FacilityDataBuilder} with sample data.
   */
  public FacilityDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    code = "F" + instanceNumber;
    name = "Facility " + instanceNumber;
    geographicZone = new GeographicZoneDataBuilder().build();
    type = new FacilityTypeDataBuilder().build();
    operator = new FacilityOperatorDataBuilder().build();
    active = true;
    enabled = true;
    openLmisAccessible = true;
    supportedPrograms = new HashSet<>();
  }

  /**
   * Builds instance of {@link Facility}.
   */
  public Facility build() {

    //TODO: add new constructor, AllArgsConstructor fails 1 integration test
    Facility facility = new Facility(code);
    facility.setId(id);
    facility.setName(name);
    facility.setDescription(description);
    facility.setGeographicZone(geographicZone);
    facility.setType(type);
    facility.setOperator(operator);
    facility.setActive(active);
    facility.setGoLiveDate(goLiveDate);
    facility.setGoDownDate(goDownDate);
    facility.setComment(comment);
    facility.setEnabled(enabled);
    facility.setOpenLmisAccessible(openLmisAccessible);
    facility.setLocation(location);
    facility.setExtraData(extraData);

    supportedPrograms.stream()
        .forEach(p -> facility.addSupportedProgram(new SupportedProgramDataBuilder()
            .withProgram(p)
            .withFacility(facility)
            .build()));

    return facility;
  }

  /**
   * Adds supported program for new {@link Facility}.
   */
  public FacilityDataBuilder withSupportedProgram(Program program) {
    supportedPrograms.add(program);
    return this;
  }

  /**
   * Adds geographic zone with parent for new {@link Facility}.
   */
  public FacilityDataBuilder withGeographicZoneWithParent() {
    GeographicZone parent = new GeographicZoneDataBuilder().build();
    this.geographicZone = new GeographicZoneDataBuilder()
        .withParent(parent).build();
    return this;
  }
}

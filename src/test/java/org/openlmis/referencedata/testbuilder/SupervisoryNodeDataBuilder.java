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

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.RequisitionGroup;
import org.openlmis.referencedata.domain.SupervisoryNode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SupervisoryNodeDataBuilder {

  private static int instanceNumber = 0;

  private UUID id;
  private String code;
  private String name;
  private String description;
  private Facility facility;
  private SupervisoryNode parentNode;
  private Set<SupervisoryNode> childNodes;
  private RequisitionGroup requisitionGroup;

  /**
   * Returns instance of {@link SupervisoryNodeDataBuilder} with sample data.
   */
  public SupervisoryNodeDataBuilder() {
    instanceNumber++;

    id = UUID.randomUUID();
    code = "SN" + instanceNumber;
    name = "Supervisory Node " + instanceNumber;
    facility = new FacilityDataBuilder().build();
    childNodes = new HashSet<>();
    //TODO: requisition group builder
  }

  /**
   * Builds instance of {@link SupervisoryNode}.
   */
  public SupervisoryNode build() {
    SupervisoryNode node = new SupervisoryNode(code, name, description, facility, parentNode,
        childNodes, requisitionGroup);
    node.setId(id);

    return node;
  }

  /**
   * Sets id with null value for new {@link SupervisoryNode}.
   */
  public SupervisoryNodeDataBuilder withoutId() {
    this.id = null;
    return this;
  }

  /**
   * Sets facility for new {@link SupervisoryNode}.
   */
  public SupervisoryNodeDataBuilder withFacility(Facility facility) {
    this.facility = facility;
    return this;
  }

  /**
   * Sets code for new {@link SupervisoryNode}.
   *
   * @param code  the code to be set
   * @return the builder instance
   */
  public SupervisoryNodeDataBuilder withCode(String code) {
    this.code = code;
    return this;
  }

  /**
   * Sets the code to null for new {@link SupervisoryNode}.
   *
   * @return the builder instance
   */
  public SupervisoryNodeDataBuilder withoutCode() {
    return withCode(null);
  }
}

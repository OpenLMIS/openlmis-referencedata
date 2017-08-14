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

package org.openlmis.referencedata.dto;

import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This DTO is used in two ways; 1) to get an intermediate right assignment from the database, 
 * and 2) to provide a structure for inserting back into the database. The first can potentially 
 * have a supervisorynodeid set, while the second only uses the fields which match a right 
 * assignment (i.e. not the supervisorynodeid).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RightAssignmentDto extends BaseDto {
  
  private UUID userId;
  private String rightName;
  private UUID facilityId;
  private UUID programId;
  private UUID supervisoryNodeId;

  /**
   * Constructor of the key fields.
   * 
   * @param userId user ID
   * @param rightName right name
   * @param facilityId facility ID
   * @param programId program ID
   */
  public RightAssignmentDto(UUID userId, String rightName, UUID facilityId, UUID programId) {
    this.userId = userId;
    this.rightName = rightName;
    this.facilityId = facilityId;
    this.programId = programId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RightAssignmentDto)) {
      return false;
    }
    RightAssignmentDto that = (RightAssignmentDto) obj;
    return Objects.equals(userId, that.userId)
        && Objects.equals(rightName, that.rightName)
        && Objects.equals(facilityId, that.facilityId)
        && Objects.equals(programId, that.programId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, rightName, facilityId, programId);
  }
}

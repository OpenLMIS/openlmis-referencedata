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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.referencedata.web.csv.model.ImportField;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserContactDetailsDto extends BaseDto {

  @ImportField(name = "phoneNumber")
  private String phoneNumber;

  private Boolean allowNotify;

  @ImportField(name = "email")
  private String email;

  @ImportField(name = "isEmailVerified")
  private Boolean emailVerified;

  /**
   * Builds api contract object from current dto class.
   *
   * @return {@link UserContactDetailsApiContract} object
   */
  public UserContactDetailsApiContract toUserContactDetailsApiContract() {
    UserContactDetailsApiContract.EmailDetails emailDetails =
        new UserContactDetailsApiContract.EmailDetails(this.getEmail(), this.getEmailVerified());

    return new UserContactDetailsApiContract(
        this.getPhoneNumber(), this.getAllowNotify(), emailDetails);
  }

  @ToString
  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Setter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class UserContactDetailsApiContract {
    private UUID referenceDataUserId;
    private String phoneNumber;
    private Boolean allowNotify;
    private EmailDetails emailDetails;

    /**
     * Builds {@link UserContactDetailsApiContract} object from current dto class.
     *
     * @param phoneNumber phone number
     * @param allowNotify allow notify flag
     * @param emailDetails {@link EmailDetails} object
     */
    public UserContactDetailsApiContract(String phoneNumber,
                                         Boolean allowNotify,
                                         EmailDetails emailDetails) {
      this.phoneNumber = phoneNumber;
      this.allowNotify = allowNotify;
      this.emailDetails = emailDetails;
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EmailDetails {
      private String email;
      private Boolean emailVerified;
    }
  }
}

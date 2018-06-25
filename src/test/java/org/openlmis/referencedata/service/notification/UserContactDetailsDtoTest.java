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

package org.openlmis.referencedata.service.notification;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.referencedata.ToStringTestUtils;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;

public class UserContactDetailsDtoTest {

  @Test
  public void shouldCreateFromUserDto() {
    UserDto userDto = new UserDto();
    new UserDataBuilder().build().export(userDto);

    UserContactDetailsDto contactDetails = new UserContactDetailsDto(userDto);
    assertThat(contactDetails.getReferenceDataUserId()).isEqualTo(userDto.getId());
    assertThat(contactDetails.getPhoneNumber()).isEqualTo(userDto.getPhoneNumber());
    assertThat(contactDetails.getAllowNotify()).isEqualTo(userDto.getAllowNotify());
    assertThat(contactDetails.getEmailDetails().getEmail()).isEqualTo(userDto.getEmail());
    assertThat(contactDetails.getEmailDetails().getEmailVerified()).isEqualTo(userDto.isVerified());
  }

  @Test
  public void shouldNotSetEmailDetailsIfUserDoesNotHaveIt() {
    UserDto userDto = new UserDto();
    new UserDataBuilder().withEmail(null).build().export(userDto);

    UserContactDetailsDto contactDetails = new UserContactDetailsDto(userDto);
    assertThat(contactDetails.getReferenceDataUserId()).isEqualTo(userDto.getId());
    assertThat(contactDetails.getPhoneNumber()).isEqualTo(userDto.getPhoneNumber());
    assertThat(contactDetails.getAllowNotify()).isEqualTo(userDto.getAllowNotify());
    assertThat(contactDetails.getEmailDetails()).isNull();
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserContactDetailsDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(UserContactDetailsDto.class, new UserContactDetailsDto());
  }

}

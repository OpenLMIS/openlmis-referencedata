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

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import org.junit.Test;
import java.util.UUID;

/**
 * Created by pawel on 16.11.17.
 */
public class UserSearchParamsTest {

  @Test
  public void shouldReturnTrueIfAllFieldsAreEmpty() {
    UserSearchParams userSearchParams = new UserSearchParams();

    assertTrue(userSearchParams.isEmpty());
  }

  @Test
  public void shouldReturnListOfIdsParsedToUuid() {
    UUID idOne = UUID.randomUUID();
    UUID idTwo = UUID.randomUUID();
    UserSearchParams userSearchParams =
        new UserSearchParams(
            Sets.newHashSet(idOne.toString(), idTwo.toString()));

    assertThat(userSearchParams.getIds(), hasItems(idOne, idTwo));
  }

  @Test
  public void shouldReturnHomeFacilityUuid() {
    UUID id = UUID.randomUUID();
    UserSearchParams userSearchParams = new UserSearchParams();
    userSearchParams.setHomeFacilityId(id.toString());

    assertEquals(id, userSearchParams.getHomeFacilityUuid());
  }

}
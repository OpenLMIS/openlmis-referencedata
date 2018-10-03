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

package org.openlmis.referencedata.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class FhirLocationTest {
  private TestFhirLocation location = new TestFhirLocation();

  @Test
  public void shouldReturnTrueIfIsFhirLocationOwnerFlagIsSetToTrue() {
    location.changeFlag(Boolean.TRUE.toString());
    assertThat(location.isFhirLocationOwnerSet()).isTrue();
  }

  @Test
  public void shouldReturnFalseIfIsFhirLocationOwnerFlagIsSetToFalse() {
    location.changeFlag(Boolean.FALSE.toString());
    assertThat(location.isFhirLocationOwnerSet()).isFalse();
  }

  @Test
  public void shouldReturnFalseIfIsFhirLocationOwnerFlagHasIncorrectValue() {
    location.changeFlag("def");
    assertThat(location.isFhirLocationOwnerSet()).isFalse();
  }

  @Test
  public void shouldReturnFalseIfIsFhirLocationOwnerFlagIsNotSet() {
    location.changeFlag(null);
    assertThat(location.isFhirLocationOwnerSet()).isFalse();
  }

  private static final class TestFhirLocation implements FhirLocation {
    private Map<String, String> map = Maps.newHashMap();

    @Override
    public UUID getId() {
      return UUID.randomUUID();
    }

    @Override
    public Map<String, String> getExtraData() {
      return map;
    }

    void changeFlag(String value) {
      if (null == value) {
        map.remove("isFhirLocationOwner");
      } else {
        map.put("isFhirLocationOwner", value);
      }
    }
  }

}

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

package org.openlmis.referencedata.service.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.FacilityDto;
import org.openlmis.referencedata.dto.FacilityOperatorDto;
import org.openlmis.referencedata.dto.FacilityTypeDto;
import org.openlmis.referencedata.dto.GeographicZoneSimpleDto;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityOperatorDataBuilder;
import org.openlmis.referencedata.testbuilder.FacilityTypeDataBuilder;
import org.openlmis.referencedata.testbuilder.GeographicZoneDataBuilder;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.validate.CsvHeaderValidator;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.springframework.core.io.ClassPathResource;

@RunWith(MockitoJUnitRunner.class)
public class FacilityDtoCsvReadTest {
  @Mock private CsvHeaderValidator csvHeaderValidator;
  @InjectMocks private FileHelper fileHelper;

  @Before
  public void setup() {
    Mockito.doNothing()
        .when(csvHeaderValidator)
        .validateHeaders(any(List.class), any(ModelClass.class), anyBoolean());
  }

  @Test
  public void shouldReadFacilityDtoFromCsv() throws IOException {
    final FacilityDto expectedFacility1 =
        FacilityDto.newInstance(
            new FacilityDataBuilder()
                .withCode("TEST1")
                .withName("Test1")
                .witActive(true)
                .witEnabled(true)
                .withType(new FacilityTypeDataBuilder().withCode("TestType").build())
                .withGeographicZone(new GeographicZoneDataBuilder().withCode("TestZone").build())
                .withDescription("TestDecription")
                .withOperator(new FacilityOperatorDataBuilder().withCode("TestOperator").build())
                .withGoLiveDate(LocalDate.of(2024, 10, 7))
                .withGoDownDate(LocalDate.of(2034, 10, 7))
                .withComment("TestComment")
                .withOpenLmisAccessible(true)
                .buildAsNew());

    final FacilityDto expectedFacility2 =
        FacilityDto.newInstance(
            new FacilityDataBuilder()
                .withCode("TEST2")
                .withName(null)
                .witActive(null)
                .witEnabled(null)
                .withType(new FacilityTypeDataBuilder().withCode("TestType").build())
                .withGeographicZone(new GeographicZoneDataBuilder().withCode("TestZone").build())
                .withDescription(null)
                .withOperator(null)
                .withGoLiveDate(null)
                .withGoDownDate(null)
                .withComment(null)
                .withOpenLmisAccessible(null)
                .buildAsNew());

    final InputStream csvStream =
        new ClassPathResource("/FacilityDtoCsvReadTest/shouldReadFacilityDtoFromCsv.csv")
            .getInputStream();
    final List<FacilityDto> facilityDtos = fileHelper.readCsv(FacilityDto.class, csvStream);

    assertEquals(2, facilityDtos.size());
    assertByContentFromCsv(expectedFacility1, facilityDtos.get(0));
    assertByContentFromCsv(expectedFacility2, facilityDtos.get(1));
  }

  @SuppressWarnings("PMD.CyclomaticComplexity")
  private void assertByContentFromCsv(FacilityDto expected, FacilityDto actual) {
    if (expected == null || actual == null) {
      fail("one is null");
      return;
    }

    if (!Objects.equals(expected.getCode(), actual.getCode())) {
      fail("code not equal");
    } else if (!Objects.equals(expected.getName(), actual.getName())) {
      fail("name not equal");
    } else if (!Objects.equals(expected.getActive(), actual.getActive())) {
      fail("active not equal");
    } else if (!Objects.equals(expected.getEnabled(), actual.getEnabled())) {
      fail("enabled not equal");
    } else if (!equalsByContentFromCsv(expected.getType(), actual.getType())) {
      fail("type not equal");
    } else if (!equalsByContentFromCsv(expected.getGeographicZone(), actual.getGeographicZone())) {
      fail("geographicZone not equal");
    } else if (!Objects.equals(expected.getDescription(), actual.getDescription())) {
      fail("description not equal");
    } else if (!equalsByContentFromCsv(expected.getOperator(), actual.getOperator())) {
      fail("operator not equal");
    } else if (!Objects.equals(expected.getGoLiveDate(), actual.getGoLiveDate())) {
      fail("goLiveDate not equal");
    } else if (!Objects.equals(expected.getGoDownDate(), actual.getGoDownDate())) {
      fail("goDownDate not equal");
    } else if (!Objects.equals(expected.getComment(), actual.getComment())) {
      fail("comment not equal");
    } else if (!Objects.equals(expected.getOpenLmisAccessible(), actual.getOpenLmisAccessible())) {
      fail("openLmisAccessible not equal");
    }
  }

  private boolean equalsByContentFromCsv(FacilityTypeDto first, FacilityTypeDto second) {
    if (first == null || second == null) {
      return first == second;
    }

    return Objects.equals(first.getCode(), second.getCode());
  }

  private boolean equalsByContentFromCsv(
      GeographicZoneSimpleDto first, GeographicZoneSimpleDto second) {
    if (first == null || second == null) {
      return first == second;
    }

    return Objects.equals(first.getCode(), second.getCode());
  }

  private boolean equalsByContentFromCsv(FacilityOperatorDto first, FacilityOperatorDto second) {
    if (first == null || second == null) {
      return first == second;
    }

    return Objects.equals(first.getCode(), second.getCode());
  }
}

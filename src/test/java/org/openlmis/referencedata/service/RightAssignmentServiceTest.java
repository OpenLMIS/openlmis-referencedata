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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.dto.RightAssignmentDto;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

@RunWith(MockitoJUnitRunner.class)
public class RightAssignmentServiceTest {

  private static final String RIGHT_NAME = "rightName";
  private static final String SQL_CONTENT = "SELECT 1";

  @Mock
  private JdbcTemplate jdbcTemplate;
  @Mock
  private Resource rightAssignmentsRes;
  @Mock
  private Resource nodeProgramFacilityRes;

  private RightAssignmentService rightAssignmentService;

  private UUID userId;
  private UUID supervisoryNodeId;
  private UUID programId;

  @Before
  public void setUp() throws IOException {
    userId = UUID.randomUUID();
    supervisoryNodeId = UUID.randomUUID();
    programId = UUID.randomUUID();

    when(rightAssignmentsRes.getInputStream())
        .thenReturn(new ByteArrayInputStream(SQL_CONTENT.getBytes(StandardCharsets.UTF_8)));

    when(nodeProgramFacilityRes.getInputStream())
        .thenReturn(new ByteArrayInputStream(SQL_CONTENT.getBytes(StandardCharsets.UTF_8)));

    rightAssignmentService = new RightAssignmentService(
        jdbcTemplate,
        rightAssignmentsRes,
        nodeProgramFacilityRes
    );
  }

  @Test
  public void convertForInsertShouldConvertDirectFulfillmentAndHomeFacilityRightAssignments() {
    // given
    UUID facilityId = UUID.randomUUID();
    RightAssignmentDto expected = new RightAssignmentDto(
        userId,
        RIGHT_NAME,
        facilityId,
        programId,
        null);

    // when
    List<Object[]> actual = rightAssignmentService
        .convertForInsert(Collections.singletonList(expected));

    // then
    assertEquals(1, actual.size());

    // Verify the Object[] row content
    Object[] row = actual.get(0);
    // [0]=ID, [1]=UserID, [2]=RightName, [3]=FacilityID, [4]=ProgramID
    assertEquals(expected.getUserId(), row[1]);
    assertEquals(RIGHT_NAME, row[2]);
    assertEquals(expected.getFacilityId(), row[3]);
    assertEquals(programId, row[4]);
  }

  @Test
  public void convertForInsertShouldConvertSupervisoryNodeRightAssignments() {
    // given
    RightAssignmentDto rightAssignmentDto = new RightAssignmentDto(
        userId,
        RIGHT_NAME,
        null,
        programId,
        supervisoryNodeId);

    UUID facility1Id = UUID.randomUUID();
    UUID facility2Id = UUID.randomUUID();

    Map<String, List<UUID>> mockHierarchyMap = new HashMap<>();
    String cacheKey = supervisoryNodeId.toString() + "_" + programId.toString();
    mockHierarchyMap.put(cacheKey, Arrays.asList(facility1Id, facility2Id));

    // Mock the cache loading
    when(jdbcTemplate.query(any(String.class), any(ResultSetExtractor.class)))
        .thenReturn(mockHierarchyMap);

    // when
    List<Object[]> actual = rightAssignmentService
        .convertForInsert(Collections.singletonList(rightAssignmentDto));

    // then
    assertEquals(2, actual.size());

    List<UUID> actualFacilityIds = actual.stream()
        .map(row -> (UUID) row[3])
        .collect(Collectors.toList());

    assertTrue(actualFacilityIds.contains(facility1Id));
    assertTrue(actualFacilityIds.contains(facility2Id));
  }
}

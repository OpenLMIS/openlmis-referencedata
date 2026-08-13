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

package org.openlmis.referencedata.web;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.referencedata.domain.RightName;
import org.openlmis.referencedata.dto.UserRoleAssignmentDto;
import org.openlmis.referencedata.repository.RoleAssignmentRepository;
import org.openlmis.referencedata.service.RightService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@SuppressWarnings({"PMD.UnusedPrivateField"})
public class RoleAssignmentControllerTest {

  @Mock
  private RoleAssignmentRepository roleAssignmentRepository;

  @Mock
  private RightService rightService;

  @InjectMocks
  private RoleAssignmentController controller;

  private UserRoleAssignmentDto roleAssignment;

  @Before
  public void setUp() {
    initMocks(this);
    roleAssignment = new UserRoleAssignmentDto(UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), null, null);
  }

  @Test
  public void shouldReturnAllRoleAssignments() {
    when(roleAssignmentRepository.findAllWithUser()).thenReturn(singletonList(roleAssignment));

    Page<UserRoleAssignmentDto> result = controller.getRoleAssignments(PageRequest.of(0, 10));

    List<UserRoleAssignmentDto> content = result.getContent();
    assertEquals(1, content.size());
    assertEquals(roleAssignment, content.get(0));
    assertEquals(1, result.getTotalElements());
  }

  @Test
  public void shouldCheckAdminRightBeforeReturningRoleAssignments() {
    when(roleAssignmentRepository.findAllWithUser()).thenReturn(singletonList(roleAssignment));

    controller.getRoleAssignments(PageRequest.of(0, 10));

    verify(rightService).checkAdminRight(RightName.USERS_MANAGE_RIGHT, true, null);
  }
}

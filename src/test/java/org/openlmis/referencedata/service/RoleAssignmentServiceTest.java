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
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.RoleAssignmentImportDto;
import org.openlmis.referencedata.repository.RoleAssignmentRepository;

@RunWith(MockitoJUnitRunner.class)
public class RoleAssignmentServiceTest {

  @Mock
  private RoleAssignmentRepository roleAssignmentRepository;

  @InjectMocks
  private RoleAssignmentService roleAssignmentService;

  @Test
  public void shouldFindAllExportableItems() {
    Right right = Right.newRight("VIEW_STOCK", RightType.SUPERVISION);
    Role role = Role.newRole("Stock Viewer", right);
    User user = new User();
    user.setUsername("john");
    Program program = new Program("WH01");

    RoleAssignment roleAssignment = new SupervisionRoleAssignment(role, user, program);
    when(roleAssignmentRepository.findAll()).thenReturn(Collections.singletonList(roleAssignment));

    List<RoleAssignmentImportDto> actual = roleAssignmentService.findAllExportableItems();

    assertEquals(1, actual.size());
    assertEquals("john", actual.get(0).getUsername());
    assertEquals("Stock Viewer", actual.get(0).getRoleName());
  }

  @Test
  public void shouldReturnCorrectExportableType() {
    assertEquals(RoleAssignmentImportDto.class, roleAssignmentService.getExportableType());
  }
}

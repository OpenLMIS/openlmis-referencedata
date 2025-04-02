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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, DataImportService.class})
@ActiveProfiles({"test", "test-run"})
public class RoleAssignmentPersisterIntegrationTest {

  @Autowired
  private RoleAssignmentPersister roleAssignmentPersister;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private ProgramRepository programRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(roleAssignmentPersister, "importExecutorService",
        MoreExecutors.newDirectExecutorService());
    ReflectionTestUtils.setField(roleAssignmentPersister, "userRepository", userRepository);
    ReflectionTestUtils.setField(roleAssignmentPersister, "roleRepository", roleRepository);
    ReflectionTestUtils.setField(roleAssignmentPersister, "facilityRepository", facilityRepository);
    ReflectionTestUtils.setField(roleAssignmentPersister, "programRepository", programRepository);
    ReflectionTestUtils.setField(
        roleAssignmentPersister, "supervisoryNodeRepository", supervisoryNodeRepository);
  }

  @Test
  public void shouldImportRoleAssignments() throws IOException, InterruptedException {
    User user = new User();
    user.setUsername("john");
    when(userRepository.findAllByUsernameIn(anyCollection()))
        .thenReturn(Collections.singletonList(user));
    when(roleRepository.findAllByNameIn(anyCollection()))
        .thenReturn(buildRoles());
    Facility warehouse = new Facility();
    warehouse.setId(UUID.randomUUID());
    warehouse.setCode("N076");
    when(facilityRepository.findAllByCodeIn(anyList()))
        .thenReturn(Collections.singletonList(warehouse));
    Program program = new Program();
    program.setCode(Code.code("PRG001"));
    when(programRepository.findAllByCodeIn(anyList()))
        .thenReturn(Collections.singletonList(program));
    SupervisoryNode supervisoryNode = new SupervisoryNode();
    supervisoryNode.setCode("110");
    when(supervisoryNodeRepository.findAllByCodeIn(anyList()))
        .thenReturn(Collections.singletonList(supervisoryNode));

    final ImportResponseDto.ImportDetails result = roleAssignmentPersister.processAndPersist(
        new ClassPathResource("/RoleAssignmentImportPersisterTest/roleAssignment.csv")
            .getInputStream(), mock(Profiler.class));

    assertEquals("roleAssignment.csv", result.getFileName());
    assertEquals(Integer.valueOf(4), result.getTotalEntriesCount());
    assertEquals(Integer.valueOf(3), result.getSuccessfulEntriesCount());
    assertEquals(Integer.valueOf(1), result.getFailedEntriesCount());
    assertEquals(1, result.getErrors().size());
  }

  private List<Role> buildRoles() {
    Right right1 = Right.newRight("VIEW_STOCK", RightType.SUPERVISION);
    Role role1 = Role.newRole("Stock Viewer", right1);
    Right right2 = Right.newRight("ORDERS_TRANSFER", RightType.ORDER_FULFILLMENT);
    Role role2 = Role.newRole("Warehouse Clerk", right2);
    Right right3 = Right.newRight("USERS_MANAGE", RightType.GENERAL_ADMIN);
    Role role3 = Role.newRole("System Administrator", right3);

    return Arrays.asList(role1, role2, role3);
  }
}

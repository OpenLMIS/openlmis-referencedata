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

package org.openlmis.referencedata.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.testbuilder.DirectRoleAssignmentDataBuilder;
import org.openlmis.referencedata.testbuilder.RightDataBuilder;
import org.openlmis.referencedata.testbuilder.RoleDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class RoleAssignmentRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<RoleAssignment> {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleAssignmentRepository repository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  private User user1;
  private User user2;
  private User user3;

  private Right right;

  private Role role1;
  private Role role2;
  private Role role3;

  @Override
  RoleAssignmentRepository getRepository() {
    return this.repository;
  }

  @Override
  RoleAssignment generateInstance() {
    return generateRoleAssignment(role1, user1);
  }

  @Before
  public void setUp() {
    user1 = userRepository.save(createUser());
    user2 = userRepository.save(createUser());
    user3 = userRepository.save(createUser());

    right = rightRepository.save(new RightDataBuilder().buildAsNew());

    role1 = roleRepository.save(createRole());
    role2 = roleRepository.save(createRole());
    role3 = roleRepository.save(createRole());
    roleRepository.save(createRole());
  }

  @Test
  public void shouldCountRoleAssignmentsByUser() {
    generateAndSaveRoleAssignment(role1, user1);
    generateAndSaveRoleAssignment(role1, user2);

    generateAndSaveRoleAssignment(role2, user1);
    generateAndSaveRoleAssignment(role2, user2);
    generateAndSaveRoleAssignment(role2, user3);

    generateAndSaveRoleAssignment(role3, user3);

    List<CountResource> result = repository.countUsersAssignedToRoles();

    assertThat(result, hasItems(
        new CountResource(role1.getId(), 2L),
        new CountResource(role2.getId(), 3L),
        new CountResource(role3.getId(), 1L)));
  }

  private RoleAssignment generateAndSaveRoleAssignment(Role role, User user) {
    return repository.save(generateRoleAssignment(role, user));
  }

  private RoleAssignment generateRoleAssignment(Role role, User user) {
    return new DirectRoleAssignmentDataBuilder()
        .withRole(role)
        .withUser(user)
        .buildAsNew();
  }

  private User createUser() {
    return new UserDataBuilder().buildAsNew();
  }

  private Role createRole() {
    return new RoleDataBuilder().withRights(right).buildAsNew();
  }
}

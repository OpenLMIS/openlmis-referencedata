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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.repository.custom.RoleRepositoryCustom;
import org.openlmis.referencedata.testbuilder.RightDataBuilder;
import org.openlmis.referencedata.testbuilder.RoleDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Allow testing roleRightsRepository.
 */

public class RoleRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Role> {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  RoleRepository repository;
  
  @Autowired
  RightRepository rightRepository;

  RoleRepository getRepository() {
    return this.repository;
  }

  private Role[] roles;

  @Override
  Role generateInstance() {
    Right right = new RightDataBuilder()
        .withType(RightType.GENERAL_ADMIN)
        .buildAsNew();
    rightRepository.save(right);
    return new RoleDataBuilder().withRights(right).buildAsNew();
  }

  @Before
  public void setUp() {
    roles = IntStream
        .range(0, 10)
        .mapToObj(idx -> generateInstance())
        .peek(repository::save)
        .toArray(Role[]::new);
  }

  private Role generateRoleInstance(String name) {
    return new RoleDataBuilder()
            .withName(name)
            .withoutId()
            .buildAsNew();
  }

  @Test
  public void shouldFindAllRolesIfNoParamsWereSet() {
    // given
    TestSearchParams params = new TestSearchParams();

    // when
    List<Role> found = repository.search(params);

    // then
    assertThat(found)
        .contains(roles);
  }

  @Test
  public void shouldFindAllRolesForGivenRightIds() {
    // given
    Set<UUID> rightIds = Stream
        .of(roles[0], roles[4], roles[7])
        .map(Role::getRights)
        .flatMap(Collection::stream)
        .map(BaseEntity::getId)
        .collect(Collectors.toSet());
    TestSearchParams params = new TestSearchParams(rightIds);

    // when
    List<Role> found = repository.search(params);

    // then
    assertThat(found)
        .hasSize(rightIds.size())
        .contains(roles[0], roles[4], roles[7]);
  }

  @Test(expected = PersistenceException.class)
  public void shouldNotAllowDuplicates() {

    Role role1 = generateRoleInstance("test-role-namE");
    Role role2 = generateRoleInstance("test-role-name");
    repository.save(role1);
    repository.save(role2);

    entityManager.flush();
  }

  @Getter
  private static final class TestSearchParams implements RoleRepositoryCustom.SearchParams {
    private Set<UUID> rightIds;

    TestSearchParams() {
      this(Collections.emptySet());
    }

    TestSearchParams(Set<UUID> rightIds) {
      this.rightIds = Optional
          .ofNullable(rightIds)
          .orElse(Collections.emptySet());
    }
  }
}

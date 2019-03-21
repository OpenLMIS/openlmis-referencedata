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

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.BaseEntity;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.repository.custom.RoleRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Allow testing roleRightsRepository.
 */

public class RoleRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Role> {

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
    int instanceNumber = this.getNextInstanceNumber();
    Right right = Right.newRight(valueOf(instanceNumber), RightType.GENERAL_ADMIN);
    rightRepository.save(right);
    return Role.newRole(valueOf(instanceNumber), right);
  }

  @Before
  public void setUp() {
    roles = IntStream
        .range(0, 10)
        .mapToObj(idx -> generateInstance())
        .peek(repository::save)
        .toArray(Role[]::new);
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

  @Test
  public void shouldCheckIfRoleExistByName() {
    assertFalse(repository.existsByName("some-random-name"));
    assertTrue(repository.existsByName(roles[0].getName()));
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

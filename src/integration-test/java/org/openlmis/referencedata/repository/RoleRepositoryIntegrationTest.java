package org.openlmis.referencedata.repository;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.exception.RightTypeException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Allow testing roleRightsRepository.
 */

public class RoleRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Role> {

  @Autowired
  RoleRepository repository;

  RoleRepository getRepository() {
    return this.repository;
  }

  @Override
  Role generateInstance() throws RightTypeException {
    int instanceNumber = this.getNextInstanceNumber();
    return new Role(valueOf(instanceNumber), new Right(valueOf(instanceNumber),
        RightType.GENERAL_ADMIN));
  }

  @Test
  public void shouldGetFirstByNameIfExists() throws RightTypeException {
    //given
    Role role1 = this.generateInstance();
    Role role2 = this.generateInstance();
    repository.save(role1);
    repository.save(role2);
    String nameToFind = role2.getName();

    //when
    Role foundRole = repository.findFirstByName(nameToFind);

    //then
    assertEquals(role2, foundRole);
  }

  @Test
  public void shouldNotGetFirstByNameIfDoesNotExist() throws RightTypeException {
    //given
    String nameToFind = "does not exist";

    //when
    Role foundRole = repository.findFirstByName(nameToFind);

    //then
    assertNull(foundRole);
  }
}

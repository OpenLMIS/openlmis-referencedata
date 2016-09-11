package org.openlmis.referencedata.repository;

import static java.lang.String.valueOf;

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
    return Role.newRole(valueOf(instanceNumber), Right.newRight(valueOf(instanceNumber),
        RightType.GENERAL_ADMIN));
  }
}

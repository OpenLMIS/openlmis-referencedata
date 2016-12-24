package org.openlmis.referencedata.repository;

import static java.lang.String.valueOf;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.exception.RoleException;
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

  @Override
  Role generateInstance() throws RoleException {
    int instanceNumber = this.getNextInstanceNumber();
    Right right = Right.newRight(valueOf(instanceNumber), RightType.GENERAL_ADMIN);
    rightRepository.save(right);
    return Role.newRole(valueOf(instanceNumber), right);
  }
}

package org.openlmis.referencedata.repository;

import static java.lang.String.valueOf;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.springframework.beans.factory.annotation.Autowired;

public class RightRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Right> {

  @Autowired
  private RightRepository repository;

  @Override
  RightRepository getRepository() {
    return this.repository;
  }

  @Override
  Right generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return new Right(valueOf(instanceNumber), RightType.GENERAL_ADMIN);
  }
}

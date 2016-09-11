package org.openlmis.referencedata.repository;

import static java.lang.String.valueOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.exception.RightTypeException;
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
    return Right.newRight(valueOf(instanceNumber), RightType.GENERAL_ADMIN);
  }

  @Test
  public void shouldGetFirstByNameIfExists() throws RightTypeException {
    //given
    Right right1 = this.generateInstance();
    Right right2 = this.generateInstance();
    repository.save(right1);
    repository.save(right2);
    String nameToFind = right2.getName();

    //when
    Right foundRight = repository.findFirstByName(nameToFind);

    //then
    assertEquals(right2, foundRight);
  }

  @Test
  public void shouldNotGetFirstByNameIfDoesNotExist() throws RightTypeException {
    //given
    String nameToFind = "does not exist";

    //when
    Right foundRight = repository.findFirstByName(nameToFind);

    //then
    assertNull(foundRight);
  }
}

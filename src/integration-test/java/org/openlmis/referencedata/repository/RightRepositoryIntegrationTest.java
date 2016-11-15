package org.openlmis.referencedata.repository;

import static java.lang.String.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.openlmis.referencedata.exception.RightTypeException;
import org.springframework.beans.factory.annotation.Autowired;

public class RightRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Right> {

  @Autowired
  private RightRepository repository;

  private Right right1;
  private Right right2;

  @Override
  RightRepository getRepository() {
    return this.repository;
  }

  @Override
  Right generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return Right.newRight(valueOf(instanceNumber), RightType.GENERAL_ADMIN);
  }

  @Before
  public void setUp() {
    right1 = this.generateInstance();
    right2 = this.generateInstance();
    repository.save(right1);
    repository.save(right2);
  }

  @Test
  public void shouldGetFirstByNameIfExists() throws RightTypeException {
    //given
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

  @Test
  public void shouldAddRightAttachments() {
    //given
    right1.attach(right2);

    //when
    right1 = repository.save(right1);

    //then
    assertThat(right1.getAttachments().size(), is(1));
    assertTrue(right1.getAttachments().contains(right2));
  }

  @Test
  public void shouldUpdateRightAttachments() {
    //given
    right1.attach(right2);
    repository.save(right1);

    Right right3 = this.generateInstance();
    repository.save(right3);

    right1.clearAttachments();
    right1.attach(right3);

    //when
    right1 = repository.save(right1);

    //then
    assertThat(right1.getAttachments().size(), is(1));
    assertFalse(right1.getAttachments().contains(right2));
    assertTrue(right1.getAttachments().contains(right3));
  }

  @Test
  public void shouldRemoveRightAttachments() {
    //given
    right1.attach(right2);
    repository.save(right1);

    right1.clearAttachments();

    //when
    right1 = repository.save(right1);

    //then
    assertThat(right1.getAttachments().size(), is(0));
    assertFalse(right1.getAttachments().contains(right2));
  }
}

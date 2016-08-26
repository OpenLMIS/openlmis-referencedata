package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlmis.referencedata.exception.RightTypeException;

import java.util.List;

public class RoleTest {

  private String roleName = "role";
  private String roleDescription = "description";
  
  private String right1Name = "right1";
  private String right2Name = "right2";

  @Test
  public void shouldCreateRoleWithDescription() throws RightTypeException {
    //given
    Right right1 = new Right(right1Name, RightType.ORDER_FULFILLMENT);
    Right right2 = new Right(right2Name, RightType.ORDER_FULFILLMENT);

    //when
    Role role = new Role(roleName, roleDescription, right1, right2);

    //then
    List<Right> rights = role.getRights();
    assertThat(rights.size(), is(2));
  }

  @Test
  public void shouldGroupRightsOfSameType() throws RightTypeException {
    //given
    Right right1 = new Right(right1Name, RightType.ORDER_FULFILLMENT);
    Right right2 = new Right(right2Name, RightType.ORDER_FULFILLMENT);

    //when
    Role role = new Role(roleName, right1, right2);

    //then
    List<Right> rights = role.getRights();
    assertThat(rights.size(), is(2));
    assertThat(rights.get(0), is(right1));
    assertThat(rights.get(1), is(right2));
  }

  @Test(expected = RightTypeException.class)
  public void shouldNotGroupRightsOfDifferentTypes() throws RightTypeException {
    //given
    Right right1 = new Right(right1Name, RightType.ORDER_FULFILLMENT);
    Right right2 = new Right(right2Name, RightType.SUPERVISION);

    //when
    new Role(roleName, right1, right2);
  }

  @Test
  public void shouldBeAbleToAddRightsOfSameTypeToExistingRole() throws RightTypeException {
    //given
    Role role = new Role(roleName, new Right(right1Name, RightType.SUPERVISION));

    //when
    Right additionalRight = new Right(right2Name, RightType.SUPERVISION);
    role.add(additionalRight);

    //then
    List<Right> rights = role.getRights();
    assertThat(rights.size(), is(2));
    assertThat(rights.get(1), is(additionalRight));
  }

  @Test(expected = RightTypeException.class)
  public void shouldNotBeAbleToAddRightsOfDifferentTypeToExistingRole() throws RightTypeException {
    //given
    Role role = new Role(roleName, new Right(right1Name, RightType.SUPERVISION));

    //when
    Right rightOfDifferentType = new Right(right2Name, RightType.ORDER_FULFILLMENT);
    role.add(rightOfDifferentType);
  }

  @Test
  public void shouldIndicateIfItContainsARight() throws RightTypeException {
    //given
    Right right1 = new Right(right1Name, RightType.SUPERVISION);
    Right right2 = new Right(right2Name, RightType.SUPERVISION);
    right1.attach(right2);

    Role role = new Role(roleName, right1);

    //when
    boolean containsRight1 = role.contains(right1);
    boolean containsRight2 = role.contains(right2);

    //then
    assertTrue(containsRight1);
    assertTrue(containsRight2);
  }
}

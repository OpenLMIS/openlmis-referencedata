package org.openlmis.referencedata.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.openlmis.referencedata.domain.RightType.ORDER_FULFILLMENT;

import org.junit.Test;
import org.openlmis.referencedata.exception.ValidationMessageException;

public class FulfillmentRoleAssignmentTest {

  private Right right;
  private Facility warehouse;
  private Facility hospital;
  private User user;
  private FulfillmentRoleAssignment fulfillmentRoleAssignment;
  private String roleName = "role";

  /**
   * Setup constructor.
   */
  public FulfillmentRoleAssignmentTest() {
    right = Right.newRight("right", ORDER_FULFILLMENT);
    warehouse = new Facility("C1");
    warehouse.setType(new FacilityType("warehouse"));
    hospital = new Facility("C2");
    hospital.setType(new FacilityType("hospital"));
    user = new UserBuilder("username", "User", "Name", "test@test.com").createUser();
    fulfillmentRoleAssignment = new FulfillmentRoleAssignment(
        Role.newRole(roleName, right), user, warehouse);
  }

  @Test
  public void shouldAllowCreationWithWarehouseFacilityType() {
    new FulfillmentRoleAssignment(Role.newRole(roleName, right), user, warehouse);
  }

  @Test(expected = ValidationMessageException.class)
  public void shouldNotAllowCreationWithNonWarehouseFacilityType() {
    new FulfillmentRoleAssignment(Role.newRole(roleName, right), user, hospital);
  }

  @Test
  public void shouldHaveRightWhenRightAndFacilityMatch() {
    //when
    RightQuery rightQuery = new RightQuery(right, warehouse);
    boolean hasRight = fulfillmentRoleAssignment.hasRight(rightQuery);

    //then
    assertTrue(hasRight);
  }

  @Test
  public void shouldNotHaveRightWhenFacilityDoesNotMatch() {
    //when
    RightQuery rightQuery = new RightQuery(right, hospital);
    boolean hasRight = fulfillmentRoleAssignment.hasRight(rightQuery);

    //then
    assertFalse(hasRight);
  }
}

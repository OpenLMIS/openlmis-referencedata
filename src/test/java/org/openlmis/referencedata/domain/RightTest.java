package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.List;

public class RightTest {
  @Test
  public void shouldHaveAttachedRightsOfSameType() {
    //given
    Right right = Right.ofType(RightType.SUPERVISION);

    //when
    Right attachment = Right.ofType(RightType.SUPERVISION);
    right.attach(attachment);

    right.attach(Right.ofType(RightType.ORDER_FULFILLMENT));

    //then
    List<Right> attachedRights = right.getAttachments();
    assertThat(attachedRights.size(), is(1));
    assertThat(attachedRights.get(0), Is.is(attachment));
  }
}

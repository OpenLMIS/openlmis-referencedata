package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.List;

public class RightTest {
  @Test
  public void shouldHaveAttachedRightsOfSameType() {
    //given
    Right right = new Right("supervisionRight1", RightType.SUPERVISION);

    //when
    Right attachment = new Right("supervisionRight2", RightType.SUPERVISION);
    right.attach(attachment);

    right.attach(new Right("fulfillmentRight1", RightType.ORDER_FULFILLMENT));

    //then
    List<Right> attachedRights = right.getAttachments();
    assertThat(attachedRights.size(), is(1));
    assertThat(attachedRights.get(0), is(attachment));
  }
}

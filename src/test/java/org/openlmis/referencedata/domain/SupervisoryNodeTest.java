package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class SupervisoryNodeTest {

  @Test
  public void shouldGetAllSupervisedFacilities() {
    //given
    SupervisoryNode supervisoryNode1 = SupervisoryNode.newSupervisoryNode(new Facility());
    RequisitionGroup requisitionGroup1 = RequisitionGroup.newRequisitionGroup(supervisoryNode1,
        null, Arrays.asList(new Facility(), new Facility()));
    supervisoryNode1.assignRequisitionGroup(requisitionGroup1);

    SupervisoryNode supervisoryNode2 = SupervisoryNode.newSupervisoryNode(new Facility());
    RequisitionGroup requisitionGroup2 = RequisitionGroup.newRequisitionGroup(supervisoryNode2,
        null, Collections.singletonList(new Facility()));
    supervisoryNode2.setRequisitionGroup(requisitionGroup2);

    supervisoryNode1.addChildNode(supervisoryNode2);

    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities();

    //then
    assertThat(facilities.size(), is(3));
  }
}

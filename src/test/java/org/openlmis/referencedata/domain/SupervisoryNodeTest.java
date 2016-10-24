package org.openlmis.referencedata.domain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;

import org.junit.Test;

import java.util.Set;

public class SupervisoryNodeTest {

  @Test
  public void shouldGetAllSupervisedFacilities() {
    //given
    SupervisoryNode supervisoryNode1 =
        SupervisoryNode.newSupervisoryNode("SN1", new Facility("C1"));
    RequisitionGroup requisitionGroup1 = new RequisitionGroup("RG1", "RGN1", supervisoryNode1);
    requisitionGroup1.setMemberFacilities(Sets.newHashSet(new Facility("C2"), new Facility("C3")));
    supervisoryNode1.setRequisitionGroup(requisitionGroup1);

    SupervisoryNode supervisoryNode2 =
        SupervisoryNode.newSupervisoryNode("SN2", new Facility("C4"));
    RequisitionGroup requisitionGroup2 = new RequisitionGroup("RG2", "RGN2", supervisoryNode2);
    requisitionGroup2.setMemberFacilities(Sets.newHashSet(new Facility("C5")));
    supervisoryNode2.setRequisitionGroup(requisitionGroup2);

    supervisoryNode2.assignParentNode(supervisoryNode1);

    //when
    Set<Facility> facilities = supervisoryNode1.getAllSupervisedFacilities();

    //then
    assertThat(facilities.size(), is(3));
  }
}

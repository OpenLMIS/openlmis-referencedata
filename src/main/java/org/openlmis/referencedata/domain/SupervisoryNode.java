package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "supervisory_nodes", schema = "referencedata")
@NoArgsConstructor
public class SupervisoryNode extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @ManyToOne
  @JoinColumn(nullable = false, name = "facilityid")
  @Getter
  @Setter
  private Facility facility;

  @JsonIdentityInfo(
      generator = ObjectIdGenerators.IntSequenceGenerator.class,
      property = "parentId")
  @ManyToOne
  @JoinColumn(name = "parentid")
  @Getter
  private SupervisoryNode parentNode;

  @JsonIdentityInfo(
      generator = ObjectIdGenerators.IntSequenceGenerator.class,
      property = "childNodesSetId")
  @OneToMany(mappedBy = "parentNode")
  @Getter
  private Set<SupervisoryNode> childNodes;

  @OneToOne(mappedBy = "supervisoryNode")
  @Getter
  @Setter
  private RequisitionGroup requisitionGroup;

  private SupervisoryNode(String code, Facility facility) {
    this.code = code;
    this.facility = facility;
    this.childNodes = new HashSet<>();
  }

  /**
   * Create a new supervisory node.
   *
   * @param facility facility associated with this supervisory node
   * @return a new SupervisoryNode
   */
  public static SupervisoryNode newSupervisoryNode(String code, Facility facility) {
    return new SupervisoryNode(code, facility);
  }

  /**
   * Assign this node's parent supervisory node. Also add this node to the parent's set of 
   * child nodes.
   *
   * @param parentNode parent supervisory node to assign.
   */
  public void assignParentNode(SupervisoryNode parentNode) {
    this.parentNode = parentNode;
    parentNode.childNodes.add(this);
  }

  /**
   * Get all facilities being supervised by this supervisory node. Note, this does not get the
   * facility attached to this supervisory node. "All supervised facilities" means all facilities
   * supervised by this node and all recursive child nodes.
   *
   * @return all supervised facilities
   */
  public Set<Facility> getAllSupervisedFacilities() {
    Set<Facility> supervisedFacilities = new HashSet<>();

    if (requisitionGroup != null && requisitionGroup.getMemberFacilities() != null) {
      supervisedFacilities.addAll(requisitionGroup.getMemberFacilities());
    }

    if (childNodes != null) {
      for (SupervisoryNode childNode : childNodes) {
        supervisedFacilities.addAll(childNode.getAllSupervisedFacilities());
      }
    }

    return supervisedFacilities;
  }

  /**
   * Copy values of attributes into new or updated SupervisoryNode.
   *
   * @param supervisoryNode SupervisoryNode with new values.
   */
  public void updateFrom(SupervisoryNode supervisoryNode) {
    this.code = supervisoryNode.getCode();
    this.name = supervisoryNode.getName();
    this.description = supervisoryNode.getDescription();
    this.facility = supervisoryNode.getFacility();
    this.parentNode = supervisoryNode.getParentNode();
    this.childNodes = supervisoryNode.getChildNodes();
  }
}

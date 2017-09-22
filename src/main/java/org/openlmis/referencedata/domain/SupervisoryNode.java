/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.referencedata.domain;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
@TypeName("SupervisoryNode")
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

  @ManyToOne
  @JoinColumn(name = "parentid")
  @Getter
  private SupervisoryNode parentNode;

  @OneToMany(mappedBy = "parentNode")
  @Getter
  @DiffIgnore
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
   * Static factory method for constructing a new supervisory node using an importer (DTO).
   *
   * @param importer the supervisory node importer (DTO)
   */
  public static SupervisoryNode newSupervisoryNode(Importer importer) {
    Facility facility = null;

    if (importer.getFacility() != null) {
      facility = Facility.newFacility(importer.getFacility());
    }

    SupervisoryNode newSupervisoryNode = new SupervisoryNode(importer.getCode(), facility);
    newSupervisoryNode.id = importer.getId();
    newSupervisoryNode.name = importer.getName();
    newSupervisoryNode.description = importer.getDescription();

    if (importer.getParentNode() != null) {
      newSupervisoryNode.parentNode = SupervisoryNode.newSupervisoryNode(importer.getParentNode());
    }

    if (importer.getRequisitionGroup() != null) {
      newSupervisoryNode.requisitionGroup =
          RequisitionGroup.newRequisitionGroup(importer.getRequisitionGroup());
    }

    if (importer.getChildNodes() != null) {
      Set<SupervisoryNode> childNodes = new HashSet<>();

      for (Importer childNodeImporter : importer.getChildNodes()) {
        childNodes.add(SupervisoryNode.newSupervisoryNode(childNodeImporter));
      }

      newSupervisoryNode.childNodes = childNodes;
    }

    return newSupervisoryNode;
  }

  /**
   * Assign this node's parent supervisory node. Also add this node to the parent's set of child
   * nodes.
   *
   * @param parentNode parent supervisory node to assign.
   */
  public void assignParentNode(SupervisoryNode parentNode) {
    this.parentNode = parentNode;
    parentNode.childNodes.add(this);
  }

  /**
   * Get all facilities being supervised by this supervisory node, by program.
   * <p/>
   * Note, this does not get the facility attached to this supervisory node. "All supervised
   * facilities" means all facilities supervised by this node and all recursive child nodes.
   *
   * @param program program to check
   * @return all supervised facilities
   */
  public Set<Facility> getAllSupervisedFacilities(Program program) {
    Set<Facility> supervisedFacilities = new HashSet<>();

    if (requisitionGroup != null && requisitionGroup.supports(program)) {
      Set<Facility> facilities = requisitionGroup
          .getMemberFacilities()
          .stream()
          .filter(member -> member.supports(program))
          .collect(Collectors.toSet());
      supervisedFacilities.addAll(facilities);
    }

    if (childNodes != null) {
      for (SupervisoryNode childNode : childNodes) {
        supervisedFacilities.addAll(childNode.getAllSupervisedFacilities(program));
      }
    }

    return supervisedFacilities;
  }

  /**
   * Check to see if this supervisory node supervises the specified facility, by program.
   */
  public boolean supervises(Facility facility, Program program) {
    return getAllSupervisedFacilities(program).contains(facility);
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
    this.requisitionGroup = supervisoryNode.getRequisitionGroup();
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setCode(code);
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setFacility(facility);
    exporter.setParentNode(parentNode);
    exporter.setChildNodes(childNodes);
    exporter.setRequisitionGroup(requisitionGroup);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupervisoryNode)) {
      return false;
    }
    SupervisoryNode that = (SupervisoryNode) obj;
    return Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code);
  }

  public interface Exporter {
    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setFacility(Facility facility);

    void setParentNode(SupervisoryNode parentNode);

    void setChildNodes(Set<SupervisoryNode> childNodes);

    void setRequisitionGroup(RequisitionGroup requisitionGroup);
  }

  public interface Importer {
    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    Facility.Importer getFacility();

    SupervisoryNode.Importer getParentNode();

    Set<SupervisoryNode.Importer> getChildNodes();

    RequisitionGroup.Importer getRequisitionGroup();
  }
}

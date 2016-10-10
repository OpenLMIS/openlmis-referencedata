package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "supply_lines", schema = "referencedata")
@NoArgsConstructor
public class SupplyLine extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "supervisoryNodeId", nullable = false)
  @Getter
  @Setter
  private SupervisoryNode supervisoryNode;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  @Setter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "supplyingFacilityId", nullable = false)
  @Getter
  @Setter
  private Facility supplyingFacility;

  /**
   * Required arguments constructor.
   */
  public SupplyLine(SupervisoryNode supervisoryNode, Program program, Facility supplyingFacility) {
    this.supervisoryNode = supervisoryNode;
    this.program = program;
    this.supplyingFacility = supplyingFacility;
  }

  /**
   * Copy values of attributes into new or updated SupplyLine.
   *
   * @param supplyLine SupplyLine with new values.
   */
  public void updateFrom(SupplyLine supplyLine) {
    this.supervisoryNode = supplyLine.getSupervisoryNode();
    this.description = supplyLine.getDescription();
    this.program = supplyLine.getProgram();
    this.supplyingFacility = supplyLine.getSupplyingFacility();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SupplyLine)) {
      return false;
    }
    SupplyLine that = (SupplyLine) obj;
    return Objects.equals(supervisoryNode, that.supervisoryNode)
        && Objects.equals(program, that.program)
        && Objects.equals(supplyingFacility, that.supplyingFacility);
  }

  @Override
  public int hashCode() {
    return Objects.hash(supervisoryNode, program, supplyingFacility);
  }
}

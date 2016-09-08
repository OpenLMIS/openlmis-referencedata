package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openlmis.referencedata.util.View;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "programs", schema = "referencedata")
@NoArgsConstructor
public class Program extends BaseEntity {

  @JsonView(View.BasicInformation.class)
  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  @Embedded
  private Code code;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String name;

  @Column(columnDefinition = "text")
  @Getter
  @Setter
  private String description;

  @Getter
  @Setter
  private Boolean active;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean periodsSkippable;

  @Getter
  @Setter
  private Boolean showNonFullSupplyTab;

  @PrePersist
  private void prePersist() {
    if (this.periodsSkippable == null) {
      this.periodsSkippable = false;
    }
  }

  /**
   * Equal by a Program's code.
   * @param other the other Program
   * @return true if the two Program's {@link Code} are equal.
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Program)) {
      return false;
    }

    Program otherProgram = (Program) other;
    return code.equals(otherProgram.code);
  }

  @Override
  public int hashCode() {
    return code.hashCode();
  }
}

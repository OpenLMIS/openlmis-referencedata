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

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import lombok.Getter;
import lombok.Setter;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "programs", schema = "referencedata")
public class Program extends BaseEntity {

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

  private Program() {
    code = null;
  }

  /**
   * Creates a new Program with given code.
   *
   * @param programCode the program code
   */
  public Program(String programCode) {
    this.code = Code.code(programCode);
  }

  @PrePersist
  private void prePersist() {
    if (this.periodsSkippable == null) {
      this.periodsSkippable = false;
    }
  }

  /**
   * Equal by a Program's code.
   *
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

  /**
   * Creates new program object based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of program.
   */
  public static Program newProgram(Importer importer) {
    Program program = new Program();
    program.setId(importer.getId());
    program.setCode(Code.code(importer.getCode()));
    program.setName(importer.getName());
    program.setDescription(importer.getDescription());
    program.setActive(importer.getActive());
    program.setPeriodsSkippable(importer.getPeriodsSkippable());
    program.setShowNonFullSupplyTab(importer.getShowNonFullSupplyTab());

    return program;
  }

  /**
   * Exports current state of program object.
   *
   * @param exporter instance of {@link Exporter}
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    String codeString = this.code.toString();
    if (isFalse(codeString.isEmpty())) {
      exporter.setCode(codeString);
    }
    exporter.setName(name);
    exporter.setDescription(description);
    exporter.setActive(active);
    exporter.setPeriodsSkippable(periodsSkippable);
    exporter.setShowNonFullSupplyTab(showNonFullSupplyTab);
  }

  public interface Exporter {

    void setId(UUID id);

    void setCode(String code);

    void setName(String name);

    void setDescription(String description);

    void setActive(Boolean active);

    void setPeriodsSkippable(Boolean periodsSkippable);

    void setShowNonFullSupplyTab(Boolean showNonFullSupplyTab);
  }

  public interface Importer {

    UUID getId();

    String getCode();

    String getName();

    String getDescription();

    Boolean getActive();

    Boolean getPeriodsSkippable();

    Boolean getShowNonFullSupplyTab();

  }
}

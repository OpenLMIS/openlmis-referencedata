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
import org.openlmis.referencedata.dto.DispensableDto;
import org.openlmis.referencedata.dto.ProgramOrderableDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Products that are Orderable by Program.  An Orderable represent any medical commodities
 * that may be ordered/requisitioned, typically by a {@link Program}.
 */
@Entity
@TypeName("Orderable")
@Table(name = "orderables", schema = "referencedata",
    uniqueConstraints = @UniqueConstraint(name = "unq_productCode", columnNames = "code"))
@NoArgsConstructor
@AllArgsConstructor
public class Orderable extends BaseEntity {

  @Embedded
  @Getter
  private Code productCode;

  @Embedded
  @Getter(AccessLevel.PACKAGE)
  private Dispensable dispensable;

  @Getter
  private String fullProductName;

  @Getter
  private String description;

  @Getter(AccessLevel.PACKAGE)
  private long netContent;

  @Getter(AccessLevel.PACKAGE)
  private long packRoundingThreshold;

  @Getter(AccessLevel.PACKAGE)
  private boolean roundToZero;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  @DiffIgnore
  @Setter
  private Set<ProgramOrderable> programOrderables;

  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "key")
  @Column(name = "value")
  @CollectionTable(
      name = "orderable_identifiers",
      joinColumns = @JoinColumn(name = "orderableId"))
  private Map<String, String> identifiers;

  @Column(name = "extradata", columnDefinition = "jsonb")
  @Convert(converter = ExtraDataConverter.class)
  private Map<String, String> extraData;

  /**
   * Get the association to a {@link Program}.
   * @param program the Program this product is (maybe) in.
   * @return the association to the given {@link Program}, or null if this product is not in the
   *        given program.
   */
  public ProgramOrderable getProgramOrderable(Program program) {
    for (ProgramOrderable programOrderable : programOrderables) {
      if (programOrderable.isForProgram(program)) {
        return programOrderable;
      }
    }

    return null;
  }

  /**
   * Returns the number of packs to order. For this Orderable given a desired number of
   * dispensing units, will return the number of packs that should be ordered.
   * @param dispensingUnits # of dispensing units we'd like to order for
   * @return the number of packs that should be ordered.
   */
  public long packsToOrder(long dispensingUnits) {
    if (dispensingUnits <= 0 || netContent == 0) {
      return 0;
    }

    long packsToOrder = dispensingUnits / netContent;
    long remainderQuantity = dispensingUnits % netContent;

    if (remainderQuantity > 0 && remainderQuantity > packRoundingThreshold) {
      packsToOrder += 1;
    }

    if (packsToOrder == 0 && !roundToZero) {
      packsToOrder = 1;
    }

    return packsToOrder;
  }

  public String getIdentifier(String key) {
    return identifiers.get(key);
  }

  /**
   * Determines equality based on product codes.
   * @param object another Orderable, ideally.
   * @return true if the two are semantically equal.  False otherwise.
   */
  @Override
  public final boolean equals(Object object) {
    return null != object
        && object instanceof Orderable
        && Objects.equals(productCode, ((Orderable) object).productCode);
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(productCode);
  }

  /**
   * Creates new instance based on data from {@link Importer}
   *
   * @param importer instance of {@link Importer}
   * @return new instance of Orderable.
   */
  public static Orderable newInstance(Importer importer) {
    Orderable orderable = new Orderable();
    orderable.id = importer.getId();
    orderable.productCode = Code.code(importer.getProductCode());
    orderable.dispensable = Dispensable.newInstance(importer.getDispensable());
    orderable.fullProductName = importer.getFullProductName();
    orderable.description = importer.getDescription();
    orderable.netContent = importer.getNetContent();
    orderable.packRoundingThreshold = importer.getPackRoundingThreshold();
    orderable.roundToZero = importer.getRoundToZero();
    orderable.programOrderables = new HashSet<>();

    orderable.identifiers = importer.getIdentifiers();
    orderable.extraData = importer.getExtraData();

    return orderable;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setProductCode(productCode.toString());
    exporter.setDispensable(DispensableDto.newInstance(dispensable));
    exporter.setFullProductName(fullProductName);
    exporter.setDescription(description);
    exporter.setNetContent(netContent);
    exporter.setPackRoundingThreshold(packRoundingThreshold);
    exporter.setRoundToZero(roundToZero);
    exporter.setPrograms(ProgramOrderableDto.newInstance(programOrderables));
    exporter.setIdentifiers(identifiers);
    exporter.setExtraData(extraData);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProductCode(String productCode);

    void setDispensable(DispensableDto dispensable);

    void setFullProductName(String fullProductName);

    void setDescription(String description);

    void setNetContent(Long netContent);

    void setPackRoundingThreshold(Long packRoundingThreshold);

    void setRoundToZero(Boolean roundToZero);

    void setPrograms(Set<ProgramOrderableDto> programOrderables);

    void setIdentifiers(Map<String, String> identifiers);

    void setExtraData(Map<String, String> extraData);
  }

  public interface Importer {
    UUID getId();

    String getProductCode();

    DispensableDto getDispensable();

    String getFullProductName();

    String getDescription();

    Long getNetContent();

    Long getPackRoundingThreshold();

    Boolean getRoundToZero();

    Set<ProgramOrderableDto> getPrograms();

    Map<String, String> getIdentifiers();

    Map<String, String> getExtraData();
  }
}

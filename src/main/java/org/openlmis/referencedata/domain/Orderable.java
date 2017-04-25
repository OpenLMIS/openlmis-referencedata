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

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Products that are Orderable by Program.  An Orderable represent any medical commodities
 * that may be ordered/requisitioned, typically by a {@link Program}. An Orderable must be
 * connected to either a {@link TradeItem} (a specific item by a manufacturer/brand owner) or to
 * a {@link CommodityType} (one category of medicines or commodities).
 */
@Entity
@Table(name = "orderables", schema = "referencedata")
@NoArgsConstructor
public class Orderable extends BaseEntity {

  @Embedded
  @Getter
  private Code productCode;

  @Embedded
  @Getter(AccessLevel.PACKAGE)
  private Dispensable dispensable;

  @Getter
  private String fullProductName;

  @Getter(AccessLevel.PACKAGE)
  private long netContent;

  @Getter(AccessLevel.PACKAGE)
  private long packRoundingThreshold;

  @Getter(AccessLevel.PACKAGE)
  private boolean roundToZero;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.EAGER)
  private Set<ProgramOrderable> programOrderables;

  @Getter(AccessLevel.PACKAGE)
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "key")
  @Column(name = "value")
  @CollectionTable(
      name = "identifiers",
      joinColumns = @JoinColumn(name = "orderableId"))
  private Map<String, String> identifiers;

  /**
   * Get the association to a {@link Program}.
   * @param program the Program this product is (maybe) in.
   * @return the asssociation to the given {@link Program}, or null if this product is not in the
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

  /**
   * Determines equality based on product codes.
   * @param object another Orderable, ideally.
   * @return true if the two are semantically equal.  False otherwise.
   */
  @Override
  public final boolean equals(Object object) {
    if (null == object) {
      return false;
    }

    if (!(object instanceof Orderable)) {
      return false;
    }

    return Objects.equals(productCode, ((Orderable) object).productCode);
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
    orderable.productCode = importer.getProductCode();
    orderable.dispensable = importer.getDispensable();
    orderable.fullProductName = importer.getFullProductName();
    orderable.netContent = importer.getNetContent();
    orderable.packRoundingThreshold = importer.getPackRoundingThreshold();
    orderable.roundToZero = importer.isRoundToZero();
    orderable.programOrderables = new HashSet<>();

    if (importer.getProgramOrderables() != null) {
      importer.getProgramOrderables()
          .forEach(po -> orderable.programOrderables.add(ProgramOrderable.newInstance(po)));
    }
    orderable.identifiers = ImmutableMap.copyOf(importer.getIdentifiers());

    return orderable;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setProductCode(productCode);
    exporter.setDispensable(dispensable);
    exporter.setFullProductName(fullProductName);
    exporter.setNetContent(netContent);
    exporter.setPackRoundingThreshold(packRoundingThreshold);
    exporter.setRoundToZero(roundToZero);
    exporter.setProgramOrderables(ProgramOrderableDto.newInstance(programOrderables));
    exporter.setIdentifiers(identifiers);
  }

  public interface Exporter {
    void setId(UUID id);

    void setProductCode(Code productCode);

    void setDispensable(Dispensable dispensable);

    void setFullProductName(String fullProductName);

    void setNetContent(long netContent);

    void setPackRoundingThreshold(long packRoundingThreshold);

    void setRoundToZero(boolean roundToZero);

    void setProgramOrderables(Set<ProgramOrderableDto> programOrderables);

    void setIdentifiers(Map<String, String> identifiers);
  }

  public interface Importer {
    UUID getId();

    Code getProductCode();

    Dispensable getDispensable();

    String getFullProductName();

    long getNetContent();

    long getPackRoundingThreshold();

    boolean isRoundToZero();

    Set<ProgramOrderableDto> getProgramOrderables();

    Map<String, String> getIdentifiers();
  }

}

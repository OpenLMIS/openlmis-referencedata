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

import static org.openlmis.referencedata.domain.Orderable.DISPENSABLE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.BOOLEAN_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.CODE_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.DISPENSABLE_TYPE;
import static org.openlmis.referencedata.web.csv.processor.CsvCellProcessors.POSITIVE_LONG;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.referencedata.domain.BaseEntity.BaseExporter;
import org.openlmis.referencedata.domain.BaseEntity.BaseImporter;
import org.openlmis.referencedata.domain.ExtraDataEntity.ExtraDataExporter;
import org.openlmis.referencedata.domain.ExtraDataEntity.ExtraDataImporter;
import org.openlmis.referencedata.domain.VersionIdentity.VersionExporter;
import org.openlmis.referencedata.domain.VersionIdentity.VersionImporter;
import org.openlmis.referencedata.domain.measurement.TemperatureMeasurement;
import org.openlmis.referencedata.domain.measurement.VolumeMeasurement;
import org.openlmis.referencedata.dto.OrderableChildDto;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.OrderableIdentifierCsvModel;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.dto.UnitOfOrderableDto;
import org.openlmis.referencedata.web.csv.model.ImportField;

/**
 * Products that are Orderable by Program.  An Orderable represent any medical commodities that may
 * be ordered/requisitioned, typically by a {@link Program}.
 */
@Entity
@TypeName("Orderable")
@Table(name = "orderables", schema = "referencedata",
        uniqueConstraints = @UniqueConstraint(name = "unq_productcode_versionid",
                columnNames = {"code", "versionnumber"}))
@NoArgsConstructor
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(
        name = "graph.Orderable",
        attributeNodes = {
                @NamedAttributeNode(value = "programOrderables", subgraph = "programOrderables"),
                @NamedAttributeNode(value = DISPENSABLE, subgraph = DISPENSABLE),
                @NamedAttributeNode("children"),
                @NamedAttributeNode("identifiers")
        },
        subgraphs = {
                @NamedSubgraph(name = "programOrderables",
                        attributeNodes = @NamedAttributeNode("orderableDisplayCategory")),
                @NamedSubgraph(name = DISPENSABLE,
                        attributeNodes = @NamedAttributeNode("attributes"))
        })
@NamedNativeQueries(
        @NamedNativeQuery(name = "Orderable.findAllOrderableIdentifierCsvModels",
                query = "select oig.key, o.code \n"
                        + "from (select key , value, orderableid, max(orderableversionnumber) \n"
                        + "from referencedata.orderable_identifiers \n"
                        + "group by key, value, orderableid) oig \n"
                        + "left outer join referencedata.orderables o \n"
                        + "on oig.orderableid = o.id ",
                resultSetMapping = "Orderable.orderableIdentifierCsvModel")
)
@SqlResultSetMappings(
        @SqlResultSetMapping(
                name = "Orderable.orderableIdentifierCsvModel",
                classes = @ConstructorResult(
                        targetClass = OrderableIdentifierCsvModel.class,
                        columns = {
                                @ColumnResult(name = "key", type = String.class),
                                @ColumnResult(name = "code", type = String.class)
                        }
                )
        )
)
public class Orderable implements Versionable {

  private static final int FETCH_SIZE = 1000;

  public static final String TRADE_ITEM = "tradeItem";
  public static final String COMMODITY_TYPE = "commodityType";
  public static final String VALUE = "value";
  private static final String PRODUCT_CODE = "productCode";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String PACK_ROUNDING_THRESHOLD = "packRoundingThreshold";
  private static final String PACK_SIZE = "packSize";
  public static final String DISPENSABLE = "dispensable";
  private static final String ROUND_TO_ZERO = "roundToZero";

  @Embedded
  @Getter
  @Setter
  @ImportField(name = PRODUCT_CODE, type = CODE_TYPE, mandatory = true)
  private Code productCode;

  @Getter
  @Setter
  @ImportField(name = NAME)
  private String fullProductName;

  @Getter
  @Setter
  @ImportField(name = DESCRIPTION)
  private String description;

  @Getter
  @Setter
  @ImportField(name = PACK_ROUNDING_THRESHOLD, type = POSITIVE_LONG, mandatory = true)
  private long packRoundingThreshold;

  @Getter
  @Setter
  @ImportField(name = PACK_SIZE, type = POSITIVE_LONG, mandatory = true)
  private long netContent;

  @Getter
  @Setter
  @ImportField(name = ROUND_TO_ZERO, type = BOOLEAN_TYPE, mandatory = true)
  private boolean roundToZero;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "dispensableid", nullable = false)
  @DiffIgnore // same reason as one in Facility.supportedPrograms
  @Getter
  @Setter
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  @ImportField(name = DISPENSABLE, type = DISPENSABLE_TYPE, mandatory = true)
  private Dispensable dispensable;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = FETCH_SIZE)
  @DiffIgnore
  @Setter
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  private List<ProgramOrderable> programOrderables;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = FETCH_SIZE)
  @DiffIgnore
  @Setter
  @Getter
  @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
  private Set<OrderableChild> children;

  @ElementCollection
  @MapKeyColumn(name = "key")
  @BatchSize(size = FETCH_SIZE)
  @Column(name = VALUE)
  @CollectionTable(
          name = "orderable_identifiers",
          joinColumns = {
                  @JoinColumn(name = "orderableId", referencedColumnName = "id"),
                  @JoinColumn(name = "orderableVersionNumber",
                          referencedColumnName = "versionNumber")})
  @Setter
  @Getter
  private Map<String, String> identifiers;

  @Embedded
  private ExtraDataEntity extraData = new ExtraDataEntity();

  @EmbeddedId
  private VersionIdentity identity;

  @Getter
  @Setter
  private ZonedDateTime lastUpdated;

  @Getter
  @Setter
  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = VALUE, column = @Column(
                  name = "minimumTemperatureValue")),
          @AttributeOverride(name = "temperatureMeasurementUnitCode", column = @Column(
                  name = "minimumTemperatureCode"))
  })
  private TemperatureMeasurement minimumTemperature;

  @Getter
  @Setter
  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = VALUE, column = @Column(
                  name = "maximumTemperatureValue")),
          @AttributeOverride(name = "temperatureMeasurementUnitCode", column = @Column(
                  name = "maximumTemperatureCode"))
  })
  private TemperatureMeasurement maximumTemperature;

  @Getter
  @Setter
  @Embedded
  @AttributeOverrides({
          @AttributeOverride(name = VALUE, column = @Column(
                  name = "inBoxCubeDimensionValue")),
          @AttributeOverride(name = "measurementUnitCode", column = @Column(
                  name = "inBoxCubeDimensionCode"))
  })
  private VolumeMeasurement inBoxCubeDimension;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "orderable_units_assignment",
      joinColumns = {
        @JoinColumn(name = "orderableId", referencedColumnName = "id", nullable = false),
        @JoinColumn(
            name = "orderableVersionNumber",
            referencedColumnName = "versionNumber",
            nullable = false),
      },
      inverseJoinColumns =
          @JoinColumn(name = "unitoforderableid", referencedColumnName = "id", nullable = false))
  @OrderBy("displayOrder ASC")
  @Getter
  @Setter
  @DiffIgnore
  private List<UnitOfOrderable> units;

  /**
   * Default constructor.
   *
   * @param productCode           product code
   * @param dispensable           dispensable
   * @param netContent            net content
   * @param packRoundingThreshold pack rounding threshold
   * @param roundToZero           round to zero
   * @param id                    id
   * @param versionNumber         version number
   */
  public Orderable(Code productCode, Dispensable dispensable, long netContent,
                   long packRoundingThreshold, boolean roundToZero, UUID id, Long versionNumber) {
    this.productCode = productCode;
    this.dispensable = dispensable;
    this.netContent = netContent;
    this.packRoundingThreshold = packRoundingThreshold;
    this.roundToZero = roundToZero;
    this.identity = new VersionIdentity(id, versionNumber);
    this.lastUpdated = ZonedDateTime.now();
  }

  /**
   * Creates a new instance of orderable with an updated version from importer.
   *
   * @param persistedOrderable persisted orderable.
   * @param importer           importer.
   * @return a new Orderable.
   */
  public static Orderable updateFrom(Orderable persistedOrderable, Importer importer) {
    Orderable orderable = newInstance(importer);
    orderable.identity = new VersionIdentity(persistedOrderable.getId(),
            persistedOrderable.getVersionNumber() + 1);
    return orderable;
  }

  /**
   * Creates new instance based on data from {@link Importer}.
   *
   * @param importer instance of {@link Importer}
   * @return new instance of Orderable.
   */
  public static Orderable newInstance(Importer importer) {
    Orderable orderable = new Orderable();
    orderable.productCode = Code.code(importer.getProductCode());
    orderable.dispensable = Dispensable.createNew(importer.getDispensable());
    orderable.fullProductName = importer.getFullProductName();
    orderable.description = importer.getDescription();
    orderable.netContent = importer.getNetContent();
    orderable.packRoundingThreshold = importer.getPackRoundingThreshold();
    orderable.roundToZero = importer.getRoundToZero();
    orderable.programOrderables = new ArrayList<>();
    orderable.children = new HashSet<>();
    orderable.identifiers = importer.getIdentifiers();
    orderable.units = UnitOfOrderable.newInstances(importer.getUnits());

    orderable.extraData = ExtraDataEntity.defaultEntity(orderable.extraData);
    orderable.extraData.updateFrom(importer.getExtraData());

    orderable.identity = new VersionIdentity(importer.getId(), importer.getVersionNumber());
    orderable.lastUpdated = ZonedDateTime.now();
    if (importer.getMinimumTemperature() != null) {
      orderable.minimumTemperature = TemperatureMeasurement
              .newTemperatureMeasurement(importer.getMinimumTemperature());
    }
    if (importer.getMaximumTemperature() != null) {
      orderable.maximumTemperature = TemperatureMeasurement
              .newTemperatureMeasurement(importer.getMaximumTemperature());
    }
    if (importer.getInBoxCubeDimension() != null) {
      orderable.inBoxCubeDimension = VolumeMeasurement
              .newVolumeMeasurement(importer.getInBoxCubeDimension());
    }

    return orderable;
  }

  @PrePersist
  @PreUpdate
  public void updateLastUpdatedDate() {
    lastUpdated = ZonedDateTime.now();
  }

  @Override
  public UUID getId() {
    return identity.getId();
  }

  public void setId(UUID id) {
    identity.setId(id);
  }

  @Override
  public Long getVersionNumber() {
    return identity.getVersionNumber();
  }

  /**
   * Get the association to a {@link Program}.
   *
   * @param program the Program this product is (maybe) in.
   * @return the association to the given {@link Program}, or null if this product is not in the
   *     given program or is marked inactive.
   */
  public ProgramOrderable getProgramOrderable(Program program) {
    for (ProgramOrderable programOrderable : programOrderables) {
      if (programOrderable.isForProgram(program) && programOrderable.isActive()) {
        return programOrderable;
      }
    }

    return null;
  }

  /**
   * Returns the number of packs to order. For this Orderable given a desired number of dispensing
   * units, will return the number of packs that should be ordered.
   *
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

  public String getTradeItemIdentifier() {
    return identifiers.get(TRADE_ITEM);
  }

  public String getCommodityTypeIdentifier() {
    return identifiers.get(COMMODITY_TYPE);
  }

  public boolean hasDispensable(Dispensable dispensable) {
    return this.dispensable.equals(dispensable);
  }

  /**
   * Checks whether this resource was modified since the provided date. If date is null,
   * this method will always return true.
   *
   * @param date date to check against, can be null
   * @return true if resource was modified since the provided date, false otherwise
   */
  public boolean wasModifiedSince(ZonedDateTime date) {
    return date == null || lastUpdated.isAfter(date);
  }

  /**
   * Determines equality based on product codes.
   *
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
   * This method checks whether fields imported from csv are the same as in object.
   */
  public static boolean isEqualForCsvFields(OrderableDto dto, Orderable orderable) {
    if (dto == null || orderable == null) {
      return false;
    }

    return Objects.equals(dto.getProductCode(), orderable.getProductCode().toString())
        && Objects.equals(dto.getFullProductName(), orderable.getFullProductName())
        && Objects.equals(dto.getDescription(), orderable.getDescription())
        && Objects.equals(dto.getPackRoundingThreshold(), orderable.getPackRoundingThreshold())
        && Objects.equals(dto.getNetContent(), orderable.getNetContent())
        && Objects.equals(dto.getRoundToZero(), orderable.isRoundToZero())
        && Objects.equals(Dispensable.createNew(dto.getDispensable()), orderable.getDispensable());
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(identity.getId());
    exporter.setProductCode(productCode.toString());
    exporter.setDispensable(dispensable);
    exporter.setFullProductName(fullProductName);
    exporter.setDescription(description);
    exporter.setNetContent(netContent);
    exporter.setPackRoundingThreshold(packRoundingThreshold);
    exporter.setRoundToZero(roundToZero);
    exporter.setPrograms(ProgramOrderableDto.newInstance(programOrderables));
    exporter.setChildren(OrderableChildDto.newInstance(children));
    exporter.setIdentifiers(identifiers);
    exporter.setUnits(UnitOfOrderableDto.newInstances(units));

    extraData = ExtraDataEntity.defaultEntity(extraData);
    extraData.export(exporter);

    exporter.setVersionNumber(identity.getVersionNumber());
    exporter.setLastUpdated(lastUpdated);
    if (minimumTemperature != null) {
      exporter.setMinimumTemperature(minimumTemperature);
    }
    if (maximumTemperature != null) {
      exporter.setMaximumTemperature(maximumTemperature);
    }
    if (inBoxCubeDimension != null) {
      exporter.setInBoxCubeDimension(inBoxCubeDimension);
    }
  }

  public void setExtraData(Map<String, Object> extraData) {
    this.extraData = ExtraDataEntity.defaultEntity(this.extraData);
    this.extraData.updateFrom(extraData);
  }

  public interface Exporter extends BaseExporter, ExtraDataExporter, VersionExporter {

    void setProductCode(String productCode);

    void setDispensable(Dispensable dispensable);

    void setFullProductName(String fullProductName);

    void setDescription(String description);

    void setNetContent(Long netContent);

    void setPackRoundingThreshold(Long packRoundingThreshold);

    void setRoundToZero(Boolean roundToZero);

    void setPrograms(Set<ProgramOrderableDto> programOrderables);

    void setChildren(Set<OrderableChildDto> children);

    void setIdentifiers(Map<String, String> identifiers);

    void setLastUpdated(ZonedDateTime lastUpdated);

    void setMinimumTemperature(TemperatureMeasurement minimumTemperature);

    void setMaximumTemperature(TemperatureMeasurement maximumTemperature);

    void setInBoxCubeDimension(VolumeMeasurement inBoxCubeDimension);

    void setUnits(List<UnitOfOrderableDto> units);
  }

  public interface Importer extends BaseImporter, ExtraDataImporter, VersionImporter {

    String getProductCode();

    Dispensable.Importer getDispensable();

    String getFullProductName();

    String getDescription();

    Long getNetContent();

    Long getPackRoundingThreshold();

    Boolean getRoundToZero();

    Set<ProgramOrderableDto> getPrograms();

    Set<OrderableChildDto> getChildren();

    Map<String, String> getIdentifiers();

    TemperatureMeasurement.Importer getMinimumTemperature();

    TemperatureMeasurement.Importer getMaximumTemperature();

    VolumeMeasurement.Importer getInBoxCubeDimension();

    List<UnitOfOrderableDto> getUnits();
  }
}


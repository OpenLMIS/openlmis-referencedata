package org.openlmis.referencedata.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products", schema = "referencedata")
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {

  @Column(nullable = false, unique = true, columnDefinition = "text")
  @Getter
  @Setter
  private String code;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String primaryName;

  @Column(nullable = false, columnDefinition = "text")
  @Getter
  @Setter
  private String dispensingUnit;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer dosesPerDispensingUnit;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer packSize;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer packRoundingThreshold;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean roundToZero;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean active;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean fullSupply;

  @Column(nullable = false)
  @Getter
  @Setter
  private Boolean tracer;

  @ManyToOne
  @JoinColumn(name = "productCategoryId", nullable = false)
  @Getter
  @Setter
  private ProductCategory productCategory;
}
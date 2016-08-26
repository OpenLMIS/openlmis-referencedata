package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "program_products", schema = "referencedata")
@NoArgsConstructor
public class ProgramProduct extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "programId", nullable = false)
  @Getter
  @Setter
  private Program program;

  @ManyToOne
  @JoinColumn(name = "productId", nullable = false)
  @Getter
  @Setter
  private OrderableProduct product;

  @Column(nullable = false)
  @Getter
  @Setter
  private Integer dosesPerMonth;

  @Column(nullable = false)
  @Getter
  @Setter
  private boolean active;

  @ManyToOne
  @JoinColumn(name = "productCategoryId", nullable = false)
  @Getter
  @Setter
  private ProductCategory productCategory;

  @Column(nullable = false)
  @Getter
  @Setter
  private boolean fullSupply;

  @Column
  @Getter
  @Setter
  private int displayOrder;

  @Column
  @Getter
  @Setter
  private int maxMonthsStock;
}

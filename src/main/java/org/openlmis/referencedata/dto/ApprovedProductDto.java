package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.ProgramOrderable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovedProductDto extends BaseDto implements FacilityTypeApprovedProduct.Exporter,
    FacilityTypeApprovedProduct.Importer {

  private ProgramOrderableDto product;

  private Double maxMonthsOfStock;

  private Double minMonthsOfStock;

  private Double emergencyOrderPoint;

  @Override
  public void setProduct(ProgramOrderable programOrderable) {
    this.product = new ProgramOrderableDto();
    programOrderable.export(product);
  }
}

package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.ProgramProduct;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovedProductDto extends BaseDto implements FacilityTypeApprovedProduct.Exporter,
    FacilityTypeApprovedProduct.Importer {

  private ProductDto product;

  private Double maxMonthsOfStock;

  private Double minMonthsOfStock;

  private Double emergencyOrderPoint;

  @Override
  public void setProduct(ProgramProduct programProduct) {
    this.product = new ProductDto();
    programProduct.export(product);
  }
}

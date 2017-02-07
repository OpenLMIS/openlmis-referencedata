package org.openlmis.referencedata.dto;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;
import org.openlmis.referencedata.domain.ProgramOrderable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovedProductDto extends BaseDto implements FacilityTypeApprovedProduct.Exporter,
    FacilityTypeApprovedProduct.Importer {

  private ProgramOrderableDto programOrderable;

  private Double maxPeriodsOfStock;

  private Double minPeriodsOfStock;

  private Double emergencyOrderPoint;

  @Override
  public void setProgramOrderable(ProgramOrderable programOrderable) {
    this.programOrderable = new ProgramOrderableDto();
    programOrderable.export(this.programOrderable);
  }
}

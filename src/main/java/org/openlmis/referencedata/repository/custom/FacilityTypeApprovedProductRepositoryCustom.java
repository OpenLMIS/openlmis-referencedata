package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.FacilityTypeApprovedProduct;

import java.util.Collection;
import java.util.UUID;

public interface FacilityTypeApprovedProductRepositoryCustom {

  Collection<FacilityTypeApprovedProduct> searchFullSupply(UUID facility, UUID program);

}

package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.OrderableProduct;
import org.openlmis.referencedata.domain.Program;

import java.util.List;

public interface OrderableProductRepositoryCustom {

  List<OrderableProduct> searchApprovedOrderableProductsByProgramAndFacility(
      Program program, Facility facility);

}



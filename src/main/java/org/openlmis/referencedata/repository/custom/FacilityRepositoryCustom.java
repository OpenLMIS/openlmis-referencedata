package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.Facility;

import java.util.List;

public interface FacilityRepositoryCustom {
  List<Facility> findFacilitiesWithSimilarCodeOrName(String code, String name);
}

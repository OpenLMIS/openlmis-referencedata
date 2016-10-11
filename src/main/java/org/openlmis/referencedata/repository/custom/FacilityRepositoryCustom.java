package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.Facility;

import java.util.List;

public interface FacilityRepositoryCustom {
  List<Facility> findFacilitiesByCodeOrName(String code, String name);
}

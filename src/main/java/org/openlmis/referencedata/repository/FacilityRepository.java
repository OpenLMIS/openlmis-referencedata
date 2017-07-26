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

package org.openlmis.referencedata.repository;

import com.vividsolutions.jts.geom.Polygon;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

@JaversSpringDataAuditable
public interface FacilityRepository
    extends PagingAndSortingRepository<Facility, UUID>, FacilityRepositoryCustom {

  @Override
  <S extends Facility> S save(S entity);

  @Override
  <S extends Facility> Iterable<S> save(Iterable<S> entities);

  @Query(value = "SELECT f.*"
      + " FROM referencedata.facilities f"
      + " WHERE f.extradata @> (:extraData)\\:\\:jsonb",
      nativeQuery = true
  )
  List<Facility> findByExtraData(@Param("extraData") String extraData);

  @Query(value = "SELECT f.*"
      + " FROM referencedata.facilities f"
      + " WHERE ST_Covers(:boundary, f.location)",
      nativeQuery = true
  )
  List<Facility> findByBoundary(@Param("boundary") Polygon boundary);
  
  Facility findFirstByCode(String code);

  @Query(value = "SELECT DISTINCT f.*"
      + " FROM referencedata.facilities f"
      + "   JOIN referencedata.right_assignments ria ON ria.facilityid = f.id"
      + "   JOIN referencedata.role_assignments roa ON roa.id = ria.roleassignmentid"
      + " WHERE ria.programid IS NOT NULL" 
      + "   AND roa.userid = :userId",
      nativeQuery = true)
  Set<Facility> findSupervisionFacilitiesByUser(@Param("userId") UUID userId);
}

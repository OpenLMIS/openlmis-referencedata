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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.dto.NamedResource;
import org.openlmis.referencedata.repository.custom.FacilityRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacilityRepository
    extends JpaRepository<Facility, UUID>, FacilityRepositoryCustom,
    BaseAuditableRepository<Facility, UUID> {

  @Query(value = "SELECT f.*"
      + " FROM referencedata.facilities f"
      + " WHERE ST_Covers(:boundary, f.location)",
      nativeQuery = true
  )
  List<Facility> findByBoundary(@Param("boundary") Polygon boundary);

  Facility findFirstByCode(String code);

  Optional<Facility> findByCode(String code);

  List<Facility> findAllByCodeIn(List<String> codes);

  @Query(name = "Facility.findSupervisionFacilitiesByUser",
      nativeQuery = true)
  Set<NamedResource> findSupervisionFacilitiesByUser(@Param("userId") UUID userId);

  boolean existsByCode(String code);

  Page<Facility> findByActive(Boolean active, Pageable pageable);

  @Query(value = "SELECT\n"
      + "    f.*\n"
      + "FROM\n"
      + "    referencedata.facilities f\n"
      + "WHERE\n"
      + "    id NOT IN (\n"
      + "        SELECT\n"
      + "            id\n"
      + "        FROM\n"
      + "            referencedata.facilities f\n"
      + "            INNER JOIN referencedata.jv_global_id g "
      + "ON CAST(f.id AS varchar) = SUBSTRING(g.local_id, 2, 36)\n"
      + "            INNER JOIN referencedata.jv_snapshot s  ON g.global_id_pk = s.global_id_fk\n"
      + "    )\n"
      + " ",
      nativeQuery = true)
  Page<Facility> findAllWithoutSnapshots(Pageable pageable);
}

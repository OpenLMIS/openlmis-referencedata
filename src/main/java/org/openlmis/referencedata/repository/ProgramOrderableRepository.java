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

import java.util.UUID;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramOrderableRepository extends JpaRepository<ProgramOrderable, UUID> {

  @Query(value = "SELECT po.* FROM referencedata.program_orderables po\n"
      + "JOIN referencedata.orderables o ON o.id = po.orderableid \n"
      + "JOIN referencedata.orderable_display_categories odc "
      + "ON odc.id = po.orderabledisplaycategoryid \n"
      + "JOIN referencedata.programs p ON p.id = po.programid \n"
      + "WHERE p.code = :program_code AND o.code = :orderable_code AND odc.code = :category_code\n"
      + "ORDER BY po.orderableversionnumber, po.active DESC FETCH FIRST 1 ROWS ONLY",
      nativeQuery = true)
  ProgramOrderable findByProgramCodeOrderableCodeCategoryCode(
      @Param("program_code") String programCode,
      @Param("orderable_code") String orderableCode,
      @Param("category_code") String categoryCode);

}

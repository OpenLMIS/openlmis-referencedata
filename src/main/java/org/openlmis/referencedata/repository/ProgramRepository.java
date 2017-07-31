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

import java.util.Set;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.custom.ProgramRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.UUID;
import org.springframework.data.repository.query.Param;

@JaversSpringDataAuditable
public interface ProgramRepository
    extends PagingAndSortingRepository<Program, UUID>, ProgramRepositoryCustom {
  // Add custom Program related members here. See UserRepository.java for examples.

  @Override
  <S extends Program> S save(S entity);

  @Override
  <S extends Program> Iterable<S> save(Iterable<S> entities);

  <S extends Program> S findByCode(Code code);
  
  @Query(value = "SELECT DISTINCT p.*"
      + " FROM referencedata.programs p"
      + "   JOIN referencedata.right_assignments ra ON ra.programid = p.id"
      + " WHERE ra.userid = :userId",
      nativeQuery = true)
  Set<Program> findSupervisionProgramsByUser(@Param("userId") UUID userId);
}

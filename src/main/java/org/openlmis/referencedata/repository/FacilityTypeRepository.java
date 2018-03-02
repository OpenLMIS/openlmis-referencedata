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

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.openlmis.referencedata.domain.FacilityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.UUID;

@JaversSpringDataAuditable
public interface FacilityTypeRepository extends PagingAndSortingRepository<FacilityType, UUID> {

  @Override
  <S extends FacilityType> S save(S entity);

  @Override
  <S extends FacilityType> Iterable<S> save(Iterable<S> entities);

  FacilityType findOneByCode(@Param("code") String code);

  Page<FacilityType> findByIdIn(Collection<UUID> id, Pageable pageable);

  boolean existsByCode(@Param("code") String code);
}

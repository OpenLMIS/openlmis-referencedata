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

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserRepository extends
    PagingAndSortingRepository<User, UUID>,
    UserRepositoryCustom {

  @Override
  <S extends User> S save(S entity);

  @Override
  <S extends User> Iterable<S> save(Iterable<S> entities);

  User findOneByUsername(@Param("username") String username);

  User findOneByEmail(@Param("email") String email);

  @Query(value = "SELECT u.*"
      + " FROM referencedata.users u"
      + " WHERE u.extradata @> (:extraData)\\:\\:jsonb",
      nativeQuery = true
  )
  List<User> findByExtraData(@Param("extraData") String extraData);

  @Query(value = "SELECT DISTINCT u.*" 
      + " FROM referencedata.users u" 
      + "   JOIN referencedata.role_assignments ra ON ra.userid = u.id" 
      + "   JOIN referencedata.roles r ON r.id = ra.roleid" 
      + "   JOIN referencedata.role_rights rr ON rr.roleid = r.id"
      + " WHERE rr.rightid = :right" 
      + "   AND ra.supervisorynodeid = :supervisoryNode" 
      + "   AND ra.programid = :program",
      nativeQuery = true)
  Set<User> findSupervisingUsersBy(@Param("right") Right right,
      @Param("supervisoryNode") SupervisoryNode supervisoryNode,
      @Param("program") Program program);
}

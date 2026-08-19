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

package org.openlmis.referencedata.repository.custom.impl;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.dto.UserRoleAssignmentDto;
import org.openlmis.referencedata.repository.custom.RoleAssignmentRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class RoleAssignmentRepositoryImpl implements RoleAssignmentRepositoryCustom {

  private static final String SELECT_QUERY = "SELECT ra.userid"
      + "   , ra.roleid"
      + "   , ra.programid"
      + "   , ra.supervisorynodeid"
      + "   , ra.warehouseid"
      + " FROM referencedata.role_assignments ra"
      + " ORDER BY ra.userid, ra.id";

  private static final String COUNT_QUERY =
      "SELECT COUNT(*) FROM referencedata.role_assignments";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Retrieves a page of all role assignments, each one tagged with the user it belongs to.
   *
   * @param pageable pagination parameters
   * @return page of role assignments
   */
  @SuppressWarnings("unchecked")
  @Override
  public Page<UserRoleAssignmentDto> findAllWithUser(Pageable pageable) {
    Number count = (Number) entityManager.createNativeQuery(COUNT_QUERY).getSingleResult();

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    List<UserRoleAssignmentDto> result = entityManager
        .createNativeQuery(SELECT_QUERY, "RoleAssignment.userIdResource")
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();

    return Pagination.getPage(result, pageable, count.longValue());
  }
}

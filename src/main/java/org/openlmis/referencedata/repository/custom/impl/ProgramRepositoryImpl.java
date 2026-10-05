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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.tuple.Pair;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.custom.ProgramRepositoryCustom;
import org.openlmis.referencedata.util.Pagination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.EscapeCharacter;

public class ProgramRepositoryImpl implements ProgramRepositoryCustom {

  private static final String CODE = "code";
  private static final String NAME = "name";

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * This method is supposed to retrieve all Programs with programName similar to name parameter.
   * Method is ignoring case for program name.
   * To find all wanted Programs we use criteria query and like operator.
   *
   * @param name Part of wanted program name.
   * @return List of Programs with wanted name.
   */
  public List<Program> findProgramsByName(String name) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Program> query = builder.createQuery(Program.class);
    Root<Program> root = query.from(Program.class);
    Predicate predicate = builder.conjunction();

    if (name != null) {
      predicate = builder.and(
          predicate,
          builder.like(
                  builder.upper(root.get("name")), "%" + name.toUpperCase() + "%"));
    }
    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }

  @Override
  public Page<Program> search(String code, String name, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    Root<Program> countRoot = countQuery.from(Program.class);
    countQuery.select(builder.count(countRoot))
        .where(searchPredicate(builder, countRoot, code, name));
    Long count = entityManager.createQuery(countQuery).getSingleResult();

    CriteriaQuery<Program> query = builder.createQuery(Program.class);
    Root<Program> root = query.from(Program.class);
    query.where(searchPredicate(builder, root, code, name))
        .orderBy(searchOrder(builder, root, pageable.getSort()));

    Pair<Integer, Integer> maxAndFirst = PageableUtil.querysMaxAndFirstResult(pageable);
    List<Program> programs = entityManager.createQuery(query)
        .setMaxResults(maxAndFirst.getLeft())
        .setFirstResult(maxAndFirst.getRight())
        .getResultList();
    return Pagination.getPage(programs, pageable, count);
  }

  private Predicate searchPredicate(CriteriaBuilder builder, Root<Program> root, String code,
      String name) {
    Predicate predicate = builder.conjunction();
    if (code != null) {
      predicate = builder.and(predicate, contains(builder, codeOf(root), code));
    }
    if (name != null) {
      predicate = builder.and(predicate, contains(builder, root.get(NAME), name));
    }
    return predicate;
  }

  private Predicate contains(CriteriaBuilder builder, Expression<String> field, String part) {
    EscapeCharacter escape = EscapeCharacter.DEFAULT;
    return builder.like(builder.upper(field), "%" + escape.escape(part.toUpperCase()) + "%",
        escape.getEscapeCharacter());
  }

  private List<Order> searchOrder(CriteriaBuilder builder, Root<Program> root, Sort sort) {
    List<Order> orders = new ArrayList<>();
    for (Sort.Order order : sort) {
      Expression<?> field = CODE.equals(order.getProperty())
          ? codeOf(root)
          : root.get(order.getProperty());
      orders.add(order.isAscending() ? builder.asc(field) : builder.desc(field));
    }
    orders.add(builder.asc(root.get("id")));
    return orders;
  }

  private Expression<String> codeOf(Root<Program> root) {
    return root.get(CODE).get(CODE);
  }
}

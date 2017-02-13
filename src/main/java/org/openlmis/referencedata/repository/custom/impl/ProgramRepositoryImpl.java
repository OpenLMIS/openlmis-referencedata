package org.openlmis.referencedata.repository.custom.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.custom.ProgramRepositoryCustom;

import java.util.List;

public class ProgramRepositoryImpl implements ProgramRepositoryCustom {

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
}

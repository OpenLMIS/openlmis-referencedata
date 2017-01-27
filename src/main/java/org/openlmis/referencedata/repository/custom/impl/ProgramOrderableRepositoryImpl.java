package org.openlmis.referencedata.repository.custom.impl;

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.custom.ProgramOrderableRepositoryCustom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class ProgramOrderableRepositoryImpl implements ProgramOrderableRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Finds ProgramOrderables matching all of provided parameters.
   * @param program program of searched ProgramOrderables.
   * @return list of all ProgramOrderables matching all of provided parameters.
   */
  public List<ProgramOrderable> searchProgramOrderables(Program program) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<ProgramOrderable> query = builder.createQuery(ProgramOrderable.class);
    Root<ProgramOrderable> root = query.from(ProgramOrderable.class);
    Predicate predicate = builder.conjunction();

    if (program != null) {
      predicate = builder.and(
              predicate,
              builder.equal(
                      root.get("program"), program));
    }
    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}

package org.openlmis.referencedata.repository.custom.impl;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.UserRepositoryCustom;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class UserRepositoryImpl implements UserRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Method returns all users with matched parameters. If all parameters are null, returns an 
   * empty list.
   *
   * @param username        username of user.
   * @param firstName       firstName of user.
   * @param lastName        lastName of user.
   * @param homeFacility    homeFacility of user.
   * @param active          is the account activated.
   * @param verified        is the account verified.
   * @param loginRestricted is the account login restricted.
   * @return List of users
   */
  public List<User> searchUsers(String username, String firstName, String lastName,
                                Facility homeFacility, Boolean active, Boolean verified,
                                Boolean loginRestricted) {

    if (username == null && firstName == null && lastName == null && homeFacility == null
        && active == null && verified == null && loginRestricted == null) {
      return new ArrayList<>();
    }

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Predicate predicate = builder.conjunction();
    if (username != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("username"), username));
    }
    if (firstName != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("firstName"), firstName));
    }
    if (lastName != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("lastName"), lastName));
    }
    if (homeFacility != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("homeFacility"), homeFacility));
    }
    if (active != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("active"), active));
    }
    if (verified != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("verified"), verified));
    }
    if (loginRestricted != null) {
      predicate = builder.and(
          predicate,
          builder.equal(
              root.get("loginRestricted"), loginRestricted));
    }
    query.where(predicate);
    return entityManager.createQuery(query).getResultList();
  }
}
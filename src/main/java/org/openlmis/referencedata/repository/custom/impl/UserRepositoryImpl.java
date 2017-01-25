package org.openlmis.referencedata.repository.custom.impl;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.UserRepositoryCustom;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
    return searchUsers(username, firstName, lastName, homeFacility, active, verified,
                        loginRestricted, null);
  }

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
                                Boolean loginRestricted, Pageable pageable) {
    if (username == null && firstName == null && lastName == null && homeFacility == null
            && active == null && verified == null && loginRestricted == null) {
      return new ArrayList<>();
    }


    int pageNumber = 0;
    int pageSize = 0;
    if (pageable != null) {
      pageNumber = pageable.getPageNumber();
      pageSize = pageable.getPageSize();
    }

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> query = builder.createQuery(User.class);
    Root<User> root = query.from(User.class);
    Predicate predicate = builder.conjunction();
    predicate = addFilter(predicate, builder, root, "username", username);
    predicate = addFilter(predicate, builder, root, "firstName", firstName);
    predicate = addFilter(predicate, builder, root, "lastName", lastName);
    predicate = addFilter(predicate, builder, root, "homeFacility", homeFacility);
    predicate = addFilter(predicate, builder, root, "active", active);
    predicate = addFilter(predicate, builder, root, "verified", verified);
    predicate = addFilter(predicate, builder, root, "loginRestricted", loginRestricted);
    query.where(predicate);
    return entityManager.createQuery(query).setMaxResults(pageSize)
                        .setFirstResult(pageNumber * pageSize)
                        .getResultList();
  }


  private Predicate addFilter(Predicate predicate, CriteriaBuilder builder, Root root,
                              String filterKey, Object filterValue) {
    if (filterValue != null) {
      return builder.and(
          predicate,
          builder.equal(
              root.get(filterKey), filterValue));
    } else {
      return predicate;
    }
  }
}
package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.UserRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRepository extends
    PagingAndSortingRepository<User, UUID>,
    UserRepositoryCustom {

  @Override
  <S extends User> S save(S entity);

  @Override
  <S extends User> Iterable<S> save(Iterable<S> entities);

  User findOneByUsername(@Param("username") String username);

  @Query(value = "SELECT u.*"
      + " FROM referencedata.users u"
      + " WHERE u.extradata @> (:extraData)\\:\\:jsonb",
      nativeQuery = true
  )
  List<User> findByExtraData(@Param("extraData") String extraData);
}

package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.UserRepositoryCustom;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends
    ReferenceDataRepository<User, UUID>,
    UserRepositoryCustom {

  @Override
  <S extends User> S save(S entity);

  @Override
  <S extends User> Iterable<S> save(Iterable<S> entities);

  User findOneByUsername(@Param("username") String username);
}

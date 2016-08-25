package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Role;

import java.util.UUID;

public interface RoleRepository extends ReferenceDataRepository<Role, UUID> {
  @Override
  <S extends Role> S save(S entity);

  @Override
  <S extends Role> Iterable<S> save(Iterable<S> entities);
}

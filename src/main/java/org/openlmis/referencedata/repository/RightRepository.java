package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Right;

import java.util.UUID;

public interface RightRepository extends ReferenceDataRepository<Right, UUID> {
  @Override
  <S extends Right> S save(S entity);

  @Override
  <S extends Right> Iterable<S> save(Iterable<S> entities);
}

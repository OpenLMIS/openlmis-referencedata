package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Orderable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

/**
 * Persistence repository for saving/finding {@link Orderable}.
 */
public interface OrderableRepository extends
    PagingAndSortingRepository<Orderable, UUID> {

  @Override
  <S extends Orderable> S save(S entity);

  @Override
  <S extends Orderable> Iterable<S> save(Iterable<S> entities);

  <S extends Orderable> S findByProductCode(Code code);

}

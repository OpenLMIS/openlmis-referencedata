package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.custom.ProgramRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface ProgramRepository extends PagingAndSortingRepository<Program, UUID>, ProgramRepositoryCustom {
  // Add custom Program related members here. See UserRepository.java for examples.

  @Override
  <S extends Program> S save(S entity);

  @Override
  <S extends Program> Iterable<S> save(Iterable<S> entities);

  <S extends Program> S findByCode(Code code);
}

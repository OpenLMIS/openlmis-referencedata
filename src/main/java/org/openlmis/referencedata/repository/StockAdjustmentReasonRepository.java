package org.openlmis.referencedata.repository;

import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockAdjustmentReasonRepository
        extends PagingAndSortingRepository<StockAdjustmentReason, UUID> {

  @Override
  <S extends StockAdjustmentReason> S save(S entity);

  @Override
  <S extends StockAdjustmentReason> Iterable<S> save(Iterable<S> entities);

  @Query("SELECT r FROM StockAdjustmentReason r WHERE r.program.id = :programId")
  List<StockAdjustmentReason> findByProgramId(@Param("programId") UUID programId);
}


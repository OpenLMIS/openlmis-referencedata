package org.openlmis.referencedata.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class StockAdjustmentReasonRepositoryIntegrationTest
        extends BaseCrudRepositoryIntegrationTest<StockAdjustmentReason> {

  @Autowired
  StockAdjustmentReasonRepository repository;

  @Autowired
  ProgramRepository programRepository;

  StockAdjustmentReasonRepository getRepository() {
    return this.repository;
  }

  StockAdjustmentReason generateInstance() {
    Program program = new Program("code");
    programRepository.save(program);
    StockAdjustmentReason stockAdjustmentReason = new StockAdjustmentReason();
    stockAdjustmentReason.setName("StockAdjustmentReason");
    stockAdjustmentReason.setProgram(program);
    return stockAdjustmentReason;
  }

  @Test
  public void shouldFindByProgram() {
    StockAdjustmentReason stockAdjustmentReason = this.generateInstance();
    repository.save(stockAdjustmentReason);

    List<StockAdjustmentReason> foundStockAdjustmentReasons = repository.findByProgramId(
            stockAdjustmentReason.getProgram().getId()
    );

    assertEquals(1, foundStockAdjustmentReasons.size());
    assertEquals(stockAdjustmentReason, foundStockAdjustmentReasons.get(0));
  }

  @Test
  public void shouldNotFindByIncorrectProgram() {
    StockAdjustmentReason stockAdjustmentReason = this.generateInstance();
    repository.save(stockAdjustmentReason);

    List<StockAdjustmentReason> foundStockAdjustmentReasons = repository.findByProgramId(
            UUID.fromString("5759ee02-4a4a-4392-8994-8cb0e6bb88c0"));

    assertEquals(0, foundStockAdjustmentReasons.size());
  }
}

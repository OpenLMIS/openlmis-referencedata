package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.openlmis.referencedata.repository.StockAdjustmentReasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class StockAdjustmentReasonController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(
          StockAdjustmentReasonController.class);

  @Autowired
  private StockAdjustmentReasonRepository stockAdjustmentReasonRepository;

  /**
   * Allows creating new Stock Adjustment Reasons.
   *
   * @param stockAdjustmentReason A stockAdjustmentReason bound to the request body
   * @return ResponseEntity containing the created stockAdjustmentReason
   */
  @RequestMapping(value = "/stockAdjustmentReasons", method = RequestMethod.POST)
  public ResponseEntity<?> createStockAdjustmentReason(
          @RequestBody StockAdjustmentReason stockAdjustmentReason) {
    LOGGER.debug("Creating new stockAdjustmentReason");
    // Ignore provided id
    stockAdjustmentReason.setId(null);
    stockAdjustmentReasonRepository.save(stockAdjustmentReason);
    return new ResponseEntity<>(stockAdjustmentReason, HttpStatus.CREATED);
  }

  /**
   * Get all stockAdjustmentReasons.
   *
   * @return StockAdjustmentReasons.
   */
  @RequestMapping(value = "/stockAdjustmentReasons", method = RequestMethod.GET)
  public ResponseEntity<?> getAllStockAdjustmentReasons() {
    Iterable<StockAdjustmentReason> stockAdjustmentReasons =
            stockAdjustmentReasonRepository.findAll();
    if (stockAdjustmentReasons == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(stockAdjustmentReasons, HttpStatus.OK);
    }
  }

  /**
   * Get chosen stockAdjustmentReason.
   *
   * @param stockAdjustmentReasonId UUID of stockAdjustmentReason which we want to get
   * @return StockAdjustmentReason.
   */
  @RequestMapping(value = "/stockAdjustmentReasons/{id}", method = RequestMethod.GET)
  public ResponseEntity<?> getChosenStockAdjustmentReason(
          @PathVariable("id") UUID stockAdjustmentReasonId) {
    StockAdjustmentReason stockAdjustmentReason =
            stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId);
    if (stockAdjustmentReason == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(stockAdjustmentReason, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting stockAdjustmentReason.
   *
   * @param stockAdjustmentReasonId UUID of stockAdjustmentReason which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/stockAdjustmentReasons/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteStockAdjustmentReason(
          @PathVariable("id") UUID stockAdjustmentReasonId) {
    StockAdjustmentReason stockAdjustmentReason =
            stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId);
    if (stockAdjustmentReason == null) {
      return new ResponseEntity(HttpStatus.NOT_FOUND);
    } else {
      stockAdjustmentReasonRepository.delete(stockAdjustmentReason);
      return new ResponseEntity<StockAdjustmentReason>(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Updates stockAdjustmentReason.
   *
   * @param stockAdjustmentReason DTO class used to update stockAdjustmentReason
   */
  @RequestMapping(value = "/stockAdjustmentReasons/{id}", method = RequestMethod.PUT)
  public ResponseEntity<?> updateStockAdjustmentReason(
          @PathVariable("id") UUID stockAdjustmentReasonId,
          @RequestBody StockAdjustmentReason stockAdjustmentReason) {
    if (stockAdjustmentReason == null || stockAdjustmentReasonId == null) {
      LOGGER.debug("Update failed - stockAdjustmentReason id not specified");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    StockAdjustmentReason storedStockAdjustmentReason =
            stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId);
    if (storedStockAdjustmentReason == null) {
      LOGGER.warn("Update failed - stockAdjustmentReason with id: {} not found",
              stockAdjustmentReasonId);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    stockAdjustmentReasonRepository.save(stockAdjustmentReason);

    return new ResponseEntity<>(stockAdjustmentReason, HttpStatus.OK);
  }


  /**
   * Retrieves StockAdjustmentReasons for a specified program
   *
   * @param programId The program id
   * @return List of StockAdjustmentReasons.
   */
  @RequestMapping(value = "/stockAdjustmentReasons/search", method = RequestMethod.GET)
  public ResponseEntity<?> findStockAdjustmentReasonsByName(
          @RequestParam("program") UUID programId) {
    List<StockAdjustmentReason> reasons =
            stockAdjustmentReasonRepository.findByProgramId(programId);
    return new ResponseEntity<>(reasons, HttpStatus.OK);
  }
}

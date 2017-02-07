package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.StockAdjustmentReason;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.StockAdjustmentReasonRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.StockAdjustmentReasonMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@Controller
@Transactional
public class StockAdjustmentReasonController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(
          StockAdjustmentReasonController.class);

  @Autowired
  private StockAdjustmentReasonRepository stockAdjustmentReasonRepository;

  /**
   * Allows creating new Stock Adjustment Reasons.
   *
   * @param stockAdjustmentReason A stockAdjustmentReason bound to the request body.
   * @return the created stockAdjustmentReason.
   */
  @RequestMapping(value = "/stockAdjustmentReasons", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public StockAdjustmentReason createStockAdjustmentReason(
          @RequestBody StockAdjustmentReason stockAdjustmentReason) {
    LOGGER.debug("Creating new stockAdjustmentReason");
    // Ignore provided id
    stockAdjustmentReason.setId(null);
    stockAdjustmentReasonRepository.save(stockAdjustmentReason);
    return stockAdjustmentReason;
  }

  /**
   * Get all stockAdjustmentReasons.
   *
   * @return the StockAdjustmentReasons.
   */
  @RequestMapping(value = "/stockAdjustmentReasons", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Iterable<StockAdjustmentReason> getAllStockAdjustmentReasons() {
    Iterable<StockAdjustmentReason> stockAdjustmentReasons =
            stockAdjustmentReasonRepository.findAll();
    if (stockAdjustmentReasons == null) {
      throw new NotFoundException(StockAdjustmentReasonMessageKeys.ERROR_NOT_FOUND);
    } else {
      return stockAdjustmentReasons;
    }
  }

  /**
   * Get chosen stockAdjustmentReason.
   *
   * @param stockAdjustmentReasonId UUID of stockAdjustmentReason which we want to get.
   * @return the StockAdjustmentReason.
   */
  @RequestMapping(value = "/stockAdjustmentReasons/{id}", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public StockAdjustmentReason getChosenStockAdjustmentReason(
          @PathVariable("id") UUID stockAdjustmentReasonId) {
    StockAdjustmentReason stockAdjustmentReason =
            stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId);
    if (stockAdjustmentReason == null) {
      throw new NotFoundException(StockAdjustmentReasonMessageKeys.ERROR_NOT_FOUND);
    } else {
      return stockAdjustmentReason;
    }
  }

  /**
   * Allows deleting stockAdjustmentReason.
   *
   * @param stockAdjustmentReasonId UUID of stockAdjustmentReason which we want to delete.
   */
  @RequestMapping(value = "/stockAdjustmentReasons/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ResponseBody
  public void deleteStockAdjustmentReason(
          @PathVariable("id") UUID stockAdjustmentReasonId) {
    StockAdjustmentReason stockAdjustmentReason =
            stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId);
    if (stockAdjustmentReason == null) {
      throw new NotFoundException(StockAdjustmentReasonMessageKeys.ERROR_NOT_FOUND);
    } else {
      stockAdjustmentReasonRepository.delete(stockAdjustmentReason);
    }
  }

  /**
   * Updates stockAdjustmentReason.
   *
   * @param stockAdjustmentReason DTO class used to update stockAdjustmentReason.
   */
  @RequestMapping(value = "/stockAdjustmentReasons/{id}", method = RequestMethod.PUT)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public StockAdjustmentReason updateStockAdjustmentReason(
          @PathVariable("id") UUID stockAdjustmentReasonId,
          @RequestBody StockAdjustmentReason stockAdjustmentReason) {
    if (stockAdjustmentReason == null || stockAdjustmentReasonId == null) {
      LOGGER.debug("Update failed - stockAdjustmentReason id not specified");
      throw new ValidationMessageException(StockAdjustmentReasonMessageKeys.ERROR_ID_NULL);
    }

    StockAdjustmentReason storedStockAdjustmentReason =
            stockAdjustmentReasonRepository.findOne(stockAdjustmentReasonId);
    if (storedStockAdjustmentReason == null) {
      LOGGER.warn("Update failed - stockAdjustmentReason with id: {} not found",
              stockAdjustmentReasonId);
      throw new ValidationMessageException(new Message(
          StockAdjustmentReasonMessageKeys.ERROR_NOT_FOUND_WITH_ID, stockAdjustmentReasonId));
    }

    stockAdjustmentReasonRepository.save(stockAdjustmentReason);

    return stockAdjustmentReason;
  }


  /**
   * Retrieves StockAdjustmentReasons for a specified program
   *
   * @param programId the program id.
   * @return a list of StockAdjustmentReasons.
   */
  @RequestMapping(value = "/stockAdjustmentReasons/search", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<StockAdjustmentReason> findStockAdjustmentReasonsByName(
          @RequestParam("program") UUID programId) {
    return stockAdjustmentReasonRepository.findByProgramId(programId);
  }
}

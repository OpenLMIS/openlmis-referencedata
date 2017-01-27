package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.OrderableDisplayCategory;
import org.openlmis.referencedata.exception.IntegrityViolationException;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.messagekeys.OrderableDisplayCategoryMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

@Controller
public class OrderableDisplayCategoryController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      OrderableDisplayCategoryController.class);

  @Autowired
  private OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  /**
   * Constructor for controller unit testing.
   *
   * @param repository      a OrderableDisplayCategoryRepository
   */
  public OrderableDisplayCategoryController(OrderableDisplayCategoryRepository repository) {
    this.orderableDisplayCategoryRepository = Objects.requireNonNull(repository);
  }

  /**
   * Get all orderableDisplayCategories.
   *
   * @return OrderableDisplayCategories.
   */
  @RequestMapping(value = "/orderableDisplayCategories", method = RequestMethod.GET)
  public ResponseEntity<Iterable<OrderableDisplayCategory>> getAllOrderableDisplayCategories() {
    Iterable<OrderableDisplayCategory> orderableDisplayCategories =
        orderableDisplayCategoryRepository.findAll();
    return new ResponseEntity<>(orderableDisplayCategories, HttpStatus.OK);
  }

  /**
   * Create a {@link OrderableDisplayCategory}.
   *
   * @param orderableDisplayCategory A orderableDisplayCategory bound to the request body
   * @return ResponseEntity containing the created orderableDisplayCategory with id.
   */
  @RequestMapping(value = "/orderableDisplayCategories", method = RequestMethod.POST)
  public ResponseEntity<OrderableDisplayCategory> createOrderableDisplayCategory(
      @RequestBody OrderableDisplayCategory orderableDisplayCategory) {
    OrderableDisplayCategory found =
        orderableDisplayCategoryRepository.findByCode(orderableDisplayCategory.getCode());
    if (null != found) {
      found.updateFrom(orderableDisplayCategory);
    } else {
      found = orderableDisplayCategory;
    }

    orderableDisplayCategoryRepository.save(found);
    return new ResponseEntity<>(found, HttpStatus.CREATED);
  }

  /**
   * Updates the given {@link OrderableDisplayCategory}.
   * Uses the ID given to base it's update and ignores the code given.
   *
   * @param orderableDisplayCategory   A orderableDisplayCategory bound to the request body
   * @param orderableDisplayCategoryId UUID of orderableDisplayCategory which we want to update
   * @return ResponseEntity containing the updated orderableDisplayCategory
   */
  @RequestMapping(value = "/orderableDisplayCategories/{id}", method = RequestMethod.PUT)
  public ResponseEntity<OrderableDisplayCategory> updateOrderableDisplayCategory(
      @RequestBody OrderableDisplayCategory orderableDisplayCategory,
      @PathVariable("id") UUID orderableDisplayCategoryId) {
    LOGGER.debug("Updating orderableDisplayCategory with id: " + orderableDisplayCategoryId);

    OrderableDisplayCategory orderableDisplayCategoryToUpdate =
        orderableDisplayCategoryRepository.findOne(orderableDisplayCategoryId);

    if (null == orderableDisplayCategoryToUpdate) {
      throw new ValidationMessageException(new Message(
          OrderableDisplayCategoryMessageKeys.ERROR_NOT_FOUND_WITH_ID, orderableDisplayCategoryId));
    }
    orderableDisplayCategoryToUpdate.updateFrom(orderableDisplayCategory);
    orderableDisplayCategoryRepository.save(orderableDisplayCategoryToUpdate);

    LOGGER.debug("Updated orderableDisplayCategory with id: " + orderableDisplayCategoryId);
    return new ResponseEntity<>(orderableDisplayCategoryToUpdate, HttpStatus.OK);
  }

  /**
   * Get chosen orderableDisplayCategory.
   *
   * @param orderableDisplayCategoryId UUID of orderableDisplayCategory which we want to get
   * @return OrderableDisplayCategory.
   */
  @RequestMapping(value = "/orderableDisplayCategories/{id}", method = RequestMethod.GET)
  public ResponseEntity<OrderableDisplayCategory> getOrderableDisplayCategory(
      @PathVariable("id") UUID orderableDisplayCategoryId) {
    OrderableDisplayCategory orderableDisplayCategory = orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId);
    if (orderableDisplayCategory == null) {
      throw new NotFoundException(OrderableDisplayCategoryMessageKeys.ERROR_NOT_FOUND);
    } else {
      return new ResponseEntity<>(orderableDisplayCategory, HttpStatus.OK);
    }
  }

  /**
   * Allows deleting orderableDisplayCategory.
   *
   * @param orderableDisplayCategoryId UUID of orderableDisplayCategory which we want to delete
   * @return ResponseEntity containing the HTTP Status
   */
  @RequestMapping(value = "/orderableDisplayCategories/{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteOrderableDisplayCategory(
      @PathVariable("id") UUID orderableDisplayCategoryId) {
    OrderableDisplayCategory orderableDisplayCategory = orderableDisplayCategoryRepository.findOne(
        orderableDisplayCategoryId);
    if (orderableDisplayCategory == null) {
      throw new NotFoundException(OrderableDisplayCategoryMessageKeys.ERROR_NOT_FOUND);
    } else {
      try {
        orderableDisplayCategoryRepository.delete(orderableDisplayCategory);
      } catch (DataIntegrityViolationException ex) {
        throw new IntegrityViolationException(new Message(
            OrderableDisplayCategoryMessageKeys.ERROR_DELETING_WITH_ID,
            orderableDisplayCategoryId), ex);
      }
      return new ResponseEntity(HttpStatus.NO_CONTENT);
    }
  }

  /**
   * Finds OrderableDisplayCategories matching all of provided parameters.
   *
   * @param codeParam code of orderableDisplayCategory.
   * @return ResponseEntity with list of all Product Categories matching provided parameters
   */
  @RequestMapping(value = "/orderableDisplayCategories/search", method = RequestMethod.GET)
  public ResponseEntity<Iterable<OrderableDisplayCategory>> searchOrderableDisplayCategories(
      @RequestParam(value = "code", required = false) String codeParam) {

    if (codeParam != null) {
      OrderableDisplayCategory orderableDisplayCategory = orderableDisplayCategoryRepository
          .findByCode(Code.code(codeParam));
      if (null == orderableDisplayCategory) {
        throw new NotFoundException(OrderableDisplayCategoryMessageKeys.ERROR_NOT_FOUND);
      }
      return ResponseEntity.ok(Collections.singletonList(orderableDisplayCategory));
    } else {
      Iterable<OrderableDisplayCategory> orderableDisplayCategories =
          orderableDisplayCategoryRepository.findAll();
      return ResponseEntity.ok(orderableDisplayCategories);
    }
  }
}

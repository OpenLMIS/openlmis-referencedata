package org.openlmis.referencedata.web;

import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.ProcessingPeriod;
import org.openlmis.referencedata.domain.Product;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProductRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Controller for retrieving refernce data objects.
 */
@Controller
public class ReferenceDataObjectsRetrieveController extends BaseController {

  @Autowired
  FacilityRepository facilityRepository;

  @Autowired
  ProductRepository productRepository;

  @Autowired
  ProcessingPeriodRepository processingPeriodRepository;

  @Autowired
  UserRepository userRepository;

  /**
   * Method for retrieving Facility object.
   * @param id Facility id
   * @return Response for requesting Facility by id.
   */
  @RequestMapping(value = "/getFacilityById", method = RequestMethod.GET)
  public ResponseEntity<?> getFacility(@RequestParam(value = "id") UUID id) {
    Facility foundfacility = facilityRepository.findOne(id);
    return new ResponseEntity<Facility>(foundfacility, HttpStatus.OK);
  }

  @RequestMapping(value = "/getAllFacilities", method = RequestMethod.GET)
  public ResponseEntity<?> getFacilities() {
    Iterable<Facility> facilities = facilityRepository.findAll();
    return new ResponseEntity<>(facilities, HttpStatus.OK);
  }

  @RequestMapping(value = "/getProductById", method = RequestMethod.GET)
  public ResponseEntity<?> getProduct(@RequestParam(value = "id") UUID id) {
    Product foundProduct = productRepository.findOne(id);
    return new ResponseEntity<Product>(foundProduct, HttpStatus.OK);
  }

  @RequestMapping(value = "/getAllProducts", method = RequestMethod.GET)
  public ResponseEntity<?> getProducts() {
    Iterable<Product> products = productRepository.findAll();
    return new ResponseEntity<>(products, HttpStatus.OK);
  }

  @RequestMapping(value = "/getProcessingPeriodById", method = RequestMethod.GET)
  public ResponseEntity<?> getProcessingPeriod(@RequestParam(value = "id") UUID id) {
    ProcessingPeriod processingPeriod = processingPeriodRepository.findOne(id);
    return new ResponseEntity<>(processingPeriod, HttpStatus.OK);
  }

  @RequestMapping(value = "/getAllProcessingPeriods", method = RequestMethod.GET)
  public ResponseEntity<?> getProcessingPeriods() {
    Iterable<ProcessingPeriod> processingPeriods = processingPeriodRepository.findAll();
    return new ResponseEntity<>(processingPeriods, HttpStatus.OK);
  }

  @RequestMapping(value = "/getUserById", method = RequestMethod.GET)
  public ResponseEntity<?> getUser(@RequestParam(value = "id") UUID id) {
    User foundUser = userRepository.findOne(id);
    return new ResponseEntity<>(foundUser, HttpStatus.OK);
  }

  @RequestMapping(value = "/getAllUsers", method = RequestMethod.GET)
  public ResponseEntity<?> getUsers() {
    Iterable<User> users = userRepository.findAll();
    return new ResponseEntity<>(users, HttpStatus.OK);
  }
}

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


  @RequestMapping(value = "/getFacilityById", method = RequestMethod.GET)
  public Facility getFacility(@RequestParam(value = "id") UUID id) {
    return facilityRepository.findOne(id);
  }

  @RequestMapping(value = "/getAllFacilities", method = RequestMethod.GET)
  public Iterable<Facility> getFacilities() {
    return facilityRepository.findAll();
  }

  @RequestMapping(value = "/getProductById", method = RequestMethod.GET)
  public Product getProduct(@RequestParam(value = "id") UUID id) {
    return productRepository.findOne(id);
  }

  @RequestMapping(value = "/getAllProducts", method = RequestMethod.GET)
  public Iterable<Product> getProducts() {
    return productRepository.findAll();
  }

  @RequestMapping(value = "/getProcessingPeriodById", method = RequestMethod.GET)
  public ProcessingPeriod getProcessingPeriod(@RequestParam(value = "id") UUID id) {
    return processingPeriodRepository.findOne(id);
  }

  @RequestMapping(value = "/getAllProcessingPeriods", method = RequestMethod.GET)
  public Iterable<ProcessingPeriod> getProcessingPeriods() {
    return processingPeriodRepository.findAll();
  }

  @RequestMapping(value = "/getUserById", method = RequestMethod.GET)
  public User getUser(@RequestParam(value = "id") UUID id) {
    return userRepository.findOne(id);
  }

  @RequestMapping(value = "/getAllUsers", method = RequestMethod.GET)
  public Iterable<User> getUsers() {
    return userRepository.findAll();
  }
}

package org.openlmis.referencedata.utils;

import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.FacilityOperatorRepository;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.FacilityTypeApprovedProductRepository;
import org.openlmis.referencedata.repository.FacilityTypeRepository;
import org.openlmis.referencedata.repository.GeographicLevelRepository;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.openlmis.referencedata.repository.ProcessingPeriodRepository;
import org.openlmis.referencedata.repository.ProcessingScheduleRepository;
import org.openlmis.referencedata.repository.ProductCategoryRepository;
import org.openlmis.referencedata.repository.ProgramProductRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RightRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class CleanRepositoryHelper {

  public static final UUID INITIAL_USER_ID =
      UUID.fromString("35316636-6264-6331-2d34-3933322d3462");

  @Autowired
  private ProgramProductRepository programProductRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private ProcessingPeriodRepository periodRepository;

  @Autowired
  private ProcessingScheduleRepository scheduleRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private FacilityOperatorRepository facilityOperatorRepository;

  @Autowired
  private GeographicLevelRepository geographicLevelRepository;

  @Autowired
  private GeographicZoneRepository geographicZoneRepository;

  @Autowired
  private FacilityTypeRepository facilityTypeRepository;

  @Autowired
  private FacilityTypeApprovedProductRepository facilityTypeApprovedProductRepository;

  @Autowired
  private ProductCategoryRepository productCategoryRepository;

  @Autowired
  private UserRepository userRepository;


  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private RightRepository rightRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  /**
   * Delete all entities from most of repositories.
   */
  @Transactional
  public void cleanAll() {
    facilityTypeApprovedProductRepository.deleteAll();
    programProductRepository.deleteAll();
    periodRepository.deleteAll();
    programRepository.deleteAll();
    supervisoryNodeRepository.deleteAll();
    deleteAllUsersExceptAdmin();
    productCategoryRepository.deleteAll();
    scheduleRepository.deleteAll();
    facilityRepository.deleteAll();
    facilityTypeRepository.deleteAll();
    geographicZoneRepository.deleteAll();
    facilityOperatorRepository.deleteAll();
    geographicLevelRepository.deleteAll();
    roleRepository.deleteAll();
    rightRepository.deleteAll();
  }

  private void deleteAllUsersExceptAdmin() {
    User initialUser = userRepository.findOne(INITIAL_USER_ID);
    initialUser.setHomeFacility(null);
    userRepository.save(initialUser);
    for (User user : userRepository.findAll()) {
      if (!user.getId().equals(INITIAL_USER_ID)) {
        userRepository.delete(user);
      }
    }
  }
}

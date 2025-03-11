/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.service.export;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.service.UserAuthService;
import org.openlmis.referencedata.service.UserDetailsService;
import org.openlmis.referencedata.service.UserService;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.validate.UserImportValidator;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service(UserImportPersister.USER_FILE_NAME)
public class UserImportPersister implements DataImportPersister<User, UserDto, UserDto> {

  public static final String USER_FILE_NAME = "user.csv";

  @Autowired
  private FileHelper fileHelper;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private UserAuthService userAuthService;

  @Autowired
  private UserService userService;

  @Autowired
  private UserImportRollback userImportRollback;

  @Override
  public ImportResponseDto.ImportDetails processAndPersist(InputStream dataStream,
                                                           Profiler profiler)
      throws InterruptedException {
    profiler.start("READ CSV");
    List<UserDto> importedData = fileHelper.readCsv(UserDto.class, dataStream);
    UserImportValidator.validateFileEntries(importedData);

    List<ImportResponseDto.ErrorDetails> errors = new ArrayList<>();
    List<Facility> allFacilities = facilityRepository.findAll();
    List<UserDto> successResults = new EasyBatchUtils(importExecutorService)
        .processInBatches(
            importedData,
            batch -> saveCompleteUsers(batch, importedData, allFacilities, errors)
        );

    profiler.start("RETURN");

    return new ImportResponseDto.ImportDetails(
        USER_FILE_NAME,
        importedData.size(),
        successResults.size(),
        importedData.size() - successResults.size(),
        errors
    );
  }

  private List<UserDto> saveCompleteUsers(List<UserDto> batch, List<UserDto> importedDtos,
                                          List<Facility> allFacilities,
                                          List<ImportResponseDto.ErrorDetails> errors) {
    List<UserDto> persistedUsers = userService.saveUsersFromFile(batch, allFacilities);
    List<UserDto> successfulContactDetails =
        userDetailsService.saveUsersContactDetailsFromFile(persistedUsers, importedDtos, errors);
    List<UserDto> successfulAuthDetails =
        userAuthService.saveUserAuthDetailsFromFile(successfulContactDetails, errors);

    userImportRollback.cleanupInconsistentData(persistedUsers, successfulAuthDetails);

    return successfulAuthDetails;
  }
}

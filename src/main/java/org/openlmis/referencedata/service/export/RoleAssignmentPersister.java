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

import static org.openlmis.referencedata.util.EasyBatchUtils.DEFAULT_BATCH_SIZE;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.ROLE_NOT_FOUND;
import static org.openlmis.referencedata.util.messagekeys.UserImportMessageKeys.USER_NOT_FOUND;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.DirectRoleAssignment;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.FulfillmentRoleAssignment;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.Role;
import org.openlmis.referencedata.domain.RoleAssignment;
import org.openlmis.referencedata.domain.SupervisionRoleAssignment;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.RoleAssignmentCreatorResult;
import org.openlmis.referencedata.dto.RoleAssignmentImportDto;
import org.openlmis.referencedata.dto.SaveBatchResultDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.RoleRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.util.EasyBatchUtils;
import org.openlmis.referencedata.util.FileHelper;
import org.openlmis.referencedata.util.TransactionUtils;
import org.openlmis.referencedata.validate.UserRoleImportValidator;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service(RoleAssignmentPersister.ROLE_ASSIGNMENT_FILE)
public class RoleAssignmentPersister implements
    DataImportPersister<RoleAssignment, RoleAssignmentImportDto, RoleAssignmentImportDto> {
  public static final String ROLE_ASSIGNMENT_FILE = "roleAssignment.csv";

  @Autowired
  private FileHelper fileHelper;

  @Autowired
  @Qualifier("importExecutorService")
  private ExecutorService importExecutorService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  private FacilityRepository facilityRepository;

  @Autowired
  private TransactionUtils transactionUtils;

  @Override
  public ImportResponseDto.ImportDetails processAndPersist(InputStream dataStream,
      Profiler profiler) throws InterruptedException {
    profiler.start("READ CSV");
    List<RoleAssignmentImportDto> importedData =
        fileHelper.readCsv(RoleAssignmentImportDto.class, dataStream);
    UserRoleImportValidator.validateFileEntries(importedData);

    List<ImportResponseDto.ErrorDetails> errors = new ArrayList<>();
    List<RoleAssignmentImportDto> successResults =
        new EasyBatchUtils(importExecutorService)
          .processInBatches(
              importedData,
              batch -> transactionUtils.runInOwnTransaction(() -> importBatch(batch, errors)),
              this::splitRoleItems
          );

    profiler.start("RETURN");
    return new ImportResponseDto.ImportDetails(
        ROLE_ASSIGNMENT_FILE,
        importedData.size(),
        successResults.size(),
        importedData.size() - successResults.size(),
        errors
    );
  }

  private List<List<RoleAssignmentImportDto>> splitRoleItems(
      List<RoleAssignmentImportDto> allItems) {
    Map<String, List<RoleAssignmentImportDto>> groupedItems =
        allItems.stream().collect(Collectors.groupingBy(RoleAssignmentImportDto::getUsername));

    List<List<RoleAssignmentImportDto>> result = new ArrayList<>();
    List<RoleAssignmentImportDto> currentBatch = new ArrayList<>();
    for (List<RoleAssignmentImportDto> entries : groupedItems.values()) {
      if (currentBatch.size() + entries.size() > DEFAULT_BATCH_SIZE && !currentBatch.isEmpty()) {
        result.add(currentBatch);
        currentBatch = new ArrayList<>();
      }

      currentBatch.addAll(entries);

      if (currentBatch.size() >= DEFAULT_BATCH_SIZE) {
        result.add(currentBatch);
        currentBatch = new ArrayList<>();
      }
    }

    if (!currentBatch.isEmpty()) {
      result.add(currentBatch);
    }

    return result;
  }

  private List<RoleAssignmentImportDto> importBatch(List<RoleAssignmentImportDto> batch,
      List<ImportResponseDto.ErrorDetails> errors) {
    SaveBatchResultDto<RoleAssignmentImportDto> userRolesImportResult =
        saveUserRoles(batch, errors);
    return userRolesImportResult.getSuccessfulEntries();
  }

  private SaveBatchResultDto<RoleAssignmentImportDto> saveUserRoles(
      List<RoleAssignmentImportDto> batch, List<ImportResponseDto.ErrorDetails> errors) {
    ImportContext importContext = new ImportContext(batch);
    List<RoleAssignmentImportDto> successfulEntries = new ArrayList<>();

    Map<String, List<RoleAssignmentImportDto>> rolesByUser =
        batch.stream().collect(Collectors.groupingBy(RoleAssignmentImportDto::getUsername));

    for (Map.Entry<String, List<RoleAssignmentImportDto>> entry : rolesByUser.entrySet()) {
      String username = entry.getKey();
      List<RoleAssignmentImportDto> userRoles = entry.getValue();
      User user = importContext.usersByUsername.get(username);
      if (user == null) {
        userRoles.forEach(dto -> addError(errors, dto, USER_NOT_FOUND));
      } else {
        user.clearRoleAssignments();
        userRepository.saveAndFlush(user);
        RoleAssignmentCreatorResult roleAssignmentCreatorResult =
            processUserRoles(userRoles, importContext, user, errors);
        user.assignRoles(
            roleAssignmentCreatorResult.getRoleAssignments().toArray(new RoleAssignment[0]));
        successfulEntries.addAll(roleAssignmentCreatorResult.getDtos());
      }
    }

    return new SaveBatchResultDto<>(successfulEntries, errors);
  }

  private RoleAssignmentCreatorResult processUserRoles(List<RoleAssignmentImportDto> userRoles,
      ImportContext importContext, User user, List<ImportResponseDto.ErrorDetails> errors) {
    List<RoleAssignment> successAssignments = new ArrayList<>();
    List<RoleAssignmentImportDto> successDtos = new ArrayList<>();

    for (RoleAssignmentImportDto dto : userRoles) {
      Role role = importContext.rolesByName.get(dto.getRoleName());
      if (role == null) {
        addError(errors, dto, ROLE_NOT_FOUND);
      } else {
        try {
          RoleAssignment roleAssignment = createRoleAssignment(user, role, dto, importContext);
          successAssignments.add(roleAssignment);
          successDtos.add(dto);
        } catch (ValidationMessageException | NotFoundException ex) {
          addError(errors, dto, ex.asMessage().getKey());
        } catch (Exception ex) {
          addError(errors, dto, ex.getMessage());
        }
      }
    }

    return new RoleAssignmentCreatorResult(successAssignments, successDtos);
  }

  private RoleAssignment createRoleAssignment(User user, Role role, RoleAssignmentImportDto dto,
                                              ImportContext importContext) {
    RoleAssignment roleAssignment;
    if (SupervisionRoleAssignment.SUPERVISION_TYPE.equalsIgnoreCase(dto.getType())) {
      Program program = requireNotNullOrThrow(
          importContext.programsByCode.get(dto.getProgramCode()), "program");
      SupervisoryNode supervisoryNode = StringUtils.isBlank(dto.getSupervisoryNodeCode())
          ? null
          : requireNotNullOrThrow(importContext.supervisoryNodesByCode
              .get(dto.getSupervisoryNodeCode()), "supervisoryNode");
      roleAssignment = new SupervisionRoleAssignment(role, user, program, supervisoryNode);
    } else if (FulfillmentRoleAssignment.FULFILLMENT_TYPE.equalsIgnoreCase(dto.getType())) {
      Facility warehouse = requireNotNullOrThrow(
          importContext.facilitiesByCode.get(dto.getWarehouseCode()), "warehouse");
      roleAssignment = new FulfillmentRoleAssignment(role, user, warehouse);
    } else {
      roleAssignment = new DirectRoleAssignment(role, user);
    }

    return roleAssignment;
  }

  private <T> T requireNotNullOrThrow(T object, String field) {
    if (object == null) {
      throw new NotFoundException("referenceData.error.user.import." + field + ".notFound");
    }
    return object;
  }

  private void addError(List<ImportResponseDto.ErrorDetails> errors, RoleAssignmentImportDto dto,
                        String messageKey) {
    errors.add(new ImportResponseDto.UserRoleErrorDetails(
        dto.getRoleName(), Collections.singletonList(messageKey), dto.getUsername()));
  }

  private class ImportContext {
    Map<String, User> usersByUsername;
    Map<String, Role> rolesByName;
    Map<String, Program> programsByCode;
    Map<String, Facility> facilitiesByCode;
    Map<String, SupervisoryNode> supervisoryNodesByCode;

    ImportContext(List<RoleAssignmentImportDto> dtoList) {
      Set<String> usernames = extract(dtoList, RoleAssignmentImportDto::getUsername);
      usersByUsername = usernames.isEmpty()
          ? Collections.emptyMap()
          : mapByKey(userRepository.findAllByUsernameIn(usernames), User::getUsername);

      Set<String> roleNames = extract(dtoList, RoleAssignmentImportDto::getRoleName);
      rolesByName = roleNames.isEmpty()
          ? Collections.emptyMap()
          : mapByKey(roleRepository.findAllByNameIn(roleNames), Role::getName);

      List<Code> programCodeNames = extract(dtoList, RoleAssignmentImportDto::getProgramCode)
          .stream().map(Code::code).collect(Collectors.toList());
      programsByCode = programCodeNames.isEmpty()
          ? Collections.emptyMap()
          : mapByKey(programRepository.findAllByCodeIn(
              programCodeNames), p -> p.getCode().toString());

      List<String> facilityCodeNames =
          new ArrayList<>(extract(dtoList, RoleAssignmentImportDto::getWarehouseCode));
      facilitiesByCode =
          facilityCodeNames.isEmpty()
              ? Collections.emptyMap()
              : mapByKey(facilityRepository.findAllByCodeIn(
                  facilityCodeNames), Facility::getCode);

      List<String> supervisoryCodeNames =
          new ArrayList<>(extract(dtoList, RoleAssignmentImportDto::getSupervisoryNodeCode));
      supervisoryNodesByCode = supervisoryCodeNames.isEmpty()
          ? Collections.emptyMap()
          : mapByKey(supervisoryNodeRepository.findAllByCodeIn(
              supervisoryCodeNames), SupervisoryNode::getCode);
    }

    private Set<String> extract(List<RoleAssignmentImportDto> list,
                                Function<RoleAssignmentImportDto, String> extractor) {
      return list.stream()
          .map(extractor)
          .filter(Objects::nonNull)
          .collect(Collectors.toSet());
    }

    private <T> Map<String, T> mapByKey(List<T> list, Function<T, String> keyMapper) {
      return list.stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }
  }
}

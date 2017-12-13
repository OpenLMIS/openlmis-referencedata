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

package org.openlmis.referencedata.web;

import static org.openlmis.referencedata.domain.RightName.SERVICE_ACCOUNTS_MANAGE;

import org.openlmis.referencedata.domain.CreationDetails;
import org.openlmis.referencedata.domain.ServiceAccount;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ServiceAccountDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.ServiceAccountRepository;
import org.openlmis.referencedata.service.AuthService;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.util.messagekeys.ServiceAccountMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@Transactional
public class ServiceAccountController extends BaseController {
  private static final Logger LOGGER = LoggerFactory.getLogger(FacilityController.class);

  @Autowired
  private ServiceAccountRepository serviceAccountRepository;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  @Autowired
  private AuthService authService;

  /**
   * Allows creating new service account.
   *
   * @return created service account.
   */
  @RequestMapping(value = "/serviceAccounts", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public ServiceAccountDto create() {
    Profiler profiler = new Profiler("CREATE_SERVICE_ACCOUNT");
    profiler.setLogger(LOGGER);

    checkAdminRight(SERVICE_ACCOUNTS_MANAGE, profiler);

    profiler.start("GET_CURRENT_USER");
    User user = authenticationHelper.getCurrentUser();

    profiler.start("GET_API_KEY");
    String key = authService.createApiKey();

    profiler.start("CREATE_NEW_INSTANCE");
    CreationDetails creationDetails = new CreationDetails(user.getId());
    ServiceAccount account = new ServiceAccount(key, creationDetails);
    serviceAccountRepository.save(account);

    ServiceAccountDto dto = toDto(account, profiler);

    profiler.stop().log();
    return dto;
  }

  /**
   * Retrieves all service accounts.
   *
   * @return List of service accounts.
   */
  @RequestMapping(value = "/serviceAccounts", method = RequestMethod.GET)
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public Page<ServiceAccountDto> getAll(Pageable pageable) {
    Profiler profiler = new Profiler("GET_SERVICE_ACCOUNTS");
    profiler.setLogger(LOGGER);

    checkAdminRight(SERVICE_ACCOUNTS_MANAGE, profiler);

    profiler.start("DB_CALL");
    Page<ServiceAccount> result = serviceAccountRepository.findAll(pageable);
    List<ServiceAccountDto> dtos = toDto(result.getContent(), profiler);
    Page<ServiceAccountDto> page = toPage(dtos, pageable, profiler);

    profiler.stop().log();

    return page;
  }

  /**
   * Allows deleting service account.
   *
   * @param id UUID of service account which we want to delete
   */
  @RequestMapping(value = "/serviceAccounts/{id}", method = RequestMethod.DELETE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable("id") UUID id) {
    Profiler profiler = new Profiler("DELETE_SERVICE_ACCOUNT");
    profiler.setLogger(LOGGER);

    checkAdminRight(SERVICE_ACCOUNTS_MANAGE, profiler);

    profiler.start("FIND_SERVICE_ACCOUNT");
    ServiceAccount account = serviceAccountRepository.findOne(id);

    if (null == account) {
      profiler.stop().log();
      throw new NotFoundException(ServiceAccountMessageKeys.ERROR_NOT_FOUND);
    }

    profiler.start("DELETE_API_KEY");
    authService.removeApiKey(account.getLogin());

    profiler.start("DELETE_SERVICE_ACCOUNT");
    serviceAccountRepository.delete(account);

    profiler.stop().log();
  }

  private ServiceAccountDto toDto(ServiceAccount account, Profiler profiler) {
    profiler.start("EXPORT_SERVICE_ACCOUNT_TO_DTO");
    return ServiceAccountDto.newInstance(account);
  }

  private List<ServiceAccountDto> toDto(List<ServiceAccount> accounts, Profiler profiler) {
    profiler.start("EXPORT_SERVICE_ACCOUNTS_TO_DTO");
    return accounts
        .stream()
        .map(ServiceAccountDto::newInstance)
        .collect(Collectors.toList());
  }

}

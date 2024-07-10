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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.openlmis.referencedata.domain.IntegrationEmail;
import org.openlmis.referencedata.dto.IntegrationEmailDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.IntegrationEmailRepository;
import org.openlmis.referencedata.util.Message;
import org.openlmis.referencedata.util.Pagination;
import org.openlmis.referencedata.util.messagekeys.IntegrationEmailMessageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller used to expose IntegrationEmails via HTTP.
 */
@RestController
@RequestMapping(IntegrationEmailController.RESOURCE_PATH)
@Transactional
public class IntegrationEmailController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationEmailController.class);

  public static final String RESOURCE_PATH = API_PATH + "/integrationEmails";

  @Autowired
  private IntegrationEmailRepository integrationEmailRepository;

  /**
   * Allows the creation of a new integration email. If the id is specified, it will be ignored.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public IntegrationEmailDto createIntegrationEmail(
      @RequestBody IntegrationEmailDto integrationEmail) {
    LOGGER.debug("Creating new integration email");
    IntegrationEmail newIntegrationEmail = IntegrationEmail.newInstance(integrationEmail);
    newIntegrationEmail.setId(null);
    newIntegrationEmail = integrationEmailRepository.saveAndFlush(newIntegrationEmail);

    return IntegrationEmailDto.newInstance(newIntegrationEmail);
  }

  /**
   * Updates the specified integration email.
   */
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  public IntegrationEmailDto saveIntegrationEmail(@PathVariable("id") UUID id,
      @RequestBody IntegrationEmailDto integrationEmail) {
    if (null != integrationEmail.getId() && !Objects.equals(integrationEmail.getId(), id)) {
      throw new ValidationMessageException(new Message(
          IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_ID_MISMATCH));
    }

    LOGGER.debug("Updating integration email");
    IntegrationEmail db;
    Optional<IntegrationEmail> integrationEmailOptional = integrationEmailRepository.findById(id);
    if (integrationEmailOptional.isPresent()) {
      db = integrationEmailOptional.get();
      db.updateFrom(integrationEmail);
    } else {
      db = IntegrationEmail.newInstance(integrationEmail);
      db.setId(id);
    }

    integrationEmailRepository.saveAndFlush(db);

    return IntegrationEmailDto.newInstance(db);
  }

  /**
   * Deletes the specified integration email.
   */
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteIntegrationEmail(@PathVariable("id") UUID id) {
    if (!integrationEmailRepository.existsById(id)) {
      throw new NotFoundException(new Message(
          IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_NOT_FOUND));
    }

    integrationEmailRepository.deleteById(id);
  }

  /**
   * Retrieves all integration emails. Note that an empty collection rather than a 404 should be
   * returned if no integration emails exist.
   */
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public Page<IntegrationEmailDto> getAllIntegrationEmails(Pageable pageable) {
    Page<IntegrationEmail> page = integrationEmailRepository.findAll(pageable);
    List<IntegrationEmailDto> content = page
        .getContent()
        .stream()
        .map(IntegrationEmailDto::newInstance)
        .collect(Collectors.toList());
    return Pagination.getPage(content, pageable, page.getTotalElements());
  }

  /**
   * Retrieves the specified integration email.
   */
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  public IntegrationEmailDto getSpecifiedIntegrationEmail(@PathVariable("id") UUID id) {
    IntegrationEmail integrationEmail = integrationEmailRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(new Message(
            IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_NOT_FOUND)));

    return IntegrationEmailDto.newInstance(integrationEmail);
  }

  /**
   * Retrieves audit information related to the specified integration email.
   *
   * @param author The author of the changes which should be returned.
   *               If null or empty, changes are returned regardless of author.
   * @param changedPropertyName The name of the property about which changes should be returned.
   *               If null or empty, changes associated with any and all properties are returned.
   * @param page A Pageable object that allows client to optionally add "page" (page number)
   *             and "size" (page size) query parameters to the request.
   */
  @GetMapping(value = "/{id}/auditLog")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<String> getIntegrationEmailAuditLog(@PathVariable("id") UUID id,
      @RequestParam(name = "author", required = false, defaultValue = "") String author,
      @RequestParam(name = "changedPropertyName", required = false, defaultValue = "")
      String changedPropertyName,
      @RequestParam(name = "returnJSON", required = false, defaultValue = "true")
          boolean returnJson,
      Pageable page) {

    //Return a 404 if the specified instance can't be found
    if (!integrationEmailRepository.existsById(id)) {
      throw new NotFoundException(new Message(
          IntegrationEmailMessageKeys.ERROR_INTEGRATION_EMAIL_NOT_FOUND));
    }

    return getAuditLogResponse(IntegrationEmail.class, id, author, changedPropertyName, page,
        returnJson);
  }

}

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

package org.openlmis.referencedata.service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.SystemNotificationDto;
import org.openlmis.referencedata.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemNotificationBuilder
    implements DomainResourceBuilder<SystemNotificationDto, SystemNotification> {

  @Autowired
  private UserRepository userRepository;

  @Value("${time.zoneId}")
  private String timeZoneId;

  @Override
  public SystemNotification build(SystemNotificationDto dto) {
    User author = userRepository.findOne(dto.getAuthorId());

    if (dto.getExpiryDate() != null && dto.getStartDate() != null) {
      dto.setDisplayed(dto.isActive()
          && dto.getExpiryDate().isAfter(ZonedDateTime.now(ZoneId.of(timeZoneId)))
          && dto.getStartDate().isBefore(ZonedDateTime.now(ZoneId.of(timeZoneId))));
    } else if (dto.getStartDate() != null) {
      dto.setDisplayed(dto.isActive()
          && dto.getStartDate().isBefore(ZonedDateTime.now(ZoneId.of(timeZoneId))));
    } else if (dto.getExpiryDate() != null) {
      dto.setDisplayed(dto.isActive()
          && dto.getExpiryDate().isAfter(ZonedDateTime.now(ZoneId.of(timeZoneId))));
    } else {
      dto.setDisplayed(dto.isActive());
    }
    return SystemNotification.newInstance(dto, author);
  }

}

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;

import java.time.ZonedDateTime;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.SystemNotificationDto;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.testbuilder.SystemNotificationDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class SystemNotificationBuilderTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private SystemNotificationBuilder systemNotificationBuilder;

  private User user = new UserDataBuilder().build();
  private SystemNotificationDto dto = new SystemNotificationDto();

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(systemNotificationBuilder, "timeZoneId", "UTC");
    given(userRepository.findOne(any(UUID.class))).willReturn(user);
  }

  @Test
  public void shouldSetDisplayedAsTrueIfActiveFlagIsTrueAndStartDateAndExpiryDateAreNotSet() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withoutExpiryDate()
        .withoutStartDate()
        .build();

    systemNotification.export(dto);

    assertTrue(systemNotificationBuilder.build(dto).isDisplayed());
  }

  @Test
  public void shouldSetDisplayedAsTrueIfIsActiveAndNonExpired() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .build();

    systemNotification.export(dto);

    assertTrue(systemNotificationBuilder.build(dto).isDisplayed());
  }

  @Test
  public void shouldSetDisplayedAsTrueIfIsActiveAndExpiryDateIsNotSet() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withoutExpiryDate()
        .build();

    systemNotification.export(dto);

    assertTrue(systemNotificationBuilder.build(dto).isDisplayed());
  }

  @Test
  public void shouldSetDisplayedAsTrueIfIsActiveAndStartDateIsNotSet() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withoutStartDate()
        .build();

    systemNotification.export(dto);

    assertTrue(systemNotificationBuilder.build(dto).isDisplayed());
  }

  @Test
  public void shouldSetDisplayedAsFalseIfActiveFlagIsFalse() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .build();

    systemNotification.export(dto);

    assertFalse(systemNotificationBuilder.build(dto).isDisplayed());
  }

  @Test
  public void shouldSetDisplayedAsFalseIfActiveFlagIsTrueAndEpiryDateIsInThePast() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withExpiryDate(ZonedDateTime.now().minusDays(1))
        .build();

    systemNotification.export(dto);

    assertFalse(systemNotificationBuilder.build(dto).isDisplayed());
  }

  @Test
  public void shouldSetDisplayedAsFalseIfActiveFlagIsTrueAndStartDateIsInTheFuture() {
    SystemNotification systemNotification = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withStartDate(ZonedDateTime.now().plusDays(1))
        .build();

    systemNotification.export(dto);

    assertFalse(systemNotificationBuilder.build(dto).isDisplayed());
  }

}

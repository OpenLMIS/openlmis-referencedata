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

package org.openlmis.referencedata.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.SystemNotification;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.repository.custom.SystemNotificationRepositoryCustom;
import org.openlmis.referencedata.testbuilder.SystemNotificationDataBuilder;
import org.openlmis.referencedata.testbuilder.UserDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class SystemNotificationRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest {

  @Autowired
  private SystemNotificationRepository repository;

  @Autowired
  private UserRepository userRepository;

  private User user;
  private Pageable pageable = PageRequest.of(0, 10);

  SystemNotificationRepository getRepository() {
    return this.repository;
  }

  @Before
  public void setUp() {
    user = new UserDataBuilder().buildAsNew();
    userRepository.save(user);
  }

  SystemNotification generateInstance() {
    return new SystemNotificationDataBuilder().withAuthor(user).buildAsNew();
  }

  SystemNotification generateInstance(User user) {
    return new SystemNotificationDataBuilder().withAuthor(user).buildAsNew();
  }

  @Test
  public void shouldReturnSystemNotificationsThatMatchAllSearchParams() {
    User secondUser = new UserDataBuilder().buildAsNew();
    userRepository.save(secondUser);

    SystemNotification notification1 = generateInstance(secondUser);
    repository.save(notification1);

    SystemNotification notification2 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .buildAsNew();
    repository.save(notification2);

    SystemNotification notification3 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withExpiryDate(ZonedDateTime.now().plusDays(1))
        .buildAsNew();
    repository.save(notification3);

    SystemNotification notification4 = generateInstance();
    repository.save(notification4);

    SystemNotificationRepositoryCustom.SearchParams searchParams =
        new SystemNotificationRepositoryIntegrationTest.TestSearchParams(user.getId(), true);

    Page<SystemNotification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(2)
        .containsExactly(notification4, notification3);
  }

  @Test
  public void shouldReturnSystemNotificationsByAuthorId() {
    User secondUser = new UserDataBuilder().buildAsNew();
    userRepository.save(secondUser);

    SystemNotification notification1 = generateInstance(secondUser);
    repository.save(notification1);

    SystemNotification notification2 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withExpiryDate(ZonedDateTime.now().plusDays(1))
        .asInactive()
        .buildAsNew();
    repository.save(notification2);

    SystemNotification notification3 = generateInstance();
    repository.save(notification3);

    SystemNotification notification4 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .buildAsNew();
    repository.save(notification4);

    SystemNotificationRepositoryCustom.SearchParams searchParams =
        new SystemNotificationRepositoryIntegrationTest.TestSearchParams(user.getId(), null);

    Page<SystemNotification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(3)
        .containsExactly(notification3, notification4, notification2);
  }

  @Test
  public void shouldReturnActiveAndNonExpiredSystemNotifications() {
    User secondUser = new UserDataBuilder().buildAsNew();
    userRepository.save(secondUser);

    SystemNotification notification1 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withoutExpiryDate()
        .withoutStartDate()
        .buildAsNew();
    repository.save(notification1);

    SystemNotification notification2 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .buildAsNew();
    repository.save(notification2);

    SystemNotification notification3 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withExpiryDate(ZonedDateTime.now().plusYears(1))
        .withoutStartDate()
        .buildAsNew();
    repository.save(notification3);

    SystemNotification notification4 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withoutExpiryDate()
        .withStartDate(ZonedDateTime.now().minusYears(1))
        .buildAsNew();
    repository.save(notification4);

    SystemNotification notification5 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withStartDate(ZonedDateTime.now(ZoneId.of("Europe/Warsaw")).minusMinutes(10))
        .buildAsNew();
    repository.save(notification5);

    SystemNotificationRepositoryCustom.SearchParams searchParams =
        new SystemNotificationRepositoryIntegrationTest.TestSearchParams(null, true);

    Page<SystemNotification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(4)
        .contains(notification4, notification1, notification3, notification5);
  }

  @Test
  public void shouldReturnInactiveOrExpiredSystemNotifications() {
    User secondUser = new UserDataBuilder().buildAsNew();
    userRepository.save(secondUser);

    SystemNotification notification1 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .withoutExpiryDate()
        .withoutStartDate()
        .buildAsNew();
    repository.save(notification1);

    SystemNotification notification2 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .buildAsNew();
    repository.save(notification2);

    SystemNotification notification3 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withExpiryDate(ZonedDateTime.now().minusDays(1))
        .withoutStartDate()
        .buildAsNew();
    repository.save(notification3);

    SystemNotification notification4 = generateInstance();
    repository.save(notification4);

    SystemNotification notification5 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withStartDate(ZonedDateTime.now().plusDays(1))
        .withoutExpiryDate()
        .buildAsNew();
    repository.save(notification5);

    SystemNotificationRepositoryCustom.SearchParams searchParams =
        new SystemNotificationRepositoryIntegrationTest.TestSearchParams(null, false);

    Page<SystemNotification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(4)
        .containsExactly(notification5, notification3, notification1, notification2);
  }

  @Test
  public void shouldReturnAllSystemNotificationsWhenNoParamsProvided() {
    User secondUser = new UserDataBuilder().buildAsNew();
    userRepository.save(secondUser);

    SystemNotification notification1 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .withoutExpiryDate()
        .buildAsNew();
    repository.save(notification1);

    SystemNotification notification2 = new SystemNotificationDataBuilder()
        .withAuthor(user)
        .asInactive()
        .buildAsNew();
    repository.save(notification2);

    SystemNotification notification3 = generateInstance();
    repository.save(notification3);

    SystemNotificationRepositoryCustom.SearchParams searchParams =
        new SystemNotificationRepositoryIntegrationTest.TestSearchParams(null, null);

    Page<SystemNotification> search = repository.search(searchParams, pageable);
    assertThat(search.getContent())
        .hasSize(3)
        .containsExactly(notification1, notification3, notification2);
  }

  @Getter
  private static final class TestSearchParams
      implements SystemNotificationRepositoryCustom.SearchParams {

    private UUID authorId;
    private Boolean isDisplayed;

    TestSearchParams() {
      this(null, true);
    }

    TestSearchParams(UUID authorId, Boolean isDisplayed) {
      this.authorId = Optional
          .ofNullable(authorId)
          .orElse(null);

      this.isDisplayed = Optional
          .ofNullable(isDisplayed)
          .orElse(null);
    }
  }

}

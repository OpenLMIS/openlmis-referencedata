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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openlmis.referencedata.Application;
import org.openlmis.referencedata.dto.ImportResponseDto;
import org.openlmis.referencedata.dto.SaveBatchResultDto;
import org.openlmis.referencedata.dto.UserDto;
import org.openlmis.referencedata.service.UserAuthService;
import org.openlmis.referencedata.service.UserDetailsService;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, DataImportService.class})
@ActiveProfiles({"test", "test-run"})
public class UserImportPersisterIntegrationTest {

  @Autowired
  private UserImportPersister userImportPersister;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private UserAuthService userAuthService;

  @Mock
  private UserImportRollback userImportRollback;

  @Before
  public void setup() {
    ReflectionTestUtils.setField(userImportPersister, "importExecutorService",
        MoreExecutors.newDirectExecutorService());
    ReflectionTestUtils.setField(userImportPersister, "userDetailsService", userDetailsService);
    ReflectionTestUtils.setField(userImportPersister, "userAuthService", userAuthService);
    ReflectionTestUtils.setField(userImportPersister, "userImportRollback", userImportRollback);
  }

  @Test
  public void shouldImportUsers() throws IOException, InterruptedException {
    when(userDetailsService.saveUsersContactDetailsFromFile(anyList(),anyList()))
        .thenReturn(new SaveBatchResultDto<>(
            Collections.singletonList(new UserDto()), Collections.emptyList()));

    when(userAuthService.saveUserAuthDetailsFromFile(anyList()))
        .thenReturn(new SaveBatchResultDto<>(
            Collections.singletonList(new UserDto()), Collections.emptyList()));

    doNothing().when(userImportRollback).cleanupInconsistentData(anyList(), anyList());

    final ImportResponseDto.ImportDetails result = userImportPersister.processAndPersist(
        new ClassPathResource("/UserImportPersisterTest/user.csv").getInputStream(),
            mock(Profiler.class));

    assertEquals(Integer.valueOf(1), result.getSuccessfulEntriesCount());
  }
}

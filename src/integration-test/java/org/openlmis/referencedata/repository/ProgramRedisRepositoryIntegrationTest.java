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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.dto.ProgramDto;
import org.openlmis.referencedata.testbuilder.ProgramDataBuilder;

public class ProgramRedisRepositoryIntegrationTest extends BaseRedisRepositoryIntegrationTest {

  private Program program;
  private ProgramDto programDto = new ProgramDto();
  private UUID programId;

  @Before
  public void setUp() {
    programId = UUID.randomUUID();
    program = new ProgramDataBuilder()
        .withId(programId)
        .build();
    program.export(programDto);

    programRepository.save(program);
    programRedisRepository.save(program);
  }

  @Test
  public void shouldReturnTrueIfProgramExistsInCacheWithGivenId() {
    boolean exists = programRedisRepository.exists(programId);

    assertTrue(exists);
  }

  @Test
  public void shouldFindProgramById() {
    Program programFromCache = programRedisRepository.findById(programId);

    assertNotNull(programFromCache);
    assertEquals(programFromCache, program);
  }

}

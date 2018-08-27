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

package org.openlmis.referencedata.util;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Dispensable;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.dto.OrderableDto;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.repository.ProgramRepository;

@RunWith(MockitoJUnitRunner.class)
public class OrderableBuilderTest {

  @Mock
  private ProgramRepository programRepository;

  @InjectMocks
  private OrderableBuilder orderableBuilder;

  @Test
  public void shouldCreateOrderableWithSingleProgram() throws Exception {
    Program program = createProgram("test_program");
    Orderable orderable = createOrderable(program.getId());

    assertOrderable(orderable, program);
  }

  @Test
  public void shouldCreateOrderableWithMultiplePrograms() throws Exception {
    Program program1 = createProgram("test_program_1");
    Program program2 = createProgram("test_program_2");

    Orderable orderable = createOrderable(program1.getId(), program2.getId());

    assertOrderable(orderable, program1, program2);
  }

  private Program createProgram(String code) {
    Program program = new Program(UUID.randomUUID());
    program.setCode(Code.code(code));

    when(programRepository.findOne(program.getId())).thenReturn(program);

    return program;
  }

  private void assertOrderable(Orderable orderable, Program... programs) {
    for (Program program : programs) {
      ProgramOrderable programOrderable = orderable.getProgramOrderable(program);

      assertThat(programOrderable, is(notNullValue()));
      assertThat(programOrderable.getProgram().getCode(), is(equalTo(program.getCode())));
    }
  }

  private Orderable createOrderable(UUID... programIds) {
    Set<ProgramOrderableDto> programs = Sets.newHashSet();

    for (UUID programId : programIds) {
      ProgramOrderableDto programOrderableDto = new ProgramOrderableDto();
      programOrderableDto.setProgramId(programId);
      programOrderableDto.setActive(true);

      programs.add(programOrderableDto);
    }

    OrderableDto orderableDto = new OrderableDto();
    orderableDto.setNetContent(100L);
    orderableDto.setPackRoundingThreshold(1L);
    orderableDto.setRoundToZero(true);
    orderableDto.setPrograms(programs);
    orderableDto.setDispensable(Dispensable.createNew("each"));

    return orderableBuilder.newOrderable(orderableDto);
  }

}
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.Program;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProgramRepositoryIntegrationTest extends BaseCrudRepositoryIntegrationTest<Program> {

  @Autowired
  ProgramRepository repository;
  
  String programCode;
  String programName;

  ProgramRepository getRepository() {
    return this.repository;
  }

  Program generateInstance() {
    programCode = String.valueOf(this.getNextInstanceNumber());
    Program program = new Program(programCode);
    programName = "Program Name";
    program.setName(programName);
    program.setPeriodsSkippable(true);
    program.setEnableDatePhysicalStockCountCompleted(true);
    return program;
  }

  @Test
  public void testSkippableEdit() {
    Program testProgram = this.generateInstance();
    testProgram = repository.save(testProgram);
    testProgram = repository.findOne(testProgram.getId());
    assertTrue(testProgram.getPeriodsSkippable());

    testProgram.setPeriodsSkippable(false);
    testProgram = repository.save(testProgram);
    testProgram = repository.findOne(testProgram.getId());
    assertFalse(testProgram.getPeriodsSkippable());
    repository.deleteAll();
  }

  @Test
  public void testEnableDatePhysicalStockCountCompletedEdit() {
    Program testProgram = this.generateInstance();
    testProgram = repository.save(testProgram);
    testProgram = repository.findOne(testProgram.getId());
    assertTrue(testProgram.getEnableDatePhysicalStockCountCompleted());

    testProgram.setEnableDatePhysicalStockCountCompleted(false);
    testProgram = repository.save(testProgram);
    testProgram = repository.findOne(testProgram.getId());
    assertFalse(testProgram.getEnableDatePhysicalStockCountCompleted());
    repository.deleteAll();
  }
  
  @Test
  public void shouldFindByCode() {
    //given
    Program program = this.generateInstance();
    repository.save(program);

    //when
    Program foundProgram = repository.findByCode(Code.code(programCode));

    //then
    assertEquals(program, foundProgram);
  }

  @Test
  public void shouldFindBySimilarName() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("Program");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));
  }

  @Test
  public void shouldFindBySimilarNameIgnoringCase() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("PROGRAM");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));

    foundPrograms = repository.findProgramsByName("program");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));

    foundPrograms = repository.findProgramsByName("ProGRam");

    assertEquals(1, foundPrograms.size());
    assertEquals(program, foundPrograms.get(0));
  }

  @Test
  public void shouldNotFindByIncorrectSimilarName() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("Incorrect Name");

    assertEquals(0, foundPrograms.size());
  }
}

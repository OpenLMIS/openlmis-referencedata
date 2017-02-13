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
  }

  @Test
  public void shouldNotFindByIncorrectSimilarName() {
    Program program = this.generateInstance();
    repository.save(program);

    List<Program> foundPrograms = repository.findProgramsByName("Incorrect Name");

    assertEquals(0, foundPrograms.size());
  }
}

package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.Program;

import java.util.List;

public interface ProgramRepositoryCustom {
  List<Program> findProgramsWithSimilarName(String name);
}

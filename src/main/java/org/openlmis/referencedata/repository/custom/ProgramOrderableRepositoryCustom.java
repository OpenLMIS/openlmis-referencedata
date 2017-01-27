package org.openlmis.referencedata.repository.custom;

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;

import java.util.List;

public interface ProgramOrderableRepositoryCustom {

  List<ProgramOrderable> searchProgramOrderables(Program program);
}

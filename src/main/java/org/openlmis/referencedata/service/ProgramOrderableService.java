package org.openlmis.referencedata.service;

import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProgramOrderableService {

  @Autowired
  private ProgramOrderableRepository programOrderableRepository;

  /**
   * Finds ProgramOrderables matching all of provided parameters.
   * @param program program of searched ProgramOrderables.
   * @return list of all ProgramOrderables matching all of provided parameters.
   */
  public List<ProgramOrderable> searchProgramOrderables(Program program) {
    return programOrderableRepository.searchProgramOrderables(program);
  }
}

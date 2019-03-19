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

import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.dto.SupervisoryNodeDto;
import org.openlmis.referencedata.repository.custom.BaseRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Transactional
public abstract class BaseRedisRepositoryIntegrationTest {

  @Autowired
  protected SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  protected BaseRedisRepository<SupervisoryNodeDto> supervisoryNodeDtoRedisRepository;

  @Autowired
  protected ProgramRepository programRepository;

  @Autowired
  protected BaseRedisRepository<Program> programRedisRepository;

  @Autowired
  protected FacilityRepository facilityRepository;

  @Autowired
  protected FacilityTypeRepository facilityTypeRepository;

  @Autowired
  protected FacilityOperatorRepository facilityOperatorRepository;

  @Autowired
  protected GeographicZoneRepository geographicZoneRepository;

  @Autowired
  protected GeographicLevelRepository geographicLevelRepository;

  @Value("${service.url}")
  protected String baseUri;
}

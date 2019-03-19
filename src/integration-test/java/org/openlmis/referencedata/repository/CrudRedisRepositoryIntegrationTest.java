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

import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.referencedata.domain.Identifiable;
import org.openlmis.referencedata.repository.custom.CrudRedisRepository;
import org.openlmis.referencedata.repository.custom.impl.ProgramRedisRepository;
import org.openlmis.referencedata.repository.custom.impl.SupervisoryNodeDtoRedisRepository;
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
public abstract class CrudRedisRepositoryIntegrationTest<T extends Identifiable> {

  @Value("${service.url}")
  protected String baseUri;

  @Autowired
  protected SupervisoryNodeRepository supervisoryNodeRepository;

  @Autowired
  protected SupervisoryNodeDtoRedisRepository supervisoryNodeDtoRedisRepository;

  @Autowired
  protected ProgramRepository programRepository;

  @Autowired
  protected ProgramRedisRepository programRedisRepository;

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

  abstract CrudRedisRepository<T> getRepository();

  /*
   * Generate a unique instance of given type.
   * @return generated instance
   */
  abstract T generateInstance() throws Exception;

  protected void assertInstance(T instance) {
    Assert.assertNotNull(instance.getId());
  }

  @Test
  public void shouldCreate() throws Exception {
    CrudRedisRepository<T> repository = this.getRepository();

    T instance = this.generateInstance();

    repository.save(instance);

    Assert.assertTrue(repository.exists(instance.getId()));
  }

  @Test
  public void shouldFindOne() throws Exception {
    CrudRedisRepository<T> repository = this.getRepository();

    T instance = this.generateInstance();

    repository.save(instance);

    UUID id = instance.getId();

    instance = repository.findById(id);
    assertInstance(instance);
    Assert.assertEquals(id, instance.getId());
  }

  @Test
  public void shouldDelete() throws Exception {
    CrudRedisRepository<T> repository = this.getRepository();

    T instance = this.generateInstance();
    Assert.assertNotNull(instance);

    repository.save(instance);

    UUID id = instance.getId();

    repository.delete(instance);
    Assert.assertFalse(repository.exists(id));
  }
}

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

package org.openlmis.referencedata.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.Facility;
import org.openlmis.referencedata.domain.SupervisoryNode;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.FacilityRepository;
import org.openlmis.referencedata.repository.SupervisoryNodeRepository;
import org.openlmis.referencedata.repository.custom.SupervisoryNodeRedisRepository;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.testbuilder.SupervisoryNodeDataBuilder;

@RunWith(MockitoJUnitRunner.class)
public class SupervisoryNodeServiceTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private FacilityRepository facilityRepository;

  @Mock
  private SupervisoryNodeRepository supervisoryNodeRepository;

  @Mock
  private SupervisoryNodeRedisRepository supervisoryNodeRedisRepository;

  @InjectMocks
  private SupervisoryNodeService supervisoryNodeService;

  private Facility facility = new FacilityDataBuilder().build();

  private SupervisoryNode supervisoryNode = new SupervisoryNodeDataBuilder()
      .withFacility(facility)
      .build();

  @Before
  public void setUp() {
    when(facilityRepository.findOne(facility.getId()))
        .thenReturn(facility);
  }

  @Test
  public void shouldGetSupervisoryNodeFromDatabaseWhenNotInCache() {
    UUID supervisoryNodeId = supervisoryNode.getId();

    when(supervisoryNodeRepository.exists(supervisoryNodeId)).thenReturn(true);
    when(supervisoryNodeRedisRepository.exists(supervisoryNodeId)).thenReturn(false);
    when(supervisoryNodeRepository.findOne(supervisoryNodeId)).thenReturn(supervisoryNode);

    SupervisoryNode supervisoryNode1 = supervisoryNodeService.getSupervisoryNode(supervisoryNodeId);
    assertNotNull(supervisoryNode1);
    verify(supervisoryNodeRepository).findOne(supervisoryNodeId);
    verify(supervisoryNodeRedisRepository, never()).findById(any(UUID.class));
  }

  @Test
  public void shouldSaveSupervisoryNodeInCacheAfterGettingOneFromDatabase() {
    UUID supervisoryNodeId = supervisoryNode.getId();

    when(supervisoryNodeRepository.exists(supervisoryNodeId)).thenReturn(true);
    when(supervisoryNodeRedisRepository.exists(supervisoryNodeId)).thenReturn(false);
    when(supervisoryNodeRepository.findOne(supervisoryNodeId)).thenReturn(supervisoryNode);

    SupervisoryNode supervisoryNode1 = supervisoryNodeService.getSupervisoryNode(supervisoryNodeId);

    verify(supervisoryNodeRedisRepository).save(supervisoryNode1);
  }

  @Test
  public void shouldGetSupervisoryNodeFromCache() {
    UUID supervisoryNodeId = supervisoryNode.getId();

    when(supervisoryNodeRepository.exists(supervisoryNodeId)).thenReturn(true);
    when(supervisoryNodeRedisRepository.exists(supervisoryNodeId)).thenReturn(true);
    when(supervisoryNodeRedisRepository.findById(supervisoryNodeId)).thenReturn(supervisoryNode);

    SupervisoryNode supervisoryNode1 = supervisoryNodeService.getSupervisoryNode(supervisoryNodeId);

    verifyZeroInteractions(supervisoryNodeRepository);
    verify(supervisoryNodeRedisRepository, times(1)).findById(supervisoryNodeId);
    assertEquals(supervisoryNode1, supervisoryNode);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowErrorNotFoundWhenNeitherInDatabaseNorInCache() {
    UUID supervisoryNodeId = supervisoryNode.getId();

    when(supervisoryNodeRepository.exists(supervisoryNodeId)).thenReturn(false);
    when(supervisoryNodeRedisRepository.exists(supervisoryNodeId)).thenReturn(false);

    supervisoryNodeService.getSupervisoryNode(supervisoryNodeId);

    assertThatThrownBy(() -> supervisoryNodeService.getSupervisoryNode(supervisoryNodeId))
        .isInstanceOf(NotFoundException.class);

    verifyZeroInteractions(supervisoryNodeRepository);
    verifyZeroInteractions(supervisoryNodeRedisRepository);
  }
}

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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.hamcrest.core.Every;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.repository.ProgramOrderableRepository;

@RunWith(MockitoJUnitRunner.class)
public class ProgramOrderableServiceTest {

  @Mock
  private ProgramOrderableRepository programOrderableRepository;

  private List<ProgramOrderable> programOrderableList;

  @InjectMocks
  private ProgramOrderableService programOrderableService = new ProgramOrderableService();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    programOrderableList = Lists.newArrayList(new ProgramOrderable(), new ProgramOrderable());
  }

  @Test
  public void shouldReturnAllProgramOrderables() {
    final Integer programOrderableListSize = programOrderableList.size();
    when(programOrderableRepository.findAll()).thenReturn(programOrderableList);

    List<ProgramOrderable> result = programOrderableService.findAll();

    assertEquals(Integer.valueOf(result.size()), programOrderableListSize);
  }

  @Test
  public void shouldReturnEmptyListIfNoProgramOrderablesWasFound() {
    when(programOrderableRepository.findAll()).thenReturn(Collections.emptyList());

    List<ProgramOrderable> result = programOrderableService.findAll();

    assertThat(result, is(empty()));
  }

  @Test
  public void shouldReturnTypeThatMatchesTypeOfFoundItems() {
    when(programOrderableRepository.findAll()).thenReturn(programOrderableList);

    List<ProgramOrderable> resultList = programOrderableService.findAll();
    Class<?> resultType = programOrderableService.getType();

    assertThat(resultList, Every.everyItem(instanceOf(resultType)));
  }

}
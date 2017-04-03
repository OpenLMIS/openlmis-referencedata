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

import static java.lang.String.valueOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

public class RightRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<Right> {

  @Autowired
  private RightRepository repository;

  private Right right1;
  private Right right2;

  @Override
  RightRepository getRepository() {
    return this.repository;
  }

  @Override
  Right generateInstance() {
    int instanceNumber = this.getNextInstanceNumber();
    return Right.newRight(valueOf(instanceNumber), RightType.GENERAL_ADMIN);
  }

  @Before
  public void setUp() {
    right1 = this.generateInstance();
    right2 = this.generateInstance();
    repository.save(right1);
    repository.save(right2);
  }

  @Test
  public void shouldGetFirstByNameIfExists() {
    //given
    String nameToFind = right2.getName();

    //when
    Right foundRight = repository.findFirstByName(nameToFind);

    //then
    assertEquals(right2, foundRight);
  }

  @Test
  public void shouldFindRight() {
    //given
    String nameToFind = right2.getName();
    RightType typeToFind = right2.getType();

    //when
    List<List<Right>> rightResults = Arrays.asList(
        repository.searchRights(nameToFind, typeToFind),
        repository.searchRights(null, typeToFind),
        repository.searchRights(nameToFind, null)
    );

    //then
    assertEquals(3, rightResults.size());
    assertTrue(rightResults.stream().allMatch(result -> result.contains(right2)));
  }

  @Test
  public void shouldNotFindRightIfIncorrectParametersAreProvided() {
    //given
    String actualName = right2.getName();
    String anotherName = "some other name";
    RightType actualType = right2.getType();
    RightType anotherType = RightType.SUPERVISION;

    //when
    List<List<Right>> rightResults = Arrays.asList(
        repository.searchRights(actualName, anotherType),
        repository.searchRights(anotherName, anotherType),
        repository.searchRights(anotherName, actualType),
        repository.searchRights(anotherName, null),
        repository.searchRights(null, anotherType)
    );

    //then
    assertEquals(5, rightResults.size());
    assertTrue(rightResults.stream().noneMatch(result -> result.contains(right2)));
  }

  @Test
  public void shouldNotGetFirstByNameIfDoesNotExist() {
    //given
    String nameToFind = "does not exist";

    //when
    Right foundRight = repository.findFirstByName(nameToFind);

    //then
    assertNull(foundRight);
  }

  @Test
  public void shouldAddRightAttachments() {
    //given
    right1.attach(right2);

    //when
    right1 = repository.save(right1);

    //then
    assertThat(right1.getAttachments().size(), is(1));
    assertTrue(right1.getAttachments().contains(right2));
  }

  @Test
  public void shouldUpdateRightAttachments() {
    //given
    right1.attach(right2);
    repository.save(right1);

    Right right3 = this.generateInstance();
    repository.save(right3);

    right1.clearAttachments();
    right1.attach(right3);

    //when
    right1 = repository.save(right1);

    //then
    assertThat(right1.getAttachments().size(), is(1));
    assertFalse(right1.getAttachments().contains(right2));
    assertTrue(right1.getAttachments().contains(right3));
  }

  @Test
  public void shouldRemoveRightAttachments() {
    //given
    right1.attach(right2);
    repository.save(right1);

    right1.clearAttachments();

    //when
    right1 = repository.save(right1);

    //then
    assertThat(right1.getAttachments().size(), is(0));
    assertFalse(right1.getAttachments().contains(right2));
  }
}

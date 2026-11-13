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

package org.openlmis.referencedata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.TestDataInitializer.BASE_DATA;
import static org.openlmis.referencedata.TestDataInitializer.DEMO_DATA;
import static org.openlmis.referencedata.TestDataInitializer.EDGE_CASE_DATA;
import static org.openlmis.referencedata.TestDataInitializer.FUNCTIONAL_TEST_DATA;
import static org.openlmis.referencedata.TestDataInitializer.LAYERS;
import static org.openlmis.referencedata.TestDataInitializer.PERFORMANCE_DATA;
import static org.openlmis.referencedata.TestDataInitializer.TABLES;
import static org.openlmis.referencedata.TestDataInitializer.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.referencedata.util.Resource2Db;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@SuppressWarnings("PMD.TooManyMethods")
@RunWith(MockitoJUnitRunner.Silent.class)
public class TestDataInitializerTest {

  private static final String GEOGRAPHIC_LEVELS = "referencedata.geographic_levels";
  private static final String FACILITIES = "referencedata.facilities";
  private static final String USERS = "referencedata.users";
  private static final String REFRESH_DB = "refresh-db";

  @Mock
  private Resource2Db loader;

  @Mock
  private ResourceLoader resourceLoader;

  @Mock
  private Environment environment;

  private final Map<String, Resource> resources = new HashMap<>();

  private TestDataInitializer initializer;

  @Before
  public void setUp() {
    when(resourceLoader.getResource(anyString())).thenAnswer(invocation ->
        resources.computeIfAbsent(invocation.getArgument(0), this::resource));
    initializer = new TestDataInitializer(loader, resourceLoader, environment);
  }

  @Test
  public void shouldLoadEveryTableOfTheCurrentDemoDataInOrder() throws IOException {
    activeProfiles(DEMO_DATA);
    for (String table : TABLES) {
      existing(DEMO_DATA, table);
    }

    initializer.run();

    InOrder order = inOrder(loader);
    for (String table : TABLES) {
      order.verify(loader).insertToDbFromCsv(table, resources.get(location(DEMO_DATA, table)));
    }
  }

  @Test
  public void shouldLoadBaseLayerBeforeTheSelectedLayer() throws IOException {
    activeProfiles(DEMO_DATA);
    Resource baseUsers = existing(BASE_DATA, USERS);
    Resource demoLevels = existing(DEMO_DATA, GEOGRAPHIC_LEVELS);

    initializer.run();

    InOrder order = inOrder(loader);
    order.verify(loader).insertToDbFromCsv(USERS, baseUsers);
    order.verify(loader).insertToDbFromCsv(GEOGRAPHIC_LEVELS, demoLevels);
  }

  @Test
  public void shouldSkipTablesWithoutFile() throws IOException {
    activeProfiles(DEMO_DATA);
    Resource demoFacilities = existing(DEMO_DATA, FACILITIES);

    initializer.run();

    verify(loader).insertToDbFromCsv(FACILITIES, demoFacilities);
    verify(loader).insertToDbFromCsv(anyString(), any(Resource.class));
  }

  @Test
  public void shouldNotLoadLayersWhoseProfileIsInactive() throws IOException {
    activeProfiles(BASE_DATA, REFRESH_DB);
    final Resource baseFacilities = existing(BASE_DATA, FACILITIES);
    final Resource demoFacilities = existing(DEMO_DATA, FACILITIES);
    final Resource performanceFacilities = existing(PERFORMANCE_DATA, FACILITIES);

    initializer.run();

    verify(loader).insertToDbFromCsv(FACILITIES, baseFacilities);
    verify(loader, never()).insertToDbFromCsv(FACILITIES, demoFacilities);
    verify(loader, never()).insertToDbFromCsv(FACILITIES, performanceFacilities);
  }

  @Test
  public void shouldResolveBaseLayerForEveryDataProfile() {
    for (String layer : LAYERS) {
      List<String> resolved = TestDataInitializer.resolveLayers(layer);
      assertEquals(BASE_DATA, resolved.get(0));
      assertTrue(resolved.contains(layer));
    }
  }

  @Test
  public void shouldResolveLayersInLoadOrderRegardlessOfProfileOrder() {
    assertEquals(Arrays.asList(BASE_DATA, DEMO_DATA, EDGE_CASE_DATA, PERFORMANCE_DATA),
        TestDataInitializer.resolveLayers(
            PERFORMANCE_DATA, REFRESH_DB, EDGE_CASE_DATA, DEMO_DATA, BASE_DATA));
  }

  @Test
  public void shouldRunForEveryDataProfileButNotInTestRuns() {
    Profiles condition = Profiles.of(
        TestDataInitializer.class.getAnnotation(Profile.class).value());

    for (String layer : LAYERS) {
      assertTrue(layer, condition.matches(active(layer, REFRESH_DB)));
      assertFalse(layer, condition.matches(active(layer, "test-run")));
    }
    assertFalse(condition.matches(active(REFRESH_DB, "production")));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldRejectDemoAndFunctionalTestDataTogether() {
    TestDataInitializer.resolveLayers(DEMO_DATA, FUNCTIONAL_TEST_DATA);
  }

  @Test
  public void shouldKnowTheTableOfEveryLayerFile() throws IOException {
    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    List<String> unknown = new ArrayList<>();
    int files = 0;

    for (String layer : LAYERS) {
      for (Resource file : resolver.getResources("classpath*:db/" + layer + "/*.csv")) {
        files++;
        String table = file.getFilename().replaceFirst("\\.csv$", "");
        if (!TABLES.contains(table)) {
          unknown.add(layer + "/" + file.getFilename());
        }
      }
    }

    assertTrue("Expected the demo-data files on the classpath", files > 0);
    assertEquals("Files the loader would silently skip; add their tables to TABLES",
        Collections.emptyList(), unknown);
  }

  private static Predicate<String> active(String... profiles) {
    return Arrays.asList(profiles)::contains;
  }

  private void activeProfiles(String... profiles) {
    when(environment.getActiveProfiles()).thenReturn(profiles);
  }

  private Resource existing(String layer, String table) {
    Resource resource = resources.computeIfAbsent(location(layer, table), this::resource);
    when(resource.exists()).thenReturn(true);
    return resource;
  }

  private Resource resource(String location) {
    return mock(Resource.class, location);
  }
}

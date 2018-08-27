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

package org.openlmis.referencedata.fhir;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class FhirClientFactoryTest {

  private static final String FHIR_ENABLED = "true";
  private static final String FHIR_VERSION = "DSTU2";
  private static final String FHIR_SERVER_URL = "http://localhost/fhir";
  private static final String SERVICE_URL = "http://localhost";

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  private LocationFactory locationFactory;

  private FhirClientFactory factory = new FhirClientFactory();

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(factory, "fhirEnabled", FHIR_ENABLED);
    ReflectionTestUtils.setField(factory, "fhirVersion", FHIR_VERSION);
    ReflectionTestUtils.setField(factory, "fhirServerUrl", FHIR_SERVER_URL);
    ReflectionTestUtils.setField(factory, "serviceUrl", SERVICE_URL);
    ReflectionTestUtils.setField(factory, "locationFactory", locationFactory);

    factory.afterPropertiesSet();
  }

  @Test
  public void shouldCreateFhirClientIfFeatureIsEnabled() {
    FhirClient client = factory.getObject();
    assertThat(client)
        .isNotNull()
        .isNotEqualTo(FhirClientFactory.EMPTY_CLIENT)
        .isInstanceOf(DefaultFhirClient.class);
  }

  @Test
  public void shouldCreateEmptyFhirClientIfFeatureIsDisabled() {
    ReflectionTestUtils.setField(factory, "fhirEnabled", "false");
    factory.afterPropertiesSet();

    assertThat(factory.getObject()).isEqualTo(FhirClientFactory.EMPTY_CLIENT);
  }

  @Test
  public void shouldThrowExceptionIfFhirServerUrlIsBlank() {
    ReflectionTestUtils.setField(factory, "fhirServerUrl", "");

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("The FHIR server url cannot be blank");

    factory.afterPropertiesSet();
  }

  @Test
  public void shouldThrowExceptionIfFhirVersionIsIncorrect() {
    ReflectionTestUtils.setField(factory, "fhirVersion", "TEST");

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Unsupported FHIR version: TEST");

    factory.afterPropertiesSet();
  }

  @Test
  public void shouldSupportOnlyFhirClientType() {
    assertThat(factory.getObjectType()).isEqualTo(FhirClient.class);
  }

  @Test
  public void shouldAlwaysReturnSameObject() {
    assertThat(factory.isSingleton()).isTrue();

    // verify that returned object is always the same
    assertThat(factory.getObject())
        .isEqualTo(factory.getObject())
        .isEqualTo(factory.getObject())
        .isEqualTo(factory.getObject());
  }
}

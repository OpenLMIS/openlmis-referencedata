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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.referencedata.fhir.FhirCoding.SITE;

import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.IUntypedQuery;
import java.util.UUID;
import org.assertj.core.api.Condition;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.testbuilder.FacilityDataBuilder;
import org.openlmis.referencedata.util.messagekeys.FhirMessageKeys;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class Dstu3LocationConverterStrategyTest {

  private static final String SERVICE_URL = "http://localhost";
  private static final String FULL_URL = "http://localhost/fhir/Location/345345/_history/1";

  private FhirLocation olmisLocation = FhirLocation
      .newInstance(SERVICE_URL, new FacilityDataBuilder().build());

  private Location result = new Location();

  @Mock
  private IGenericClient client;

  @Mock
  private CacheControlDirective cacheControlDirective;

  @Mock
  private CriterionBuilder criterionBuilder;

  @Mock
  private ICriterion criterion;

  @Mock
  private IUntypedQuery search;

  @Mock
  private IQuery baseQuery;

  @Mock
  private IQuery query;

  private Dstu3LocationConverterStrategy strategy;

  private Bundle emptyBundle;
  private Bundle bundle;

  @Before
  public void setUp() {
    strategy = new Dstu3LocationConverterStrategy(client, cacheControlDirective, criterionBuilder);

    emptyBundle = new Bundle();

    BundleEntryComponent entry = new BundleEntryComponent();
    entry.setFullUrl(FULL_URL);

    bundle = new Bundle();
    bundle.addEntry(entry);

    when(criterionBuilder.buildIdentifierCriterion(any(UUID.class))).thenReturn(criterion);

    when(client.search()).thenReturn(search);
    when(search.forResource(Location.class)).thenReturn(baseQuery);
    when(baseQuery.cacheControl(cacheControlDirective)).thenReturn(baseQuery);
    when(baseQuery.where(criterion)).thenReturn(baseQuery);
    when(baseQuery.returnBundle(Bundle.class)).thenReturn(query);

    when(query.execute()).thenReturn(bundle);
  }

  @Test
  public void shouldInitiateResource() {
    assertThat(strategy.initiateResource()).isInstanceOf(result.getClass());
  }

  @Test
  public void shouldSetName() {
    strategy.setName(result, olmisLocation);

    assertThat(result.getName()).isEqualTo(olmisLocation.getName());
  }

  @Test
  public void shouldSetPhysicalType() {
    strategy.setPhysicalType(result, olmisLocation);

    assertThat(result.getPhysicalType()).isNotNull();
    assertThat(result.getPhysicalType().getCoding()).hasSize(1);
    assertThat(result.getPhysicalType().getCoding().get(0))
        .hasFieldOrPropertyWithValue("system", SITE.getSystem())
        .hasFieldOrPropertyWithValue("code", SITE.getCode())
        .hasFieldOrPropertyWithValue("display", SITE.getDisplay());
  }

  @Test
  public void shouldSetPartOf() {
    strategy.setPartOf(result, olmisLocation);

    assertThat(result.getPartOf()).isNotNull();
    assertThat(result.getPartOf().getReference()).isEqualTo(FULL_URL);
  }

  @Test
  public void shouldNotSetPartOfIfInputDoesNotHaveValue() {
    ReflectionTestUtils.setField(olmisLocation, "partOf", null);

    strategy.setPartOf(result, olmisLocation);

    // getter always return an object
    assertThat(result.getPartOf()).isNotNull();
    assertThat(result.getPartOf().getReference()).isNull();
  }

  @Test
  public void shouldThrowExceptionIfRelatedLocationWasNotFound() {
    when(query.execute()).thenReturn(emptyBundle);

    assertThatThrownBy(() -> strategy.setPartOf(result, olmisLocation))
        .isInstanceOf(ValidationMessageException.class)
        .hasMessageContaining(FhirMessageKeys.ERROR_NOT_FOUND_LOCATION_FOR_RESOURCE);
  }

  @Test
  public void shouldSetIdentifier() {
    strategy.setIdentifier(result, olmisLocation);

    assertThat(result.getIdentifier()).hasSize(olmisLocation.getIdentifier().size());
    olmisLocation
        .getIdentifier()
        .forEach(identifier -> assertThat(result.getIdentifier())
            .haveExactly(1, new Condition<Identifier>() {
              @Override
              public boolean matches(Identifier value) {
                return value.getSystem().equals(identifier.getSystem())
                    && value.getValue().equals(identifier.getValue());
              }
            }));
  }

  @Test
  public void shouldAddSystemIdentifier() {
    UUID id = UUID.randomUUID();

    strategy.addSystemIdentifier(result, SERVICE_URL, id);

    assertThat(result.getIdentifier()).hasSize(1);
    assertThat(result.getIdentifier().get(0))
        .hasFieldOrPropertyWithValue("system", SERVICE_URL)
        .hasFieldOrPropertyWithValue("value", id.toString());
  }

  @Test
  public void shouldSetAlias() {
    strategy.setAlias(result, olmisLocation);

    assertThat(result.getAlias()).hasSize(olmisLocation.getAlias().size());
    olmisLocation
        .getAlias()
        .forEach(alias -> assertThat(result.getAlias())
            .haveExactly(1, new Condition<StringType>() {
              @Override
              public boolean matches(StringType value) {
                return value.toString().equals(alias);
              }
            }));
  }

  @Test
  public void shouldSetPosition() {
    strategy.setPosition(result, olmisLocation);

    assertThat(result.getPosition()).isNotNull();
    assertThat(result.getPosition().getLatitude().doubleValue())
        .isEqualTo(olmisLocation.getPosition().getLatitude());
    assertThat(result.getPosition().getLongitude().doubleValue())
        .isEqualTo(olmisLocation.getPosition().getLongitude());
  }

  @Test
  public void shouldSetDescription() {
    strategy.setDescription(result, olmisLocation);

    assertThat(result.getDescription()).isEqualTo(olmisLocation.getDescription());
  }

  @Test
  public void shouldSetStatus() {
    strategy.setStatus(result, olmisLocation);

    assertThat(result.getStatus().toCode()).isEqualTo(olmisLocation.getStatus());
  }

}

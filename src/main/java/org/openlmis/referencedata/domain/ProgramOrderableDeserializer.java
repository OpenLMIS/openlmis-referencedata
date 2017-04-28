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

package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.service.ConfigurationSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static org.openlmis.referencedata.util.ConfigurationSettingKeys.CURRENCY_CODE;

@Component
public class ProgramOrderableDeserializer extends StdDeserializer<ProgramOrderable> {

  @Autowired
  ProgramRepository programRepository;

  @Autowired
  OrderableRepository orderableRepository;

  @Autowired
  OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

  @Autowired
  private ConfigurationSettingService configurationSettingService;

  public ProgramOrderableDeserializer() {
    this(null);
  }

  public ProgramOrderableDeserializer(Class<ProgramOrderable> programOrderableClass) {
    super(programOrderableClass);
  }

  @Override
  public ProgramOrderable deserialize(JsonParser jsonParser, DeserializationContext ctxt)
      throws IOException {
    ObjectCodec objectCodec = jsonParser.getCodec();
    JsonNode node = objectCodec.readTree(jsonParser);

    Program program = programRepository.findOne(UUID.fromString(node.get("programId").asText()));
    Orderable orderable = orderableRepository.findOne(UUID.fromString(node.get("orderableId")
        .asText()));
    OrderableDisplayCategory orderableDisplayCategory = orderableDisplayCategoryRepository.findOne(
        UUID.fromString(node.get("orderableDisplayCategoryId").asText()));

    Integer dosesPerPatient = null;
    if (node.get("dosesPerPatient") != null) {
      dosesPerPatient = node.get("dosesPerPatient").asInt();
    }
    boolean active = node.get("active").asBoolean();
    boolean fullSupply = node.get("fullSupply").asBoolean();
    int displayOrder = node.get("displayOrder").asInt();

    String code = configurationSettingService.getStringValue(CURRENCY_CODE);

    Money pricePerPack = Money.of(CurrencyUnit.of(code),
        node.get("pricePerPack").asDouble());

    return ProgramOrderable.createNew(program, orderableDisplayCategory, orderable,
        dosesPerPatient, active, fullSupply, displayOrder, pricePerPack,
        CurrencyUnit.of(code));
  }
}

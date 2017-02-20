package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.openlmis.referencedata.CurrencyConfig;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.repository.OrderableDisplayCategoryRepository;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class ProgramOrderableDeserializer extends StdDeserializer<ProgramOrderable> {

  @Autowired
  ProgramRepository programRepository;

  @Autowired
  OrderableRepository orderableRepository;

  @Autowired
  OrderableDisplayCategoryRepository orderableDisplayCategoryRepository;

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

    Money pricePerPack = Money.of(CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE),
        node.get("pricePerPack").asDouble());

    return ProgramOrderable.createNew(program, orderableDisplayCategory, orderable,
        dosesPerPatient, active, fullSupply, displayOrder, pricePerPack,
        CurrencyUnit.of(CurrencyConfig.CURRENCY_CODE));
  }
}

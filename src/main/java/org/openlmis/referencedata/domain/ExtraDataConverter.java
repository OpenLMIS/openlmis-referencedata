package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.validation.constraints.NotNull;

public class ExtraDataConverter implements AttributeConverter<Map<String, String>, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @NotNull
  public String convertToDatabaseColumn(@NotNull Map<String, String> extraData) {
    try {
      return objectMapper.writeValueAsString(extraData);
    } catch (JsonProcessingException ex) {
      return null;
    }
  }

  @Override
  @NotNull
  public Map<String, String> convertToEntityAttribute(@NotNull String databaseDataAsJsonString) {
    try {
      if (databaseDataAsJsonString == null || databaseDataAsJsonString.equalsIgnoreCase("null")) {
        return null;
      } else {
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
        };
        return objectMapper.readValue(databaseDataAsJsonString, typeRef);
      }
    } catch (IOException ex) {
      return null;
    }
  }
}

package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.validation.constraints.NotNull;

public class ExtraDataConverter implements AttributeConverter<Object, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @NotNull
  public String convertToDatabaseColumn(@NotNull Object extraData) {
    try {
      return objectMapper.writeValueAsString(extraData);
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  @NotNull
  public Object convertToEntityAttribute(@NotNull String databaseDataAsJsonString) {
    try {
      if (databaseDataAsJsonString.equalsIgnoreCase("null")) {
        return null;
      } else {
        return objectMapper.readValue(databaseDataAsJsonString, Object.class);
      }
    } catch (Exception ex) {
      return null;
    }
  }
}

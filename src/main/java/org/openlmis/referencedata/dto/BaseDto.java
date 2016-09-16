package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDto {
  
  @Getter
  @Setter
  UUID id;
}

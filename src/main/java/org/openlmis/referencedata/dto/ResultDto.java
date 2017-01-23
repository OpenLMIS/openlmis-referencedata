package org.openlmis.referencedata.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ResultDto<T> {

  T result;
  
  public T getResult() {
    return result;
  }
}

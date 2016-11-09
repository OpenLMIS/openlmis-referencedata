package org.openlmis.referencedata.web;

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

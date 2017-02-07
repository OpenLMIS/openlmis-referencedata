package org.openlmis.referencedata.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.joda.money.Money;
import org.openlmis.referencedata.domain.Code;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.serializer.MoneyDeserializer;
import org.openlmis.referencedata.serializer.MoneySerializer;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProgramOrderableDto extends BaseDto
    implements ProgramOrderable.Exporter, ProgramOrderable.Importer {

  private UUID orderableId;

  private String orderableName;

  private Code orderableCode;

  private Long orderablePackSize;

  private UUID orderableDisplayCategoryId;

  private String orderableCategoryDisplayName;

  private int orderableCategoryDisplayOrder;

  private boolean active;

  private boolean fullSupply;

  private int displayOrder;

  private Integer dosesPerPatient;

  @JsonSerialize(using = MoneySerializer.class)
  @JsonDeserialize(using = MoneyDeserializer.class)
  private Money pricePerPack;

}

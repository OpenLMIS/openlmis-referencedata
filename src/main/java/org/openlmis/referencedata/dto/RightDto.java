package org.openlmis.referencedata.dto;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;

import java.util.HashSet;
import java.util.Set;

public class RightDto extends BaseDto implements Right.Exporter, Right.Importer {

  @Getter
  @Setter
  private String name;

  @Getter
  @Setter
  private RightType type;

  @Getter
  @Setter
  private String description;

  private Set<RightDto> attachments = new HashSet<>();

  @Override
  public void addAttachment(Right.Exporter rightExporter) {
    attachments.add((RightDto)rightExporter);
  }

  public Set<Right.Importer> getAttachments() {
    Set<Right.Importer> attachmentDtos = new HashSet<>();
    attachmentDtos.addAll(this.attachments);
    return attachmentDtos;
  }
}

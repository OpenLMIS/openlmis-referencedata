package org.openlmis.referencedata.dto;

import lombok.Getter;
import lombok.Setter;

import org.openlmis.referencedata.domain.Right;
import org.openlmis.referencedata.domain.RightType;

import java.util.HashSet;
import java.util.Objects;
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
  public void setAttachments(Set<Right> attachments) {
    for (Right attachment : attachments) {
      RightDto attachmentDto = new RightDto();
      attachment.export(attachmentDto);
      this.attachments.add(attachmentDto);
    }
  }
  
  @Override
  public Set<Right.Importer> getAttachments() {
    Set<Right.Importer> attachments = new HashSet<>();
    attachments.addAll(this.attachments);
    return attachments;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof RightDto)) {
      return false;
    }
    RightDto rightDto = (RightDto) obj;
    return Objects.equals(name, rightDto.name);
  }
}

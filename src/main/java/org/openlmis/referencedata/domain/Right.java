package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "rights", schema = "referencedata")
@NoArgsConstructor
@SuppressWarnings({"PMD.UnusedPrivateField"})
public class Right extends BaseEntity {
  private static final String TEXT = "text";

  @Column(nullable = false, unique = true, columnDefinition = TEXT)
  @Getter
  @Setter
  private String name;

  @Column(nullable = false, columnDefinition = TEXT)
  @Getter
  @Setter
  private RightType rightType;

  @Column(columnDefinition = TEXT)
  @Getter
  @Setter
  private String description;
  
  @OneToMany(mappedBy = "parent")
  @Getter
  private List<Right> attachments = new ArrayList<>();

  @ManyToOne
  @JoinColumn(name = "parentid")
  private Right parent;
  
  @ManyToMany(
      mappedBy = "rights",
      cascade = {CascadeType.PERSIST, CascadeType.MERGE}
  )
  private List<Role> roles;

  private Right(RightType rightType) {
    this.rightType = rightType;
  }

  public static Right ofType(RightType rightType) {
    return new Right(rightType);
  }

  /**
   * Attach other rights to this one, to create relationships between rights. The attachment is
   * one-way with this method call. The attached rights must be of the same type; only 
   * attachments of the same type are attached.
   *
   * @param attachments the rights being attached
   */
  public void attach(Right... attachments) {
    for (Right attachment : attachments) {
      if (attachment.rightType == rightType) {
        this.attachments.add(attachment);
      }
    }
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
    if (!(obj instanceof Right)) {
      return false;
    }
    Right right = (Right) obj;
    return Objects.equals(name, right.name);
  }
}
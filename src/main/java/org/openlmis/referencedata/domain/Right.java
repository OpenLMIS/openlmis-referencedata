package org.openlmis.referencedata.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
  @Enumerated(value = EnumType.STRING)
  private RightType type;

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
      mappedBy = "rights"
  )
  private List<Role> roles;

  public Right(String name, RightType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Constructor for name, type, description.
   * 
   * @param name right name
   * @param type right type
   * @param description right description
   */
  public Right(String name, RightType type, String description) {
    this.name = name;
    this.type = type;
    this.description = description;
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
      if (attachment.type == type) {
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
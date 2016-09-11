package org.openlmis.referencedata.domain;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.openlmis.referencedata.util.View;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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

  @JsonView(View.BasicInformation.class)
  @Column(nullable = false, unique = true, columnDefinition = TEXT)
  @Getter
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
  private Set<Right> attachments = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "parentid")
  private Right parent;

  @ManyToMany(
      mappedBy = "rights"
  )
  private Set<Role> roles;

  private Right(String name, RightType type) {
    this.name = name;
    this.type = type;
  }

  /**
   * Static factory method for constructing a new right with a name and type.
   *
   * @param name right name
   * @param type right type
   */
  public static Right newRight(String name, RightType type) {
    return new Right(name, type);
  }

  /**
   * Attach other rights to this one, to create relationships between rights. The attachment is
   * one-way with this method call. The attached rights must be of the same type; only attachments
   * of the same type are attached.
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

  /**
   * Copy values of attributes into new or updated Right.
   *
   * @param right Right with new values.
   */
  public void updateFrom(Right right) {
    this.name = right.getName();
    this.type = right.getType();
    this.description = right.getDescription();
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
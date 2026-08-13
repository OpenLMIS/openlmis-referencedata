/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.referencedata.domain;

import static org.openlmis.referencedata.util.messagekeys.LotMessageKeys.ERROR_LOT_CODE_INVALID_FORMAT;
import static org.openlmis.referencedata.util.messagekeys.LotMessageKeys.ERROR_LOT_CODE_TOO_LONG;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.javers.core.metamodel.annotation.TypeName;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.util.Message;

@Entity
@Table(name = "lots", schema = "referencedata")
@TypeName("Lot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lot extends BaseEntity {

  private static final int LOT_CODE_MAX_LENGTH = 20;

  // GS1 Application Identifier (10) encodable character set: digits, upper and lower case letters
  // and the GS1 invariant special characters. Kept in sync with the barcode scan parser's set.
  private static final Pattern LOT_CODE_PATTERN =
      Pattern.compile("[!\"%&'()*+,\\-./0-9:;<=>?A-Z_a-z]*");

  @Column(nullable = false, columnDefinition = "text")
  private String lotCode;

  private LocalDate expirationDate;

  private LocalDate manufactureDate;

  @ManyToOne(cascade = CascadeType.REFRESH)
  @JoinColumn(nullable = false, name = "tradeitemid")
  private TradeItem tradeItem;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  private boolean active;

  /**
   * Sets the lot code after enforcing the GS1 Application Identifier (10) contract: a maximum
   * length and the GS1 invariant character set. Enforcing it here, rather than only in the
   * pluggable {@link org.openlmis.referencedata.extension.point.LotValidator}, keeps the bound on
   * every write path, including a lot created by another service through the API. A null or empty
   * code is left to the required-field check in the validator.
   *
   * @param lotCode the lot code to set
   * @throws ValidationMessageException if the code is too long or has disallowed characters
   */
  public void setLotCode(String lotCode) {
    if (lotCode != null) {
      if (lotCode.length() > LOT_CODE_MAX_LENGTH) {
        throw new ValidationMessageException(
            new Message(ERROR_LOT_CODE_TOO_LONG, LOT_CODE_MAX_LENGTH));
      }
      if (!LOT_CODE_PATTERN.matcher(lotCode).matches()) {
        throw new ValidationMessageException(new Message(ERROR_LOT_CODE_INVALID_FORMAT));
      }
    }
    this.lotCode = lotCode;
  }

  /**
   * Creates new lot object based on data from {@link Importer} and tradeItem argument.
   *
   * @param importer instance of {@link Importer}
   * @param tradeItem tradeItem to set.
   * @return new instance of facility.
   */
  public static Lot newLot(Importer importer, TradeItem tradeItem) {
    Lot lot = new Lot();
    lot.setId(importer.getId());
    lot.setLotCode(importer.getLotCode());
    lot.setActive(importer.isActive());
    if (importer.getExpirationDate() != null) {
      lot.setExpirationDate(importer.getExpirationDate());
    }
    if (importer.getManufactureDate() != null) {
      lot.setManufactureDate(importer.getManufactureDate());
    }
    lot.setTradeItem(tradeItem);
    return lot;
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setId(id);
    exporter.setLotCode(lotCode);
    exporter.setTradeItemId(tradeItem.getId());
    exporter.setActive(active);
    if (expirationDate != null) {
      exporter.setExpirationDate(expirationDate);
    }
    if (manufactureDate != null) {
      exporter.setManufactureDate(manufactureDate);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Lot)) {
      return false;
    }
    Lot lot = (Lot) obj;
    return Objects.equals(lotCode, lot.lotCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lotCode);
  }

  public interface Exporter {
    void setId(UUID id);

    void setLotCode(String lotCode);

    void setActive(boolean active);

    void setTradeItemId(UUID tradeItemId);

    void setExpirationDate(LocalDate expirationDate);

    void setManufactureDate(LocalDate manufactureDate);
  }

  public interface Importer {
    UUID getId();

    String getLotCode();

    boolean isActive();

    UUID getTradeItemId();

    LocalDate getExpirationDate();

    LocalDate getManufactureDate();
  }
}

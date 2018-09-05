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

package org.openlmis.referencedata.util;

import java.math.BigDecimal;
import java.util.Properties;
import org.hibernate.SessionFactory;
import org.hibernate.usertype.ParameterizedType;
import org.jadira.usertype.moneyandcurrency.joda.columnmapper.BigDecimalColumnMoneyMapper;
import org.jadira.usertype.moneyandcurrency.joda.util.CurrencyUnitConfigured;
import org.jadira.usertype.spi.shared.AbstractSingleColumnUserType;
import org.jadira.usertype.spi.shared.ColumnMapper;
import org.jadira.usertype.spi.shared.IntegratorConfiguredType;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

public class CustomSingleColumnMoneyUserType<T, J, C
    extends ColumnMapper<Money, BigDecimal>>
    extends AbstractSingleColumnUserType<Money, BigDecimal, BigDecimalColumnMoneyMapper>
    implements ParameterizedType, IntegratorConfiguredType {

  private Properties parameterValues;
  private CurrencyUnit currencyUnit;

  @Override
  public void setParameterValues(Properties parameters) {
    this.parameterValues = parameters;
  }

  protected Properties getParameterValues() {
    return parameterValues;
  }

  @Override
  public void applyConfiguration(SessionFactory sessionFactory) {

    CurrencyUnitConfigured columnMapper = (CurrencyUnitConfigured) getColumnMapper();
    if (currencyUnit == null) {
      String currencyString = null;
      if (parameterValues != null) {
        currencyString = parameterValues.getProperty("currencyCode");
      }
      if (currencyString == null) {
        currencyString = System.getenv("CURRENCY_CODE");
      }
      if (currencyString != null) {
        currencyUnit = CurrencyUnit.of(currencyString);
      } else {
        throw new IllegalStateException(getClass().getSimpleName()
            + " requires currencyCode to be defined as a parameter, or"
            + " the jadira.usertype.currencyCode Hibernate property to be defined");
      }
    }
    columnMapper.setCurrencyUnit(currencyUnit);
  }
}

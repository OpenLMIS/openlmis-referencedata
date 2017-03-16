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

package org.openlmis.referencedata.util.messagekeys;

public abstract class SystemMessageKeys extends MessageKeys {
  public static final String ERROR_UNAUTHORIZED = join(SERVICE_ERROR, UNAUTHORIZED);
  public static final String ERROR_UNAUTHORIZED_GENERIC = join(ERROR_UNAUTHORIZED, GENERIC);

  public static final String ACCOUNT_CREATED_EMAIL_SUBJECT = join(ACCOUNT, CREATED, EMAIL, SUBJECT);
  public static final String PASSWORD_RESET_EMAIL_BODY = join(PASSWORD, RESET, EMAIL, BODY);
}

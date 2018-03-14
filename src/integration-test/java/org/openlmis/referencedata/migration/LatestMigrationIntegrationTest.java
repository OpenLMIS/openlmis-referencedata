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

package org.openlmis.referencedata.migration;

import org.junit.Test;

public class LatestMigrationIntegrationTest extends BaseMigrationIntegrationTest {

  @Test
  public void shouldMigrateToLatestVersion() {
    // the migration process is in the FlywayTestExecutionListener which is executed before all
    // tests if there will be any problem with migrations there will be exception from that class

    // This class is used to ensure that all migrations will be applied to database that is why
    // this class does not have @FlywayTarget annotation
  }
}

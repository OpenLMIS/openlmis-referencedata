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

package org.openlmis.referencedata;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("performance-test")
public class TestDataInitializer implements CommandLineRunner {
  @Value(value = "classpath:db/testData/referencedata.users.sql")
  private Resource usersResource;

  @Value(value = "classpath:db/testData/referencedata.facilities.sql")
  private Resource facilitiesResource;

  @Autowired
  JdbcTemplate template;
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  /**
   * Initializes test data.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry(args);

    updateDbFromResource(usersResource);
    updateDbFromResource(facilitiesResource);

    XLOGGER.exit();
  }

  private void updateDbFromResource(Resource resource) throws IOException {
    XLOGGER.entry(resource.getDescription());
    List<String> sqlLines = resourceToStrings(resource);
    updateDbFromSqlStrings(sqlLines);
    XLOGGER.exit();
  }

  private List<String> resourceToStrings(final Resource resource) throws IOException {
    XLOGGER.entry(resource.getDescription());
    List<String> lines = new BufferedReader(
        new InputStreamReader(resource.getInputStream())).lines().collect(Collectors.toList());
    XLOGGER.exit("SQL lines read: " + lines.size());
    return lines;
  }

  private void updateDbFromSqlStrings(final List<String> sqlLines) {
    XLOGGER.entry();
    int[] updateCounts = template.batchUpdate(sqlLines.toArray(new String[sqlLines.size()]));
    XLOGGER.exit("Total db updates: " + Arrays.stream(updateCounts).sum());
  }
}

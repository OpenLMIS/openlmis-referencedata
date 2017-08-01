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

import static java.util.stream.Collectors.joining;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("performance-data")
public class TestDataInitializer implements CommandLineRunner {
  private static final String PERF_DATA_PATH = "classpath:db/performance-data/";

  @Value(value = PERF_DATA_PATH + "referencedata.users.sql")
  private Resource usersResource;

  @Value(value = PERF_DATA_PATH + "facilities.csv")
  private Resource facilitiesResource;

  @Value(value = PERF_DATA_PATH + "orderables.csv")
  private Resource orderablesResource;

  @Autowired
  JdbcTemplate template;
  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(TestDataInitializer.class);

  /**
   * Initializes test data.
   * @param args command line arguments
   */
  public void run(String... args) throws IOException {
    XLOGGER.entry();

    updateDbFromSql(usersResource);
    insertToDbFromCsv("referencedata.facilities", facilitiesResource);
    insertToDbFromCsv("referencedata.orderables", orderablesResource);

    XLOGGER.exit();
  }

  private void updateDbFromSql(Resource resource) throws IOException {
    XLOGGER.entry(resource.getDescription());
    List<String> sqlLines = resourceToStrings(resource);
    updateDbFromSqlStrings(sqlLines);
    XLOGGER.exit();
  }

  private void insertToDbFromCsv(String tableName, Resource resource) throws IOException {
    XLOGGER.entry(tableName, resource);
    insertToDbFromBatchedPair(tableName, resourceCsvToBatchedPair(resource));
    XLOGGER.exit();
  }

  /*
   converts a Resource into a List of Strings - used when those strings are direct SQL
   */
  private List<String> resourceToStrings(final Resource resource) throws IOException {
    XLOGGER.entry(resource.getDescription());
    List<String> lines = new BufferedReader(
        new InputStreamReader(resource.getInputStream())).lines().collect(Collectors.toList());
    XLOGGER.exit("SQL lines read: " + lines.size());
    return lines;
  }

  /*
   converts a Resource which is a CSV, into a Pair where Pair.left is the SQL column names,
   and Pair.right is the rows of data which go into those columns (each row is an array, the array
   matches the order of the columns
   */
  private Pair<List<String>, List<Object[]>> resourceCsvToBatchedPair(final Resource resource)
      throws IOException {
    XLOGGER.entry(resource.getDescription());

    // parse CSV
    CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(
        new InputStreamReader(resource.getInputStream()));

    // read header row
    MutablePair<List<String>, List<Object[]>> readData = new MutablePair<>();
    readData.setLeft( new ArrayList<>( parser.getHeaderMap().keySet() ) );
    XLOGGER.info("Read header: " + readData.getLeft() );

    // read data rows
    List<Object[]> rows = new ArrayList<>();
    for ( CSVRecord record : parser.getRecords() ) {
      if ( ! record.isConsistent() ) {
        throw new IllegalArgumentException("CSV record inconsistent: " + record);
      }

      List theRow = IteratorUtils.toList(record.iterator());
      rows.add( theRow.toArray() );
    }
    readData.setRight(rows);

    XLOGGER.exit("Records read: " + readData.getRight().size());
    return readData;
  }

  /*
   runs the list of SQL strings directly on the database - could be insert / update
   */
  private void updateDbFromSqlStrings(final List<String> sqlLines) {
    XLOGGER.entry();
    int[] updateCounts = template.batchUpdate(sqlLines.toArray(new String[sqlLines.size()]));
    XLOGGER.exit("Total db updates: " + Arrays.stream(updateCounts).sum());
  }

  /*
   runs a sql insert with the contents of a Pair as defined in resourceCsvToBatchedPair against
   the given table name (which should include the schema name)
   */
  private void insertToDbFromBatchedPair(String tableName,
                                         Pair<List<String>, List<Object[]>> dataWithHeader) {
    XLOGGER.entry(tableName);

    String columnDesc = dataWithHeader.getLeft()
        .stream()
        .collect(joining(","));
    String valueDesc = dataWithHeader.getLeft()
        .stream()
        .map(s -> "?")
        .collect((joining(",")));
    String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
        tableName,
        columnDesc,
        valueDesc);
    XLOGGER.info("Insert SQL: " + insertSql);

    List<Object[]> data = dataWithHeader.getRight();
    data.forEach(e -> XLOGGER.info(tableName + ": " + Arrays.toString(e)));
    int[] updateCount = template.batchUpdate(insertSql, data);

    XLOGGER.exit("Total " + tableName + " inserts: " + Arrays.stream(updateCount).sum());
  }
}
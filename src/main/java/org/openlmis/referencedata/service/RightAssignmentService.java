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

package org.openlmis.referencedata.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.openlmis.referencedata.dto.RightAssignmentDto;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.util.StreamUtils;

/**
 * Service responsible for the asynchronous regeneration of user right assignments.
 * * <p>This implementation uses a "Shadow Table" strategy: it populates
 * a temporary table in the background and atomically swaps it with the live table
 * upon completion, ensuring users never experience missing permissions.
 */
@Service
public class RightAssignmentService {

  private static final XLogger XLOGGER = XLoggerFactory.getXLogger(RightAssignmentService.class);

  // Constants
  private static final String RIGHT_NAME = "rightname";
  private static final String USER_ID = "userid";
  private static final String FACILITY_ID = "facilityid";
  private static final String PROGRAM_ID = "programid";
  private static final String SUPERVISORY_NODE_ID = "supervisorynodeid";
  private static final int BATCH_SIZE = 5000;

  // SQL Resources
  private final String rightAssignmentsSql;
  private final String nodeProgramFacilitySql;
  private final JdbcTemplate template;

  @Autowired
  @Lazy
  private RightAssignmentService self;

  /**
   * Constructs the RightAssignmentService with required dependencies.
   * Loads SQL resources from the classpath during initialization.
   *
   * @param template               the JdbcTemplate used for database operations
   * @param rightAssignmentsRes    the SQL resource for fetching raw assignments
   * @param nodeProgramFacilitySql the SQL resource for fetching facilities for snode program pairs
   */
  public RightAssignmentService(JdbcTemplate template,
      @Value("classpath:db/right-assignments/get_right_assignments.sql")
      Resource rightAssignmentsRes,
      @Value("classpath:db/right-assignments/get_all_node_facility_program_mappings.sql")
      Resource nodeProgramFacilitySql) {

    this.template = template;
    this.rightAssignmentsSql = resourceToString(rightAssignmentsRes);
    this.nodeProgramFacilitySql = resourceToString(nodeProgramFacilitySql);
  }

  /**
   * Asynchronously regenerates right assignments by fetching them from DB,
   * expanding by supervisory nodes hierarchy, and updating.
   *
   * @return a Future representing the completion of the async task
   */
  @Async("rightAssignmentTaskExecutor")
  public Future<Void> regenerateRightAssignments() {
    Profiler profiler = new Profiler("REGENERATE_RIGHT_ASSIGNMENTS");
    profiler.setLogger(XLOGGER);

    StopWatch stopWatch = new StopWatch("Right Assignment Regeneration");

    XLOGGER.entry();
    XLOGGER.info("Starting right assignment regeneration...");

    try {
      profiler.start("GET_RIGHT_ASSIGNMENTS");
      stopWatch.start("Get Right Assignments From DB");

      List<RightAssignmentDto> dbRightAssignments = getRightAssignmentsFromDb();

      stopWatch.stop();
      XLOGGER.debug("Fetched {} assignments in {} ms", dbRightAssignments.size(),
          stopWatch.getLastTaskTimeMillis());

      profiler.start("EXPAND_BY_SUPERVISORY_NODES");
      stopWatch.start("Expand By Supervisory Nodes & Convert");

      List<Object[]> rowsToInsert = convertForInsert(dbRightAssignments);

      stopWatch.stop();
      XLOGGER.debug("Converted to {} rows (expanded) in {} ms", rowsToInsert.size(),
          stopWatch.getLastTaskTimeMillis());


      profiler.start("BATCH_INSERT_INTO_DB");
      stopWatch.start("Batch Insert into DB");

      self.updateDatabase(rowsToInsert, profiler);

      stopWatch.stop();
      XLOGGER.debug("Database update complete in {} ms", stopWatch.getLastTaskTimeMillis());

      profiler.start("ANALYZE_TABLE");
      stopWatch.start("Analyze Table");

      template.execute("ANALYZE referencedata.right_assignments;");

      stopWatch.stop();

    } catch (Exception e) {
      XLOGGER.error("Unexpected system error during right regeneration. Total time: {} ms",
          stopWatch.getTotalTimeMillis(), e);
    } finally {
      XLOGGER.exit();
      profiler.stop().log();

      if (XLOGGER.isDebugEnabled()) {
        XLOGGER.debug(stopWatch.prettyPrint());
      } else {
        XLOGGER.info("Regeneration finished. Total time: {} ms",
            stopWatch.getTotalTimeMillis());
      }
    }

    return new AsyncResult<>(null);
  }

  /**
   * Performs a replacement of the {@code referencedata.right_assignments} table
   * using a "Shadow Table" strategy.
   *
   * <p>The method replaces the entire table contents by:
   * <ul>
   *   <li>Creating a temporary shadow table with the same schema and constraints</li>
   *   <li>Bulk-inserting the new dataset into the shadow table</li>
   *   <li>Swapping the shadow table with the live table (DROP + RENAME)</li>
   *   <li>Updating table statistics (ANALYZE)</li>
   * </ul>
   *
   * @param rowsToInsert list of right assignments representing the full replacement dataset
   * @param profiler     profiler instance for performance tracking
   */
  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void updateDatabase(List<Object[]> rowsToInsert, Profiler profiler) {
    XLOGGER.info("Starting the zero-downtime bulk update...");

    profiler.start("DB_CREATE_SHADOW_TABLE");
    template.execute("CREATE TABLE referencedata.right_assignments_new "
        + "(LIKE referencedata.right_assignments INCLUDING ALL)");

    profiler.start("DB_BATCH_INSERT_ROWS");
    template.batchUpdate("INSERT INTO referencedata.right_assignments_new "
            + "(id, userid, rightname, facilityid, programid) VALUES (?, ?, ?, ?, ?)",
        rowsToInsert, BATCH_SIZE,
        (PreparedStatement ps, Object[] row) -> {
          ps.setObject(1, row[0]);
          ps.setObject(2, row[1]);
          ps.setObject(3, row[2]);
          ps.setObject(4, row[3]);
          ps.setObject(5, row[4]);
        });

    profiler.start("DB_SWAP_TABLES");
    template.execute("DROP TABLE referencedata.right_assignments");
    template.execute("ALTER TABLE referencedata.right_assignments_new "
        + "RENAME TO right_assignments");

    template.execute("ANALYZE referencedata.right_assignments");
    XLOGGER.info("The bulk update swap complete.");
  }

  /**
   * Retrieves raw right assignment records from the database and maps them into DTOs.
   *
   * @return a list of {@link RightAssignmentDto} objects representing the current assignment data
   */
  private List<RightAssignmentDto> getRightAssignmentsFromDb() {
    return template.query(rightAssignmentsSql, (rs, rowNum) -> {
      RightAssignmentDto dto = new RightAssignmentDto();

      dto.setUserId(UUID.fromString(rs.getString(USER_ID)));
      dto.setRightName(rs.getString(RIGHT_NAME));


      String facilityIdStr = rs.getString(FACILITY_ID);
      if (facilityIdStr != null) {
        dto.setFacilityId(UUID.fromString(facilityIdStr));
      }

      String programIdStr = rs.getString(PROGRAM_ID);
      if (programIdStr != null) {
        dto.setProgramId(UUID.fromString(programIdStr));
      }

      String supervisoryNodeIdStr = rs.getString(SUPERVISORY_NODE_ID);
      if (supervisoryNodeIdStr != null) {
        dto.setSupervisoryNodeId(UUID.fromString(supervisoryNodeIdStr));
      }

      return dto;
    });
  }

  /**
   * Optimized conversion that avoids N+1 queries by pre-fetching the data.
   *
   * @param rightAssignments the initial list of assignments from the DB
   * @return a list of object arrays ready for batch insertion
   */
  protected List<Object[]> convertForInsert(List<RightAssignmentDto> rightAssignments) {
    Map<String, List<UUID>> nodeProgramFacilitiesCache = loadNodeProgramFacilitiesCache();
    Set<RightAssignmentDto> uniqueAssignments = new HashSet<>();

    for (RightAssignmentDto dto : rightAssignments) {
      if (dto.getSupervisoryNodeId() != null) {
        String key = generateCacheKey(dto.getSupervisoryNodeId(), dto.getProgramId());
        List<UUID> facilityIds = nodeProgramFacilitiesCache
            .getOrDefault(key, Collections.emptyList());

        for (UUID facilityId : facilityIds) {
          uniqueAssignments.add(new RightAssignmentDto(
              dto.getUserId(),
              dto.getRightName(),
              facilityId,
              dto.getProgramId(),
              null
          ));
        }
      } else {
        uniqueAssignments.add(dto);
      }
    }

    return uniqueAssignments.stream()
        .map(dto -> createRowArray(dto, dto.getFacilityId()))
        .collect(Collectors.toList());
  }

  /**
   * Loads the complete supervisory node hierarchy into memory to optimize performance.
   *
   * <p>
   * This method executes a single bulk SQL query to retrieve every valid combination of
   * Supervisory Node, Program, and Facility. It groups these results into a Map to allow
   * O(1) lookups during the expansion phase, instead of the N+1 query problem.
   * </p>
   *
   * @return a Map where the key is a composite string of NodeID and ProgramID,
   *         and the value is a List of facility UUIDs supervised by that node.
   */
  private Map<String, List<UUID>> loadNodeProgramFacilitiesCache() {
    return template.query(nodeProgramFacilitySql, (ResultSet rs) -> {
      Map<String, List<UUID>> cache = new HashMap<>();
      while (rs.next()) {
        UUID nodeId = rs.getObject(SUPERVISORY_NODE_ID, UUID.class);
        UUID programId = rs.getObject(PROGRAM_ID, UUID.class);
        UUID facilityId = rs.getObject(FACILITY_ID, UUID.class);

        String key = generateCacheKey(nodeId, programId);
        cache.computeIfAbsent(key, k -> new ArrayList<>()).add(facilityId);
      }
      return cache;
    });
  }

  /**
   * Generates a composite cache key for the supervisory node and program.
   *
   * @param nodeId    the UUID of the supervisory node
   * @param programId the UUID of the program (can be null)
   * @return a unique string key for map lookups
   */
  private String generateCacheKey(UUID nodeId, UUID programId) {
    return nodeId + "_" + (programId != null ? programId : "null");
  }

  /**
   * Creates an object array suitable for JDBC batch insertion.
   *
   * @param dto        the data source
   * @param facilityId the facility UUID
   * @return an array of objects for the PreparedStatement
   */
  private Object[] createRowArray(RightAssignmentDto dto, UUID facilityId) {
    return new Object[] {
        UUID.randomUUID(),
        dto.getUserId(),
        dto.getRightName(),
        facilityId,
        dto.getProgramId()
    };
  }

  /**
   * Reads the content of a Spring Resource into a String using the default charset.
   * This is used during service initialization to load SQL files from the classpath.
   *
   * @param resource the Spring Resource to read (must not be null)
   * @return the content of the resource as a String
   */
  private static String resourceToString(Resource resource) {
    String str = "";
    try (InputStream is = resource.getInputStream()) {
      str = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
    } catch (IOException ioe) {
      XLOGGER.warn("Could not load SQL resource: {}", ioe.getMessage());
    }
    return str;
  }
}

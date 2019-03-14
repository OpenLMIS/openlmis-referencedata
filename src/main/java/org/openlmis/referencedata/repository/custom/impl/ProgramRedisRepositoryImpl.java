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

package org.openlmis.referencedata.repository.custom.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.repository.custom.ProgramRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProgramRedisRepositoryImpl implements ProgramRedisRepository {

  private static final String HASH_KEY = "SUPERVISORY_NODE";

  private RedisTemplate redisTemplate;
  private HashOperations hashOperations;
  private ObjectMapper mapper = new ObjectMapper();

  @Autowired
  public ProgramRedisRepositoryImpl(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @PostConstruct
  private void init() {
    hashOperations = redisTemplate.opsForHash();
  }

  @Override
  public boolean existsInCache(UUID programId) {
    return hashOperations.hasKey(HASH_KEY, programId.toString());
  }

  @Override
  public Program findById(UUID programId) {
    Map<Object, Object> found = findAll();
    return mapper.convertValue(found.get(programId.toString()), Program.class);
  }

  @Override
  public Map<Object,Object> findAll() {
    return this.redisTemplate.opsForHash().entries(HASH_KEY);
  }

  @Override
  public void save(Program program) {
    if (null != program) {
      hashOperations.put(HASH_KEY, program.getId(), program);
    }
  }
}

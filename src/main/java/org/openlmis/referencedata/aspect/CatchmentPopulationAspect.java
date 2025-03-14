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

package org.openlmis.referencedata.aspect;

import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.openlmis.referencedata.domain.GeographicZone;
import org.openlmis.referencedata.repository.GeographicZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CatchmentPopulationAspect {

  @Autowired private GeographicZoneRepository geographicZoneRepository;

  @Value("${referencedata.catchmentPopulationAutoCalc.enabled}")
  private boolean catchmentPopulationAutoCalc;

  /**
   * Advice executed after the `save` method of the `GeographicZoneRepository` is called.
   *
   * @param joinPoint the join point providing access to the method arguments,
   *                  specifically the `GeographicZone` object being saved.
   */
  @AfterReturning(
      "execution(* org.openlmis.referencedata.repository.GeographicZoneRepository.save(*))")
  public void afterGeographicZoneSaveReturningAdvice(JoinPoint joinPoint) {
    final GeographicZone updatedGeoZone = (GeographicZone) joinPoint.getArgs()[0];
    updateCatchmentPopulation(updatedGeoZone.getParent());
  }

  /**
   * Advice executed after the `saveAll` method of the `GeographicZoneRepository` is called.
   *
   * @param joinPoint the join point providing access to the method arguments,
   *                  specifically the list of `GeographicZone` objects being saved.
   */
  @AfterReturning(
      "execution(* org.openlmis.referencedata.repository.GeographicZoneRepository.saveAll(*))")
  public void afterGeographicZoneSaveAllReturningAdvice(JoinPoint joinPoint) {
    final Iterable<GeographicZone> updatedGeoZones = (Iterable) joinPoint.getArgs()[0];
    Set<GeographicZone> uniqueParents = new HashSet<>();
    updatedGeoZones.forEach(updatedGeoZone -> {
      if (updatedGeoZone.getParent() != null) {
        uniqueParents.add(updatedGeoZone.getParent());
      }
    });
    uniqueParents.forEach(this::updateCatchmentPopulation);
  }

  private void updateCatchmentPopulation(GeographicZone parent) {
    if (catchmentPopulationAutoCalc && parent != null) {
      Integer totalCatchmentPopulation = geographicZoneRepository
          .sumCatchmentPopulationByParent(parent);
      parent.setCatchmentPopulation(totalCatchmentPopulation);
      geographicZoneRepository.save(parent);
    }
  }
}

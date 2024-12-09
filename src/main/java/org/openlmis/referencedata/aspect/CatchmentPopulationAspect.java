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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CatchmentPopulationAspect {

  @Value("${referencedata.catchmentPopulationAutoCalc.enabled}")
  private boolean catchmentPopulationAutoCalc;

  @AfterReturning(
      "execution(* org.openlmis.referencedata.repository.GeographicZoneRepository.save(*))")
  public void afterGeographicZoneSaveReturningAdvice(JoinPoint joinPoint) {
    final GeographicZone updatedGeoZone = (GeographicZone) joinPoint.getArgs()[0];
    calculateCatchmentPopulation(updatedGeoZone.getParent());
  }

  @AfterReturning(
      "execution(* org.openlmis.referencedata.repository.GeographicZoneRepository.saveAll(*))")
  public void afterGeographicZoneSaveAllReturningAdvice(JoinPoint joinPoint) {
    final Iterable<GeographicZone> updatedGeoZones = (Iterable) joinPoint.getArgs()[0];

    final Set<GeographicZone> uniqueParents = new HashSet<>();
    updatedGeoZones.forEach(updatedGeoZone -> uniqueParents.add(updatedGeoZone.getParent()));

    uniqueParents.forEach(this::calculateCatchmentPopulation);
  }

  private void calculateCatchmentPopulation(GeographicZone parent) {
    if (parent == null || !catchmentPopulationAutoCalc) {
      // No parent or feature is disabled
      return;
    }

    // TODO: calc parent only, save will trigger calculate for the parent's parent
  }
}

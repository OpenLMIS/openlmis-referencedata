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

import org.springframework.context.ApplicationEvent;

/**
 * Published when an admin operation has changed an entity that feeds the right_assignments cache
 * (roles, facilities, supervisory nodes, requisition groups). Handled by a transactional event
 * listener bound to the AFTER_COMMIT phase, so regeneration runs only after the surrounding
 * transaction commits, avoiding a stale read of pre-commit entity state.
 */
public class RegenerateRightAssignmentsEvent extends ApplicationEvent {

  public RegenerateRightAssignmentsEvent(Object source) {
    super(source);
  }
}

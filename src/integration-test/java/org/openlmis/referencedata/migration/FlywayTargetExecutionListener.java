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

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

/**
 * This class is used to set the target version up to which Flyway should consider migrations. It
 * uses {@link FlywayTarget} annotation details. If annotation is not set Flyway will apply all
 * migrations.
 */
public class FlywayTargetExecutionListener
    extends AbstractTestExecutionListener {

  @Override
  public void beforeTestClass(final TestContext testContext) {
    final FlywayTarget annotation = AnnotationUtils
        .findAnnotation(testContext.getTestClass(), FlywayTarget.class);

    if (null != annotation) {
      testContext
          .getApplicationContext()
          .getBean(Flyway.class)
          .setTarget(MigrationVersion.fromVersion(annotation.value()));
    }
  }

}

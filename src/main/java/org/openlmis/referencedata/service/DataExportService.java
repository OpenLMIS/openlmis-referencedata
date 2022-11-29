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
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.dto.OrderableCsvModel;
import org.openlmis.referencedata.exception.ValidationMessageException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.util.messagekeys.MessageKeys;
import org.openlmis.referencedata.web.csv.format.CsvFormatter;
import org.openlmis.referencedata.web.csv.model.ModelClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataExportService {

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private CsvFormatter csvFormatter;

  /**
   * Parses orderables data into the csv model.
   *
   * @param output input stream of csv file
   */
  public void generateOrderablesCsv(OutputStream output) throws IOException {

    List<Orderable> orderables = orderableRepository.findAll();

    List<OrderableCsvModel> items = toOrderableCsvDto(orderables);

    try {
      csvFormatter.process(
              output, new ModelClass(OrderableCsvModel.class), items);
    } catch (IOException ex) {
      throw new ValidationMessageException(ex, MessageKeys.ERROR_IO, ex.getMessage());
    }

  }

  private List<OrderableCsvModel> toOrderableCsvDto(Iterable<Orderable> items) {
    return StreamSupport
            .stream(items.spliterator(), false)
            .map(this::toOrderableCsvDto)
            .collect(Collectors.toList());
  }

  private OrderableCsvModel toOrderableCsvDto(Orderable orderable) {
    OrderableCsvModel dto = new OrderableCsvModel();
    orderable.export(dto);
    return dto;
  }

}



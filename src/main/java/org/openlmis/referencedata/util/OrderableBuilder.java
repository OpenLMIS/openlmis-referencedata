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

package org.openlmis.referencedata.util;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.openlmis.referencedata.domain.Orderable;
import org.openlmis.referencedata.domain.OrderableChild;
import org.openlmis.referencedata.domain.PriceChange;
import org.openlmis.referencedata.domain.Program;
import org.openlmis.referencedata.domain.ProgramOrderable;
import org.openlmis.referencedata.domain.User;
import org.openlmis.referencedata.dto.ProgramOrderableDto;
import org.openlmis.referencedata.exception.NotFoundException;
import org.openlmis.referencedata.repository.OrderableRepository;
import org.openlmis.referencedata.repository.ProgramRepository;
import org.openlmis.referencedata.repository.UserRepository;
import org.openlmis.referencedata.service.AuthenticationHelper;
import org.openlmis.referencedata.util.messagekeys.UserMessageKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderableBuilder {

  @Autowired
  private ProgramRepository programRepository;

  @Autowired
  private OrderableRepository orderableRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AuthenticationHelper authenticationHelper;

  /**
   * Creates new instance based on data from {@link Orderable.Importer}.
   *
   * @param importer instance of {@link Orderable.Importer}
   * @return new instance of Orderable.
   */
  public Orderable newOrderable(Orderable.Importer importer, Orderable persistedOrderable) {
    Orderable orderable = (persistedOrderable == null) ? Orderable.newInstance(importer)
        : Orderable.updateFrom(persistedOrderable, importer);

    if (!isEmpty(importer.getPrograms())) {
      Map<UUID, Program> programs = importer
          .getPrograms()
          .stream()
          .map(item -> programRepository.findById(item.getProgramId()).orElse(null))
          .filter(Objects::nonNull)
          .collect(Collectors.toMap(Program::getId, program -> program, (id1, id2) -> id1));

      List<ProgramOrderable> programOrderables = importer
          .getPrograms()
          .stream()
          .map(item -> {
            Program program = programs.get(item.getProgramId());

            ProgramOrderable programOrderable = ProgramOrderable.newInstance(item);
            programOrderable.setProgram(program);
            programOrderable.setProduct(orderable);

            setPriceChanges(persistedOrderable, item, programOrderable, program);
            return programOrderable;
          })
          .collect(Collectors.toList());

      orderable.setProgramOrderables(programOrderables);
    }

    if (!isEmpty(importer.getChildren())) {
      setChildren(importer, orderable);
    }

    return orderable;
  }

  private void setPriceChanges(Orderable persistedOrderable, ProgramOrderableDto item,
      ProgramOrderable programOrderable, Program program) {
    List<PriceChange> priceChanges = getPreviousPriceChanges(item, programOrderable);

    if (persistedOrderable != null) {
      boolean priceHasChanged = true;
      Money newPrice = programOrderable.getPricePerPack();
      if (persistedOrderable.getProgramOrderable(program) != null) {
        Money currentPrice = persistedOrderable.getProgramOrderable(program).getPricePerPack();
        priceHasChanged = !currentPrice.equals(newPrice);
      }

      if (newPrice == null) {
        String currencyString = defaultIfBlank(System.getenv("CURRENCY_CODE"), "USD");
        CurrencyUnit currencyUnit = CurrencyUnit.of(currencyString);
        newPrice = Money.zero(currencyUnit);
        programOrderable.setPricePerPack(newPrice);
      }

      if (priceHasChanged) {
        addPriceChange(programOrderable, newPrice, priceChanges);
      }
    }

    programOrderable.setPriceChanges(priceChanges);
  }

  private List<PriceChange> getPreviousPriceChanges(ProgramOrderableDto item,
      ProgramOrderable programOrderable) {
    return item.getPriceChanges().stream()
        .map(priceChange -> {
          User author = userRepository.findById(priceChange.getAuthor().getId())
              .orElseThrow(() -> new NotFoundException(UserMessageKeys.ERROR_NOT_FOUND));
          PriceChange priceChangeItem = PriceChange.newInstance(priceChange, author);
          priceChangeItem.setProgramOrderable(programOrderable);
          return priceChangeItem;
        })
        .collect(Collectors.toList());
  }

  private void addPriceChange(ProgramOrderable programOrderable, Money newPrice,
      List<PriceChange> priceChanges) {
    PriceChange priceChange = new PriceChange();
    priceChange.setAuthor(authenticationHelper.getCurrentUser());
    priceChange.setPrice(newPrice);
    priceChange.setOccurredDate(ZonedDateTime.now());
    priceChange.setProgramOrderable(programOrderable);

    priceChanges.add(priceChange);
  }

  private void setChildren(Orderable.Importer importer, Orderable orderable) {
    List<UUID> uuids = importer.getChildren()
        .stream()
        .map(item -> item.getOrderable().getId())
        .collect(Collectors.toList());

    Map<UUID, Orderable> childrenOrderables = orderableRepository
        .findAllLatestByIds(uuids, null)
        .getContent()
        .stream()
        .collect(Collectors.toMap(Orderable::getId, o -> o));

    Set<OrderableChild> children = importer.getChildren().stream().map(
        item -> {
          Orderable child = childrenOrderables.get(item.getOrderable().getId());
          return OrderableChild.newInstance(orderable, child, item.getQuantity());
        }).collect(Collectors.toSet());
    orderable.setChildren(children);
  }

}

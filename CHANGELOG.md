4.0.0 / 2017-03-29
==================

Breaking changes:
* [OLMIS-2143](https://openlmis.atlassian.net/browse/OLMIS-2143), 
[OLMIS-2202](https://openlmis.atlassian.net/browse/OLMIS-2202): Add pagination to the users search endpoint
* [OLMIS-1776](https://openlmis.atlassian.net/browse/OLMIS-1776): Trade Item: rename manufacturer for GS1.
  * TradeItem.manufacturer => TradeItem.manufacturerOfTradeItem
* [OLMIS-1773](https://openlmis.atlassian.net/browse/OLMIS-1773): Orderable: rename packSize and name for GS1.
  * Orderable.name => Orderable.fullProductName
  * Orderable.packSize => Orderable.netContent
* Facility search endpoint now uses POST method instead of GET.
  * The parameters are passed in request body, similarly to /api/users/search endpoint.
  * The "zone" parameter was renamed to "zoneId"
  
New functionality added in a backwards-compatible manner:
* [OLMIS-1779](https://openlmis.atlassian.net/browse/OLMIS-1779): Add validations on saving program
  * code can not be null
  * code can not change - once set, it's set
  * code can not be an empty string
* [OLMIS-1970](https://openlmis.atlassian.net/browse/OLMIS-1970): Add validations on updating processing periods
  * It is not possible to modify any fields but name and description
  * Skip lastEndDate validation if period already exists
  * The Processing Period endpoints now include a schema
* [OLMIS-1985](https://openlmis.atlassian.net/browse/OLMIS-1985): Add data validation to the PUT /facilities/{id} endpoint
  * We do not allow saving with null supported programs
  * We do not allow saving with an empty list of supported programs
* [OLMIS-1965](https://openlmis.atlassian.net/browse/OLMIS-1965): Add validation for existing products that are not a commodity type
* [OLMIS-2027](https://openlmis.atlassian.net/browse/OLMIS-2027): Create unit and integration tests for repository/service/controller methods added during reporting rate/timeliness report development
* [OLMIS-2148](https://openlmis.atlassian.net/browse/OLMIS-2148), 
[OLMIS-633](https://openlmis.atlassian.net/browse/OLMIS-633),
[OLMIS-632](https://openlmis.atlassian.net/browse/OLMIS-632),
[OLMIS-629](https://openlmis.atlassian.net/browse/OLMIS-629) 
Add new rights for stock manager role:
  * STOCK_INVENTORIES_EDIT
  * STOCK_ADJUST
  * STOCK_CARDS_VIEW
  * STOCK_SOURCES_MANAGE
  * STOCK_DESTINATIONS_MANAGE
  * STOCK_CARD_LINE_ITEM_REASONS_MANAGE
  * STOCK_ORGANIZATIONS_MANAGE

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-1694](https://openlmis.atlassian.net/browse/OLMIS-1694): Remove Referencedata service to dependency to Auth service
  * Removed calls to auth service in PUT /api/users
  * The endpoints for changing and resetting password were removed from referencedata service. These were not included in RAML.
* [OLMIS-1977](https://openlmis.atlassian.net/browse/OLMIS-1977): Fix sending reset password emails for new user.
  * The user is now created even if there is a faliure sending notification email.
* [OLMIS-2185](https://openlmis.atlassian.net/browse/OLMIS-2185): Attempt to create invalid user now results in a proper error message.
  * The validations for User were added: The username, email, first and last names are required, the username and email have to be unique. The username cannot contain invalid characters (only letters, numbers, dashes and underscores are permitted).
* [OLMIS-2130](https://openlmis.atlassian.net/browse/OLMIS-2130): Assign Stock Event Manage role to srmanager2
* [OLMIS-1989](https://openlmis.atlassian.net/browse/OLMIS-1989): Assign Store Manager role to Administrator

3.0.0 / 2017-03-01
==================

* Released openlmis-referencedata 3.0.0 as part of openlmis-ref-distro 3.0.0. See [3.0.0 Release Notes](https://openlmis.atlassian.net/wiki/display/OP/3.0.0+Release+Notes).
  * This was the first stable release of openlmis-referencedata. It builds on the code, patterns,  and lessons learned from OpenLMIS 1 and 2.
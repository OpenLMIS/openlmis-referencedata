9.0.0 / WIP

Breaking changes:
* [OLMIS-3116](https://openlmis.atlassian.net/browse/OLMIS-3116): User DTO now returns home facility UUID instead of Facility object.
* [OLMIS-3105](https://openlmis.atlassian.net/browse/OLMIS-3105): User DTO now returns UUIDs instead of codes for role assignments.
* [OLMIS-3293](https://openlmis.atlassian.net/browse/OLMIS-3293): Paginate search facilityTypeApprovedProducts and made endpoit RESTful

New functionality added in a backwards-compatible manner:
* [OLMIS-2892](https://openlmis.atlassian.net/browse/OLMIS-2892): Added ideal stock amounts model.
* [OLMIS-2966](https://openlmis.atlassian.net/browse/OLMIS-2966): Create User Rights for Managing Ideal Stock Amounts.
* [OLMIS-3227](https://openlmis.atlassian.net/browse/OLMIS-3227): Added GET Ideal Stock Amounts endpoint with download csv functionality.
* [OLMIS-3022](https://openlmis.atlassian.net/browse/OLMIS-3022): Refresh right assignments on role-based access control (RBAC) structural changes.
* [OLMIS-3263](https://openlmis.atlassian.net/browse/OLMIS-3263): Added new ISA dto with links to nested objects.
* [OLMIS-396](https://openlmis.atlassian.net/browse/OLMIS-396): Added ISA upload endpoint.

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-2857](https://openlmis.atlassian.net/browse/OLMIS-2857): Refactored user search repository method to user database pagination and sorting.
* [OLMIS-2913](https://openlmis.atlassian.net/browse/OLMIS-2913): add DIVO user and assign to Inventory Manager role for SN1 and SN2
* [OLMIS-3146](https://openlmis.atlassian.net/browse/OLMIS-3146): added PROGRAMS_MANAGE right and enforce it on CUD endpoints.
* [OLMIS-3209](https://openlmis.atlassian.net/browse/OLMIS-3209): Fixed problem with parsing orderable DTO when it contains several program orderables.

8.0.1 / 2017-09-05

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-3130](https://openlmis.atlassian.net/browse/OLMIS-3130): Fixed high memory usage during right assignment migration.

8.0.0 / 2017-09-01
==================

Breaking changes:
* [OLMIS-2709](https://openlmis.atlassian.net/browse/OLMIS-2709): Facility search now returns smaller objects.
* [OLMIS-2698](https://openlmis.atlassian.net/browse/OLMIS-2698): Geographic Zone search endpoint now is paginated and accepts POST requests, also has new parameters: name and code.

New functionality added in a backwards-compatible manner:
* [OLMIS-2609](https://openlmis.atlassian.net/browse/OLMIS-2609): Created rights to manage CCE and assigned to system administrator.
* [OLMIS-2610](https://openlmis.atlassian.net/browse/OLMIS-2610): Added CCE Inventory View/Edit rights, added demo data for those rights.
* [OLMIS-2696](https://openlmis.atlassian.net/browse/OLMIS-2696): Added search requisition groups endpoint.
* [OLMIS-2780](https://openlmis.atlassian.net/browse/OLMIS-2780): Added endpoint for getting all facilities with minimal representation.
* Introduced JaVers to all domain entities. Also each domain entity has endpoint to get the audit information.
* [OLMIS-3023](https://openlmis.atlassian.net/browse/OLMIS-3023): Added enableDatePhysicalStockCountCompleted field to program settings.
* [OLMIS-2619](https://openlmis.atlassian.net/browse/OLMIS-2619): Added CCE Manager role and assigned CCE Manager and Inventory Manager roles to new user ccemanager.
* [OLMIS-2811](https://openlmis.atlassian.net/browse/OLMIS-2811): Added API endpoint for user's permission strings.
* [OLMIS-2885](https://openlmis.atlassian.net/browse/OLMIS-2885): Added ETag support for programs and facilities endpointsw.

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-2871](https://openlmis.atlassian.net/browse/OLMIS-2871): The service now uses an Authorization header instead of an access_token request parameter when communicating with other services.
* [OLMIS-2534](https://openlmis.atlassian.net/browse/OLMIS-2534): Fixed potential huge performance issue
* [OLMIS-2716](https://openlmis.atlassian.net/browse/OLMIS-2716): Set productCode field in Orderable as unique
* [OLMIS-2238](https://openlmis.atlassian.net/browse/OLMIS-2238): Added sorting users by username in user search endpoint.
* [MW-412](https://openlmis.atlassian.net/browse/MW-412): Added CORS support.
* [MW-430](https://openlmis.atlassian.net/browse/MW-430): Added missing foreign keys in FTAP.
* [MW-129](https://openlmis.atlassian.net/browse/MW-129): Introduced JaVers to all domain objects.
* [OLMIS-2901](https://openlmis.atlassian.net/browse/OLMIS-2901): Added keys to the lots table.
* [OLMIS-2724](https://openlmis.atlassian.net/browse/OLMIS-2724): Facility minimal endpoint will not be protected by admin right.
* [OLMIS-2831](https://openlmis.atlassian.net/browse/OLMIS-2831): Deprecate stock adjustment reason APIs in RAML.

7.0.0 / 2017-06-23
==================

Breaking changes:
* [OLMIS-2280](https://openlmis.atlassian.net/browse/OLMIS-2280): Added pagination, new parameters (zoneId, name, code) to supervisory node search endpoint
* [OLMIS-2277](https://openlmis.atlassian.net/browse/OLMIS-2277): Validate email adress

New functionality added in a backwards-compatible manner:
* [OLMIS-2611](https://openlmis.atlassian.net/browse/OLMIS-2611): Added using locale from env file
* [OLMIS-2729](https://openlmis.atlassian.net/browse/OLMIS-2729): Added endpoint for get user supported programs for home facility
  * We should not really require client apps to filter if programs are supported by user facility. The logic should be shared in backend service.

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-2606](https://openlmis.atlassian.net/browse/OLMIS-2606): Fix saving requisition groups with requisition group program schedules.

6.0.1 / 2017-06-01
==================

* [OLMIS-2628](https://openlmis.atlassian.net/browse/OLMIS-2628):
Fixes 6.0.0 Reference Data migrations with wrong timestamps - this fixes problems with the database
migration from 5.0.0/5.0.1, by fixing wrong timestamps in migrations. It makes migrating from 6.0.0
 to 6.0.1 problematic, hence we advise migrating straight to this version.


6.0.0 / 2017-05-26
==================

New functionality added in a backwards-compatible manner:

* [OLMIS-2492](https://openlmis.atlassian.net/browse/OLMIS-2492): Added new query param to facility search endpoint - code (facility type code)
  * Moved warehouse facility type from demo data to initial data.
* [OLMIS-2370](https://openlmis.atlassian.net/browse/OLMIS-2370): Added paginated search orderables endpoint.
  * Description field was added to Orderable class.
* [OLMIS-2357](https://openlmis.atlassian.net/browse/OLMIS-2357): Added audit logs for User entity.

Breaking changes:

* [OLMIS-1696](https://openlmis.atlassian.net/browse/OLMIS-1696):
In our medical commodities model, we changed how Orderables, TradeItems and CommodityTypes are associated - both internally to the Reference Data service, but also to external services.
Before, the code modeled this relationship as Orderables being an abstract base class to TradeItem and CommodityType. Orderable no longer are abstract, and TradeItem/CommodityType are not inherited, but rather having a "has a" relationship to Orderable.
  * CommodityType and TradeItem no longer inherit from Orderable.
  * Orderable, TradeItem and CommodityType all migrated to separate tables.
  * Orderable is no longer an abstract class - objects may be instantiated from it.
  * Orderable accept a map of identifiers where the key of that map is the type of identifier, and the value is the identifier.
  * When creating an Orderable, one should give it's identifier in the Orderable's map as e.g. key = commodityType (or tradeItem), value = uuid.
  * CommodityTypes is able of having 0 or more Orderables.
  * Removed description from CommodityType, added name (string), classificationSystem (string), classificationId (string). All required.
  * TradeItem is capable of having 0 or more Orderable.
  * Added Dtos for: TradeItem, CommodityType, OrderableDisplayCategory, TradeItemClassification, Dispensable.
  * Added get endpoint for retrieve all commoditytypes.
  * Added endpoint for create orderables.
  * Added pagination for getAll/search endpoints for Orderables, CommodityTypes and TradeItems.
  * Added validation for orderable, commodity type and trade item.
  * Added data migrations.
  * Removed redundant code.

5.0.1 / 2017-05-09
==================

Bug fixes, security and performance improvements, also backwards-compatible:
* [OLMIS-2479](https://openlmis.atlassian.net/browse/OLMIS-2479): Fix database migration error in 5.0.0

5.0.0 / 2017-05-08
==================

Breaking changes:

* [OLMIS-2320](https://openlmis.atlassian.net/browse/OLMIS-2320): Refactor FTAP - ProgramOrderable association
* [OLMIS-2284](https://openlmis.atlassian.net/browse/OLMIS-2284): Pagination for search facilities endpoint

New functionality added in a backwards-compatible manner:

* [OLMIS-2262](https://openlmis.atlassian.net/browse/OLMIS-2262): Lot domain object added
* [OLMIS-2259](https://openlmis.atlassian.net/browse/OLMIS-2259): Support for searching by right type to the 'api/rights/search' endpoint
* [OLMIS-2264](https://openlmis.atlassian.net/browse/OLMIS-2264): Lot repository and controller with POST and PUT methods
* [OLMIS-2265](https://openlmis.atlassian.net/browse/OLMIS-2265): Right check for PUT Lot
* [OLMIS-2263](https://openlmis.atlassian.net/browse/OLMIS-2263): Lot paginated search endpoint
* [OLMIS-2266](https://openlmis.atlassian.net/browse/OLMIS-2266): Lot GET endpoint
* [OLMIS-2338](https://openlmis.atlassian.net/browse/OLMIS-2338): Search user based on rights endpoint

Bug fixes, security and performance improvements, also backwards-compatible:

* [OLMIS-2258](https://openlmis.atlassian.net/browse/OLMIS-2258): Rights GET endpoint is available to users
* [OLMIS-2267](https://openlmis.atlassian.net/browse/OLMIS-2267): Made email an optional field for user
* [OLMIS-2155](https://openlmis.atlassian.net/browse/OLMIS-2155): Performance issue with custom ZonedDateTimeAttributeConverter
* [OLMIS-2319](https://openlmis.atlassian.net/browse/OLMIS-2319): Fixed creating new Commodity Type
* [OLMIS-2342](https://openlmis.atlassian.net/browse/OLMIS-2342): Lot expiration/manufacture dates changed from ZonedDateTime to LocalDate
* [OLMIS-1695](https://openlmis.atlassian.net/browse/OLMIS-1695): Refactor CommodityType - TradeItem association
* Requisition Group endpoints allow associating facilities and schedules
* [OLMIS-2404](https://openlmis.atlassian.net/browse/OLMIS-2404): Fixed concurrency issues in FTAP search
* [OLMIS-2369](https://openlmis.atlassian.net/browse/OLMIS-2369): Allow creating facilities without supported programs
* [OLMIS-2382](https://openlmis.atlassian.net/browse/OLMIS-2382): Check for creating duplicated FTAP
*

Dev and tooling updates made in a backwards-compatible manner:

* [OLMIS-2296](https://openlmis.atlassian.net/browse/OLMIS-2296): Lot demo data
* [OLMIS-2343](https://openlmis.atlassian.net/browse/OLMIS-2343): Lot endpoints marked as experimental
* [OLMIS-1972](https://openlmis.atlassian.net/browse/OLMIS-1972): Update Postgres from 9.4 to 9.6
  * This upgrade will apply automatically and all data will migrate.
* Update [Docker Dev Image](https://github.com/OpenLMIS/docker-dev) for builds from v1 to v2
  * Moves the sync_transifex.sh script out of each service and into the Docker Dev Image.
* Improve demo data
  * [OLMIS-2188](https://openlmis.atlassian.net/browse/OLMIS-2188): Description for roles in demo data
  * [OLMIS-2098](https://openlmis.atlassian.net/browse/OLMIS-2098): Stock manager role assigned to administrator

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

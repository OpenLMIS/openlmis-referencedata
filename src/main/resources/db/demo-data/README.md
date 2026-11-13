# Demo Data for OpenLMIS Reference Data Service

This folder holds the `demo-data` layer of the referencedata service's test data. It is loaded
with the `demo-data` Spring profile, after the base layer in `../base-data/`, which also documents
the curated programs, facilities, products and users.

This layer adds:

* the facilities, users and orderables that the demo data of other services refers to, such as
  facilities with cold chain equipment (cce) and users who recorded stock (stockmanagement),
  together with their geographic zones, supported programs and requisition group memberships;
* users with role assignments who are not named in `../base-data/README.md`, with their home
  facilities;
* ideal stock amounts for the EPI program.

Rows here refer only to rows of this folder or of `../base-data/`, so this layer does not need
`performance-data` and can be combined with it.

The auth and notification services still create logins and contact details for the generated users
without roles, which are in `../performance-data/`. Those logins have no referencedata user unless
`performance-data` is loaded too.

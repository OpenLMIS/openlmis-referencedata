# Demo Data for OpenLMIS Reference Data Service

This folder holds the `demo-data` layer of the referencedata service's test data. It is loaded
with the `demo-data` Spring profile, after the base layer in `../base-data/`, which also documents
the curated programs, facilities, products and users.

This layer adds:

* the EPI facilities of the Niassa province (`NIA*` codes) that the cold chain equipment (cce)
  demo data refers to, together with their geographic zones, supported programs and requisition
  group memberships;
* users with role assignments who are not named in `../base-data/README.md`, with their home
  facilities, such as the EPI users and `reporter1` to `reporter3`;
* the orderables that the demo data of other services refers to;
* ideal stock amounts for the EPI program.

Rows here refer only to rows of this folder or of `../base-data/`, so this layer does not need
`performance-data` and can be combined with it. The generated facilities and users without roles
are in `../performance-data/`.

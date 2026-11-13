# Performance Data for OpenLMIS Reference Data Service

This folder holds the `performance-data` layer of the referencedata service's test data: the
generated volume. It is loaded with the `performance-data` Spring profile, after the base layer in
`../base-data/`. It does not need `demo-data`, and `demo-data,performance-data` together load the
same rows as the single demo data set did before the data was split into layers.

This layer adds about 1,900 facilities, 10,000 Essential Meds products with their program
settings and facility type approvals, 500 supervisory nodes, requisition groups and supply lines,
and 500 users without roles. Rows here refer only to rows of this folder or of `../base-data/`.

The directory `demo-datasets/` holds IDs that are expected to exist when generating this data, so
changing IDs in the base layer may need these files to be re-generated.

The directory `schemas/` holds copies of the Mockaroo schemas used for generating data. When the
set of schemas on Mockaroo changes, please revision them here.

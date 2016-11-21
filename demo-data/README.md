# Demo Data for OpenLMIS Reference Data Service
This folder holds demo data for the referencedata service. The demo data is used by developers, QA
staff, and is automatically loaded into some environments for demo and testing purposes. It is not
for use in production environments.

Each .json file contains demo data that corresponds to one database table.

## Facilities (referencedata.facilities.json, referencedata.facility_operators.json and referencedata.facility_types.json)

1. HC01/Comfort Health Clinic
  * type: Health Center
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Balaka (City)
  * this is the home facility for the _administrator_ user
2. DH01/Balaka District Hospital
  * type: District Hospital
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Balaka District
3. W01/CMST Warehouse
  * type: Warehouse
  * programs: New Program
  * operated by: moh/Ministry of Health
  * zone: Southern Region
  * this is the home facility for the _devadmin_ user

There are also 2 inactive facilities, FAC003 and FAC004.

## Geographic Levels and Zones (referencedata.geographic_levels.json and referencedata.geographic_zones.json)

1. Country - Mal/Malawi
2. Region - Mal-So/Southern Region
3. District - M-So-Bal/Balaka District
4. City - M-So-Bal-Bal/Balaka

## Users (referencedata.users.json)

There are 2 user accounts:

1. administrator
  * home facility is HC01/Comfort Health Clinic
2. devadmin
  * home facility is W01/CMST Warehouse

Passwords for these user accounts come from the
[Authentication Service demo data](https://github.com/OpenLMIS/openlmis-auth/tree/master/demo-data).

Names for the facilities come from referencedata.facilities.json (see above).

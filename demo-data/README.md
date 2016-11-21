# Demo Data for OpenLMIS Reference Data Service
This folder holds demo data for the referencedata service. The demo data is used by developers, QA
staff, and is automatically loaded into some environments for demo and testing purposes. It is not
for use in production environments.

Each .json file contains demo data that corresponds to one database table.

## Facilities

In referencedata.facilities.json, .facility_operators.json, .facility_types.json,
.supported_programs.json, .supervisory_nodes.json and .supply_lines.json.

1. HC01/Comfort Health Clinic
  * type: Health Center
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Balaka (City)
  * this is the home facility for the _administrator_ user
  * this facility is the supplying facility for the Family Planning program with the Supervisory
    Node N1/FP Approval point
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

There are also 2 inactive facilities, FAC003 and FAC004. FAC003 is the supplying facility for
the Essential Meds program with the supervisory node N2/EM Approval point.

## Geographic Levels and Zones

In referencedata.geographic_levels.json and .geographic_zones.json.

1. Country - Mal/Malawi
2. Region - Mal-So/Southern Region
3. District - M-So-Bal/Balaka District
4. City - M-So-Bal-Bal/Balaka

## Programs and Products

In referencedata.programs.json, .orderable_products.json and .program_products.json.

1. Family Planning
  * skipping requisition periods is enabled
  * non-full-supply tab is enabled
  * products: (all of these are Global Products)
    * Oral Contraceptive
      1. C100/Levora
      2. C200/Ortho-Novum
    * Injectable Hormonal Contraceptive
      1. C300/Depo-Estradiol
    * Barrier Method
      1. C400/Male Condom
    * Implantable Contraceptive
      1. C500/Implanon
      2. C600/Levonorgestrel
2. Essential Meds
  * products:
    * Antibiotics
      1. C1/Acetylsalicylic Acid (a Trade Item)
      2. C2/Glibenclamide (a Trade Item)
    * Vaccines
      1. C3/Streptococcus Pneumoniae Vaccine (a Trade Item)
      2. C4/Streptococcus Pneumoniae Vaccine II (a Global Product)
3. PRG003/New program
  * products:
    * Injectable Hormonal Contraceptive
      1. C300/Depo-Estradiol

Currently, all the products are generally approved at all the facility types.
In the future we could add more differentiation by types of facilities in
referencedata.facility_type_approved_products.json.

## Requisition Groups and Schedules

In referencedata.requisition_groups.json, .processing_schedules.json,
.requisition_group_program_schedules.json and .requisition_group_members.json.

1. RG Family Planning
  * schedule: Monthly (Jan2016, Feb2016 ... Dec2016)
  * facilities:
    * HC01/Comfort Health Clinic (in two programs and two requisition groups)
  * note: Family Planning program is also supported at Balaka District Hospital,
    but that facility is not in any requisition group
2. RG Essential Meds
  * schedule: Quarterly (Q1, Q2, Q3, Q4 of 2016)
  * facilities:
    * HC01/Comfort Health Clinic (in two programs and two requisition groups)
3. RG New program
  * schedule: SCH005.
    * W01/CMST Warehouse

There are also 2 unused schedules, SCH003 and SCH004.

## Roles, Users and Rights

In referencedata.users.json, .roles.json, .role_assignments.json, .role_rights.json and
.supervisory_nodes.json.

1. Program Supervisor
  * rights:
    * Requisition Create, Approve, Authorize, Delete and View
  * users:
    * administrator - has this role for both Essential Meds and Family Planning programs
      at their home facility, HC01/Comfort Health Clinic, _and_ at the Supervisory Node
      N1/FP Approval point
2. Storeroom Manager
  * no rights
  * users:
    * administrator - has this fulfillment role at warehouse HC01/Comfort Health Clinic
3. Warehouse Clerk
  * no rights
  * no users

Other supervisory nodes are not currently used:
  * N1.1/FP Approval sub point
  * N2/EM Approval point
  * N3/New program approval point

Other users with no roles:
  * devadmin (home facility is W01/CMST Warehouse)
  * admin (this admin account is built into the bootstrap data)

Passwords for these user accounts come from the
[Authentication Service demo data](https://github.com/OpenLMIS/openlmis-auth/tree/master/demo-data).

Rights themselves come from the bootstrap data,
[bootstrap.sql](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/resources/bootstrap.sql).

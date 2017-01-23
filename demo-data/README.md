# Demo Data for OpenLMIS Reference Data Service
This folder holds demo data for the referencedata service. The demo data is used by developers, QA
staff, and is automatically loaded into some environments for demo and testing purposes. It is not
for use in production environments.

Each .json file contains demo data that corresponds to one database table.

## Facilities

The facilities described below are defined in these files:
referencedata.facilities.json, referencedata.facility_operators.json,
referencedata.facility_types.json, referencedata.supported_programs.json,
referencedata.supervisory_nodes.json and referencedata.supply_lines.json.

1. HC01/Comfort Health Clinic
  * type: Health Center
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Balaka (City)
  * this is the home facility for the _administrator_, _srmanager_ and _smanager_ users
  * this facility is the supplying facility for the Family Planning program with the Supervisory
    Node SN1.1/FP Approval sub point and SN3/FP Approval point
2. DH01/Balaka District Hospital
  * type: District Hospital
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Balaka District
  * this is the home facility for the _dsrmanager_ user
  * this facility is the supplying facility for the Family Planning program with the Supervisory
  Node SN1/FP Approval Point and for the Essential Meds program with the Supervisory Node SN2/EM
  Approval Point
3. W01/CMST Warehouse
  * type: Warehouse
  * programs: Family Planning
  * operated by: moh/Ministry of Health
  * zone: Southern Region
  * this is the home facility for the _devadmin_ and _wclerk_ users
4. HF01/Kankao Health Facility
  * type: Health Center
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Malawi
5. HC02/Nandumbo Health Center
  * type: Health Center
  * programs: Family Planning
  * operated by: moh/Ministry of Health
  * zone: Malawi
6. HC03/Kalembo Health Center
  * type: Health Center
  * programs: Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Malawi
7. WH02/South Warehouse
  * type: Warehouse
  * programs: Family Planning and Essential Meds
  * operated by: moh/Ministry of Health
  * zone: Malawi

## Geographic Levels and Zones

Defined in referencedata.geographic_levels.json and referencedata.geographic_zones.json.

1. Country - Mal/Malawi
2. Region - Mal-So/Southern Region
3. District - M-So-Bal/Balaka District
4. City - M-So-Bal-Bal/Balaka

## Programs and Products

Defined in referencedata.programs.json, referencedata.orderable_products.json and
referencedata.program_products.json.

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

Defined in referencedata.requisition_groups.json, referencedata.processing_schedules.json,
referencedata.requisition_group_program_schedules.json and
referencedata.requisition_group_members.json.

1. RGFP1/RG Family Planning 1
  * schedule: Monthly (Jan2016, Feb2016 ... Dec2016)
  * facilities:
    * HC01/Comfort Health Clinic
    * HC02/Nandumbo Health Center
  * note: Family Planning program is also supported at Balaka District Hospital,
    but that facility is not in any requisition group
2. RGEM1/RG Essential Meds 1
  * schedule: Quarterly (Q1, Q2, Q3, Q4 of 2016)
  * facilities:
    * HC02/Nandumbo Health Center
    * HC03/Kalembo Health Center
    * DH01/Balaka District Hospital
    * HF01/Kankao Health Facility
3. RGFP2/RG Family Planning 2
  * schedule: SCH005
  * facilities:
    * DH01/Balaka District Hospital
    * HF01/Kankao Health Facility

There are also 2 unused schedules, SCH003 and SCH004.

## Roles, Users and Rights

Defined in referencedata.users.json, referencedata.roles.json, referencedata.role_assignments.json,
referencedata.role_rights.json and referencedata.supervisory_nodes.json.

1. Storeroom Manager
  * rights:
    * Requisition View
    * Requisition Delete
    * Requisition Create
  * users:
    * administrator - has this role for Family Planning & Essential Meds
    * srmanager - has this role for Family Planning & Essential Meds
2. Store Manager
  * rights:
    * Requisition View
    * Requisition Authorize
  * users:
    * administrator - has this role for Family Planning & Essential Meds
    * smanager - has this role for Family Planning & Essential Meds
3. District Storeroom Manager
  * rights:
    * Requisition View
    * Requisition Approve
  * users:
    * administrator - has this role for Family Planning & Essential Meds
    * dsrmanager - has this role for Family Planning & Essential Meds
4. Program Supervisor
  * rights:
    * Requisition View
    * Requisition Approve
  * users:
    * administrator - has this role for Family Planning & Essential Meds for SN1/FP Approval
                      point supervisory node
    * psupervisor - has this role for Family Planning & Essential Meds for SN1/FP Approval
                    point supervisory node
5. Warehouse Clerk
  * rights:
    * Requisition View
    * Requisition Convert to Order
  * users:
    * administrator - has this role for WH01/CMST Warehouse and WH02/South Warehouse
    * wclerk
6. Admin
  * rights:
    * Facilities manage
    * Geographic zones manage
    * Supervisory nodes manage
    * Products manage
    * Requisition templates manage
    * Stock card templates manage
  * users:
    * administrator
7. Program Coordinator
  * users:
    * administrator - has this role for Family Planning & Essential Meds
    * devadmin - has this role for Family Planning & Essential Meds for SN1/FP Approval point

## Supervisory Nodes and Supply Lines
1. SN1/FP Approval point
  * facility: DH01/Balaka District Hospital
  * supply lines:
    * family planning
      * warehouse:
        * WH01/CMST Warehouse
        * WH02/South Warehouse
2. SN1.1/FP Approval sub point
  * facility: HC01/Comfort Health Clinic
  * parent: SN1/FP Approval point
3. SN2/EM Approval point
  * facility: DH01/Balaka District Hospital
  * supply lines:
    * essential meds
      * warehouse: WH02/South Warehouse
4. SN3/FP Approval point
  * facility: HC01/Comfort Health Clinic


Other users with no roles:
  * admin (this admin account is built into the bootstrap data)

Passwords for these user accounts come from the
[Authentication Service's demo data](https://github.com/OpenLMIS/openlmis-auth/tree/master/demo-data).

Rights themselves come from the bootstrap data,
[bootstrap.sql](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/resources/bootstrap.sql).

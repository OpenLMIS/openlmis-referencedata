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
  * zone: Neno District
  * this is the home facility for _administrator_, _srmanager2_ and _smanager2_ users
  * this facility is in the requisition group RGFP1 and RGEM1
2. HF01/Kankao Health Facility
  * type: Health Center
  * programs: Family Planning and Essential Meds
  * zone: Zomba District
  * this is the home facility for _srmanager4_ and _smanager4_ users
  * this facility is in the requisition group RGEM1 and RGFP2
3. HC02/Nandumbo Health Center
  * type: Health Center
  * programs: Family Planning
  * zone: Phalombe District
  * this is the home facility for _srmanager1_ and _smanager1_ users
  * this facility is in the requisition group RGFP1
4. HC03/Kalembo Health Center
  * type: Health Center
  * programs: Essential Meds
  * zone: Thyolo District
  * this is the home facility for _srmanager3_ and _smanager3_ users
  * this facility is in the requisition group RGEM1
5. DH01/Balaka District Hospital
  * type: District Hospital
  * programs: Family Planning and Essential Meds
  * zone: Balaka (City)
  * this is the home facility for the _dsrmanager_ user
  * this facility is in the requisition group RGEM1 and RGFP2
6. WH01/Ntcheu District Warehouse
  * type: Warehouse
  * programs: Family Planning
  * zone: Ntcheu District
  * this is the home facility for _devadmin_ and _wclerk1_ users
7. WH02/Balaka District Warehouse
  * type: Warehouse
  * programs: Family Planning and Essential Meds
  * zone: Balaka (City)
  * this is the home facility for _wclerk2_ user

**For EPI (Vaccines) Program**

* Mozambique Central Warehouse (W001)
  * type: Warehouse
  * zone: Mozambique Country
  * this is the home facility for the _vwclerk1_ user
* Dep—sito Provincial Niassa (P001)
  * type: Provincial Store
  * zone: Niassa Province
  * this is the home facility for the _rivo_ user
* Dep—sito Distrital Cuamba (D001)
  * type: District Store
  * zone: Cuamba District
  * this is the home facility for the _divo1_ and _vsrmanager2 users
  * this facility is in the requisition RGEPI1
* Dep—sito Distrital Lichinga (D002)
  * type: District Store
  * zone: Lichinga District
  * this is the home facility for the _divo2_ user
  * this facility is in the requisition RGEPI1

There are 16 facilities in the Cuamba district, all health centers. Though all of them can be used
 to demo, it is recommended to use the following facility:

* Cuamba (N003)
  * type: Health Center
  * zone: Cuamba District
  * this facility is in the requisition group RGEPI2

There are 25 facilities in the Lichinga district, all health centers. Though all of them can be used
 to demo, it is recommended to use the following facility:

* Assumane (N036)
  * type: Health Center
  * zone: Lichinga District
  * this facility is in the requisition group RGEPI3

## Geographic Levels and Zones

Defined in referencedata.geographic_levels.json and referencedata.geographic_zones.json.

1. Country
  * Malawi/Malawi
2. Region
  * Malawi-Central/Central Region
  * Malawi-Northern/Northern Region
  * Malawi-Southern/Southern Region
3. District
  * Malawi-Central-Dedza/Dedza District
  * Malawi-Central-Dowa/Dowa District
  * Malawi-Central-Kasungu/Kasungu District
  * Malawi-Central-Lilongwe/Lilongwe District
  * Malawi-Central-Mchinji/Mchinji District
  * Malawi-Central-Nkhotakota/Nkhotakota District
  * Malawi-Central-Ntcheu/Ntcheu District
  * Malawi-Central-Ntchisi/Ntchisi District
  * Malawi-Central-Salima/Salima District
  * Malawi-Northern-Chitipa/Chitipa District
  * Malawi-Northern-Karonga/Karonga District
  * Malawi-Northern-Likoma/Likoma District
  * Malawi-Northern-Mzimba/Mzimba District
  * Malawi-Northern-Nkhata Bay/Nkhata Bay District
  * Malawi-Northern-Rumphi/Rumphi District
  * Malawi-Southern-Balaka/Balaka District
  * Malawi-Southern-Blantyre/Blantyre District
  * Malawi-Southern-Chikwawa/Chikwawa District
  * Malawi-Southern-Chiradzulu/Chiradzulu District
  * Malawi-Southern-Machinga/Machinga District
  * Malawi-Southern-Mangochi/Mangochi District
  * Malawi-Southern-Mulanje/Mulanje District
  * Malawi-Southern-Mwanza/Mwanza District
  * Malawi-Southern-Nsanje/Nsanje District
  * Malawi-Southern-Thyolo/Thyolo District
  * Malawi-Southern-Phalombe/Phalombe District
  * Malawi-Southern-Zomba/Zomba District
  * Malawi-Southern-Neno/Neno District
4. City
  * Malawi-Southern-Balaka-Balaka/Balaka

**For EPI (Vaccines) Program**

* Mozambique (moz) - Country
  * Niassa (niassa) - Province
    * Cuamba (cuamba) - District
    * Lichinga (lichinga-distrito) - District

## Programs and Products

Defined in referencedata.programs.json, referencedata.orderables.json and
referencedata.program_orderables.json.

1. Family Planning
  * skipping requisition periods is enabled
  * non-full-supply tab is enabled
  * products: (all of these are Commodity Types)
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
      2. C4/Streptococcus Pneumoniae Vaccine II (a Commodity Type)
3. PRG003/New program
  * products:
    * Injectable Hormonal Contraceptive
      1. C300/Depo-Estradiol
4. EPI
  * products:
    * Vaccines: (all are VVM enabled)
      * BCG (bcg20) - Commodity Type and Orderable
        * BCGI2017A - Lot, expires 2019-06-01
        * BCGI2017B - Lot, expires 2019-07-01
      * Polio - Commodity Type
        * Polio (20 dose) (polio20) - Orderable
        * IPV (ipv5) - Orderable
      * Pentavalent - Commodity Type
        * Pentavalent (1 dose) (penta1) - Orderable
        * Pentavalent (10 dose) (penta10) - Orderable
      * PCV (pcv10) - Commodity Type and Orderable
      * Rotavirus (rota1) - Commodity Type and Orderable
        * ROTAM2017A - Lot, expires 2019-06-01
        * ROTAM2017B - Lot, expires 2019-07-01
      * Measles (measles10) - Commodity Type and Orderable
      * Tetanus (tetanus10) - Commodity Type and Orderable
      * Syringe 5ml (syringe5ml) - Orderable
      * Syringe 0.5ml (syringe05ml) - Orderable
      * Syringe 0.05ml (syringe005ml) - Orderable
      * Safety Box (safetybox) - Orderable
      * Gas (Cylinder) (gas) - Orderable
      * Diluent BCG (bcg20dil) - Orderable
      * Diluent Measles (measles10dil) - Orderable

Currently, all the products are generally approved at all the facility types.
In the future we could add more differentiation by types of facilities in
referencedata.facility_type_approved_products.json.

## Requisition Groups and Schedules

Defined in referencedata.requisition_groups.json, referencedata.processing_schedules.json,
referencedata.requisition_group_program_schedules.json and
referencedata.requisition_group_members.json.

1. RGFP1/RG Family Planning 1
  * schedule: Monthly (Jan2017, Feb2017 ... Dec2017)
  * facilities:
    * HC01/Comfort Health Clinic
    * HC02/Nandumbo Health Center
2. RGEM1/RG Essential Meds 1
  * schedule: Quarterly (Q1, Q2, Q3, Q4 of 2017 and 2018)
  * facilities:
    * HC01/Comfort Health Clinic
    * HC03/Kalembo Health Center
    * DH01/Balaka District Hospital
    * HF01/Kankao Health Facility
3. RGFP2/RG Family Planning 2
  * schedule: Quarterly (Q1, Q2, Q3, Q4 of 2017 and 2018)
  * facilities:
    * DH01/Balaka District Hospital
    * HF01/Kankao Health Facility

There are also 3 unused schedules, SCH003 and SCH004, SCH005.

**For EPI (Vaccines) Program**

* RG EPI 1 (districts) (RGEPI1)
  * schedule: Quarterly
  * facilities:
    * Dep—sito Distrital Cuamba (D001)
    * Dep—sito Distrital Lichinga (D002)
* RG EPI 2 (health facilities) (RGEPI2)
  * schedule: Monthly
  * facilities:
    * Cuamba (N003)
    * 15 others...
* RG EPI 3 (health facilities) (RGEPI3)
  * schedule: Monthly
  * facilities:
    * Assumane (N036)
    * 24 others...

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
    * srmanager2, srmanager4 - has this role for Family Planning & Essential Meds
    * srmanager1 - has this role for Family Planning
    * srmanager3 - has this role for Essential Meds
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * vsrmanager1 - has this role for EPI at Cuamba district approval point (SN-CUAMBA-DIST)
2. Store Manager
  * rights:
    * Requisition View
    * Requisition Delete
    * Requisition Authorize
  * users:
    * administrator - has this role for Family Planning & Essential Meds
    * smanager2, smanager4 - has this role for Family Planning & Essential Meds
    * smanager1 - has this role for Family Planning
    * smanager3 - has this role for Essential Meds
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * vsrmanager1 - has this role for EPI at Cuamba district approval point (SN-CUAMBA-DIST)
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
    * divo1 - has this role for EPI at Cuamba district approval point (SN-CUAMBA-DIST)
    * rivo - has this role for EPI at Niassa province approval point (SN-NIASSA-PROV)
5. Warehouse Clerk
  * rights:
    * Orders View
    * Orders Edit (Requisition Convert to Order)
    * Shipments View
    * Shipments Edit
    * PoDs Manage
  * users:
    * administrator - has this role for WH01/Ntcheu District Warehouse and WH02/Balaka District Warehouse
    * divo1 - has this role at D001/Dep—sito Distrital Cuamba
    * divo2 - has this role at D002/Dep—sito Distrital Lichinga
    * rivo - has this role at P001/Dep—sito Provincial Niassa
    * wclerk1 - has this role for WH01/Ntcheu District Warehouse
    * wclerk2 - has this role for WH02/Balaka District Warehouse
    * vwclerk1 - has this role at Mozambique Central Warehouse (W001)
6. Delivery Approver
  * rights:
    * PoDs Manage
  * users:
    * srmanager1, srmanager2, srmanager4 - has this role for WH01/Ntcheu District Warehouse and WH02/Balaka District Warehouse
    * srmanager3 - has this role for WH02/Balaka District Warehouse
7. Stock Manager
  * rights:
    * Stock Cards View
    * Stock Adjust
    * Stock Inventories Edit
  * users:
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * administrator - has this role for EPI at Niassa province approval point (SN-NIASSA-PROV)
8. Requisition Viewer
  * rights:
    * Requisition View
  * users:
    * wclerk1 - has this role for Family Planning
    * wclerk2 - has this role for Family Planning & Essential Meds
9. System Administrator
  * rights:
    * Facilities Manage
    * Facilities Approved Orderables Manage
    * Geographic Zones Manage
    * Orderables Manage
    * Processing Schedules Manage
    * Products Manage
    * Requisition Groups Manage
    * Requisition Templates Manage
    * Stock Adjustment Reasons Manage
    * Stock Card Templates Manage
    * Supervisory Nodes Manage
    * Supply Lines Manage
    * System Settings Manage
    * Users Manage
    * User Roles Manage
    * System Ideal Stock Amounts Manage
    * CCE Manage
    * Service Accounts Manage
  * users:
    * administrator
    * admin
10. CCE Inventory Manager
  * rights:
    * CCE Inventory View
    * CCE Inventory Edit
  * users:
    * administrator - has this role for Family Planning at FP approval point and Essential Meds at EM approval point
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
11. CCE Inventory Viewer
  * rights:
    * CCE Inventory View
  * users:
    * administrator - has this role for Family Planning at FP approval point and Essential Meds at EM approval point
    * divo1 - has this role for EPI at Cuamba district approval point (SN-CUAMBA-DIST)
    * divo2 - has this role for EPI at Lichinga district approval point (SN-LICHINGA-DIST)
    * rivo - has this role for EPI at Niassa province approval point (SN-NIASSA-PROV)
    * vsrmanager1 - has this role for EPI at Cuamba district approval point (SN-CUAMBA-DIST)
    * vsrmanager2 - has this role for EPI at home facility


Passwords for these user accounts come from the
[Authentication Service's demo data](https://github.com/OpenLMIS/openlmis-auth/tree/master/demo-data).

Rights themselves come from the 
[bootstrap data](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/resources/db/migration/20170206205310272__initial_bootstrap_data.sql). **NOTE:** This SQL script does not have all of 
the rights, as additional rights have been added to later migrations.

## Supervisory Nodes and Supply Lines

Defined in referencedata.supervisory_nodes.json, referencedata.supply_lines.json, referencedata.requisition_groups.json

1. SN1/FP Approval point
  * requisition group: RGFP1
    * program: Family Planning
  * supply lines:
    * WH01/Ntcheu District Warehouse
  * facility: HC01/Comfort Health Clinic
2. SN1.1/FP Approval sub point
  * requisition group: RGFP2
    * program: Family Planning
  * facility: DH01/Balaka District Hospital
  * parent: SN1/FP Approval point
3. SN2/EM Approval point
  * requisition group: RGEM1
    * program: Essential Meds
  * supply lines:
    * WH02/Balaka District Warehouse
  * facility: DH01/Balaka District Hospital
4. SN-NIASSA-PROV/Niassa province approval point
  * requisition group: RGEPI1
    * program: EPI
  * supply lines:
    * W001/Mozambique Central Warehouse
  * facility: Dep—sito Provincial Niassa
5. SN-CUAMBA-DIST/Cuamba district approval point
  * requisition group: RGEPI2
    * program: EPI
  * supply lines:
    * D001/Dep—sito Distrital Cuamba
  * facility: Dep—sito Distrital Cuamba
  * parent: SN-NIASSA-PROV/Niassa province approval point

**For EPI (Vaccines) Program**

* Niassa province approval point (SN-NIASSA-PROV)
  * requisition group: RG EPI 1 (districts) (RGEPI1)
  * facility: Dep—sito Provincial Niassa (P001)
  * supply lines:
      * Mozambique Central Warehouse (W001)
* Cuamba district approval point (SN-CUAMBA-DIST)
  * requisition group: RG EPI 2 (health facilities) (RGEPI2)
  * facility: Dep—sito Distrital Cuamba (D001)
  * parent: Niassa province approval point (SN-NIASSA-PROV)
* Lichinga district approval point (SN-LICHINGA-DIST)
  * requisition group: RG EPI 3 (health facilities) (RGEPI3)
  * facility: Dep—sito Distrital Lichinga (D002)
  * parent: Niassa province approval point (SN-NIASSA-PROV)

## Ideal Stock Amounts

Defined in referencedata.ideal_stock_amounts.json.

Only in the EPI program. These amounts are defined for:
 
* Health facilities (all 41)
  * All monthly periods in 2017 and 2018
  * All seven commodity types
  * Values range from 1,000 - 10,000
* District stores (2)
  * All quarterly periods in 2017 and 2018
  * All seven commodity types 
  * Values range from 10,000 - 99,999

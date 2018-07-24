# Demo Data for OpenLMIS Reference Data Service

This folder holds demo data for the referencedata service. The demo data is used by developers, QA
staff, and is automatically loaded into some environments for demo and testing purposes. It is not
for use in production environments.

Each .csv file contains demo data that corresponds to one database table.

## Facilities

The facilities described below are defined in these files:
referencedata.facilities.csv, referencedata.facility_operators.csv,
referencedata.facility_types.csv, referencedata.supported_programs.csv,
referencedata.supervisory_nodes.csv and referencedata.supply_lines.csv.

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
* Depósito Provincial Niassa (P001)
  * type: Provincial Store
  * zone: Niassa Province
  * this is the home facility for the _rivo_ user
* Depósito Distrital Cuamba (D001)
  * type: District Store
  * zone: Cuamba District
  * this is the home facility for the _divo1_ and _vsrmanager2_ users
  * this facility is in the requisition RGEPI1
* Depósito Distrital Lichinga (D002)
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

Additionally, there are many other facilities in other districts, but they are not used except to
 populate the system.

## Geographic Levels and Zones

Defined in referencedata.geographic_levels.csv and referencedata.geographic_zones.csv.

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
  * and others...

## Programs and Products

Defined in referencedata.programs.csv, referencedata.orderables.csv and
referencedata.program_orderables.csv.

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
referencedata.facility_type_approved_products.csv.

## Requisition Groups and Schedules

Defined in referencedata.requisition_groups.csv, referencedata.processing_schedules.csv,
referencedata.requisition_group_program_schedules.csv and
referencedata.requisition_group_members.csv.

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
    * Depósito Distrital Cuamba (D001)
    * Depósito Distrital Lichinga (D002)
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

Defined in referencedata.users.csv, referencedata.roles.csv, referencedata.role_assignments.csv,
referencedata.role_rights.csv and referencedata.supervisory_nodes.csv.

1. Storeroom Manager
  * rights:
    * Requisition View
    * Requisition Delete
    * Requisition Create
    * Proofs of Delivery Manage
  * users:
    * administrator - has this role for Family Planning & Essential Meds at SN1/FP approval point and SN2/EM approval point
    * srmanager2, srmanager4 - has this role for Family Planning & Essential Meds at home facility
    * srmanager1 - has this role for Family Planning at home facility
    * srmanager3 - has this role for Essential Meds at home facility
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * vsrmanager1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
2. Store Manager
  * rights:
    * Requisition View
    * Requisition Delete
    * Requisition Authorize
  * users:
    * administrator - has this role for Family Planning & Essential Meds at home facility, SN1/FP approval point
        and SN2/EM approval point
    * smanager1 - has this role for Family Planning at home facility
    * smanager2 - has this role for Family Planning at home facility and SN1/FP approval point
        & for Essential Meds at home facility and SN2/EM approval point
    * smanager3 - has this role for Essential Meds at home facility
    * smanager4 - has this role for Family Planning & Essential Meds at home facility
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * vsrmanager1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
3. District Storeroom Manager
  * rights:
    * Requisition View
    * Requisition Approve
  * users:
    * administrator - has this role for Family Planning & Essential Meds at SN2/EM approval
        point and SN1/FP approval point
    * dsrmanager - has this role for Family Planning & Essential Meds at SN2/EM approval
        point and SN1/FP approval point
4. Program Supervisor
  * rights:
    * Requisition View
    * Requisition Approve
  * users:
    * administrator - has this role for Family Planning & Essential Meds at SN2/EM approval
        point and SN1/FP approval point
    * psupervisor - has this role for Family Planning & Essential Meds at SN1/FP approval
        point supervisory node
    * divo1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
    * rivo - has this role for EPI at SN-NIASSA-PROV/Niassa province approval point
5. Warehouse Clerk
  * rights:
    * Orders View
    * Orders Edit (Requisition Convert to Order)
    * Shipments View
    * Shipments Edit
  * users:
    * administrator - has this role for WH01/Ntcheu District Warehouse and WH02/Balaka District Warehouse
    * divo1 - has this role at D001/Depósito Distrital Cuamba
    * divo2 - has this role at D002/Depósito Distrital Lichinga
    * rivo - has this role at P001/Depósito Provincial Niassa
    * wclerk1 - has this role for WH01/Ntcheu District Warehouse
    * wclerk2 - has this role for WH02/Balaka District Warehouse
    * vwclerk1 - has this role at W001/Mozambique Central Warehouse
6. Delivery Approver
  * rights:
    * Proofs of Delivery Manage
  * users:
    * administrator - has this role for Family Planning at SN1/FP approval point
    * divo1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
    * smanager1, smanager2, smanager4, srmanager1, srmanager2, srmanager4 - has this role for Family Planning
        at SN1/FP approval point and for Essential Meds at SN2/EM approval point
    * smanager3, srmanager3 - has this role for Essential Meds at SN2/EM approval point
7. Stock Manager
  * rights:
    * Stock Cards View
    * Stock Adjust
    * Stock Inventories Edit
  * users:
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * administrator - has this role for EPI at SN-NIASSA-PROV/Niassa province approval point
        and for Family Planning & Essential Meds at SN1/FP approval point and SN2/EM approval point
    * srmanager1 - has this role for Family Planning at home facility
    * srmanager2 - has this role for Family Planning & Essential Meds at home facility
8. Stock Viewer
  * rights:
    * Stock Cards View
  * users:
    * wclerk1 - has this role for Family Planning at home facility
9. Requisition Viewer
  * rights:
    * Requisition View
  * users:
    * wclerk1 - has this role for Family Planning at home facility
    * wclerk2 - has this role for Family Planning & Essential Meds at home facility
10. System Administrator
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
11. CCE Inventory Manager
  * rights:
    * CCE Inventory View
    * CCE Inventory Edit
  * users:
    * administrator - has this role for Family Planning at SN1/FP approval point and Essential Meds at SN2/EM approval point
    * divo1 - has this role for EPI at home facility and SN-CUAMBA-DIST/Cuamba district approval point
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
12. CCE Inventory Viewer
  * rights:
    * CCE Inventory View
  * users:
    * administrator - has this role for Family Planning at SN1/FP approval point and Essential Meds at SN2/EM approval point
    * divo2 - has this role for EPI at SN-LICHINGA-DIST/Lichinga district approval point
    * rivo - has this role for EPI at SN-NIASSA-PROV/Niassa province approval point
    * vsrmanager1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
    * vsrmanager2 - has this role for EPI at home facility


Passwords for these user accounts come from the
[Authentication Service's demo data](https://github.com/OpenLMIS/openlmis-auth/tree/master/src/main/resources/db/demo-data).

Rights themselves come from the 
[bootstrap data](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/resources/db/migration/20170206205310272__initial_bootstrap_data.sql). **NOTE:** This SQL script does not have all of 
the rights, as additional rights have been added to later migrations.

## Supervisory Nodes and Supply Lines

Defined in referencedata.supervisory_nodes.csv, referencedata.supply_lines.csv, referencedata.requisition_groups.csv

1. SN1/FP approval point
  * requisition group: RGFP1
    * program: Family Planning
  * supply lines:
    * WH01/Ntcheu District Warehouse
  * facility: HC01/Comfort Health Clinic
2. SN1.1/FP Approval sub point
  * requisition group: RGFP2
    * program: Family Planning
  * facility: DH01/Balaka District Hospital
  * parent: SN1/FP approval point
3. SN2/EM approval point
  * requisition group: RGEM1
    * program: Essential Meds
  * supply lines:
    * WH02/Balaka District Warehouse
  * facility: DH01/Balaka District Hospital

**For EPI (Vaccines) Program**

* SN-NIASSA-PROV/Niassa province approval point
  * requisition group: RG EPI 1 (districts) (RGEPI1)
  * facility: P001/Depósito Provincial Niassa
  * supply lines:
    * W001/Mozambique Central Warehouse
* SN-CUAMBA-DIST/Cuamba district approval point
  * requisition group: RG EPI 2 (health facilities) (RGEPI2)
  * supply lines:
    * D001/Depósito Distrital Cuamba
  * facility: D001/Depósito Distrital Cuamba
  * parent: SN-NIASSA-PROV/Niassa province approval point
* SN-LICHINGA-DIST/Lichinga district approval point
  * requisition group: RG EPI 3 (health facilities) (RGEPI3)
  * facility: D002/Depósito Distrital Lichinga
  * parent: SN-NIASSA-PROV/Niassa province approval point

## Ideal Stock Amounts

Defined in referencedata.ideal_stock_amounts.csv.

Only in the EPI program. These amounts are defined for:
 
* Health facilities (all 41)
  * All monthly periods in 2017 and 2018
  * All seven commodity types
  * Values range from 1,000 - 10,000
* District stores (2)
  * All quarterly periods in 2017 and 2018
  * All seven commodity types 
  * Values range from 10,000 - 99,999

# Additional Information

The directory `demo-datasets/` holds IDs that are expected to exist from demo-data.
Therefore changing ID's in demo-data may need for these files to be re-generated.

The directory `schemas/` holds copies of the Mockaroo schemas used for generating
data. When the set of schemas on Mockaroo changes, please revision them here.

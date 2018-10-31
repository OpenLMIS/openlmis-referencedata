# Demo Data for OpenLMIS Reference Data Service

This folder holds demo data for the referencedata service. The demo data is used by developers, QA
staff, and is automatically loaded into some environments for demo and testing purposes. It is not
for use in production environments.

Each .csv file contains demo data that corresponds to one database table.

## Geographic Levels and Zones

Defined in
* referencedata.geographic_levels.csv
* referencedata.geographic_zones.csv.

| Name (code)                                      | Level    |
|--------------------------------------------------|----------|
| Malawi (Malawi)                                  | Country  |
| Central Region  (Malawi-Central)                 | Region   |
| Northern Region (Malawi-Northern)                | Region   |
| Southern Region (Malawi-Southern)                | Region   |
| Dedza District (Malawi-Central-Dedza)            | District |
| Dowa District (Malawi-Central-Dowa)              | District |
| Kasungu District (Malawi-Central-Kasungu)        | District |
| Lilongwe District (Malawi-Central-Lilongwe)      | District |
| Mchinji District (Malawi-Central-Mchinji)        | District |
| Nkhotakota District (Malawi-Central-Nkhotakota)  | District |
| Ntcheu District (Malawi-Central-Ntcheu)          | District |
| Ntchisi District (Malawi-Central-Ntchisi)        | District |
| Salima District (Malawi-Central-Salima)          | District |
| Chitipa District (Malawi-Northern-Chitipa)       | District |
| Karonga District (Malawi-Northern-Karonga)       | District |
| Likoma District (Malawi-Northern-Likoma)         | District |
| Mzimba District (Malawi-Northern-Mzimba)         | District |
| Nkhata Bay District (Malawi-Northern-Nkhata Bay) | District |
| Rumphi District (Malawi-Northern-Rumphi)         | District |
| Balaka District (Malawi-Southern-Balaka)         | District |
| Blantyre District (Malawi-Southern-Blantyre)     | District |
| Chikwawa District (Malawi-Southern-Chikwawa)     | District |
| Chiradzulu District (Malawi-Southern-Chiradzulu) | District |
| Machinga District (Malawi-Southern-Machinga)     | District |
| Mangochi District (Malawi-Southern-Mangochi)     | District |
| Mulanje District (Malawi-Southern-Mulanje)       | District |
| Mwanza District (Malawi-Southern-Mwanza)         | District |
| Nsanje District (Malawi-Southern-Nsanje)         | District |
| Thyolo District (Malawi-Southern-Thyolo)         | District |
| Phalombe District (Malawi-Southern-Phalombe)     | District |
| Zomba District (Malawi-Southern-Zomba)           | District |
| Neno District (Malawi-Southern-Neno)             | District |
| Balaka (Malawi-Southern-Balaka-Balaka)           | City     |
| Mozambique (moz)                                 | Country  |
| Niassa (niassa)                                  | Region   |
| Cuamba (cuamba)                                  | District |
| Lichinga (lichinga-distrito)                     | District |

## Programs

Defined in
* referencedata.programs.csv

| Name (code)             | active | Periods skippable | Skip authorization | Non full supply tab | Date physical stock count completed |
|-------------------------|--------|-------------------|--------------------|---------------------|-------------------------------------|
|Family Planning (PRG001) | true   | true              | false              | true                | false                               |
|Essential Meds (PRG002)  | true   | false             | false              |                     | false                               |
|New Program (PRG003)     | true   | false             | false              | false               | false                               |
|EPI (PRG004)             | true   | false             | true               | false               | false                               |
|CHAZ (PRG005)            | true   | true              | false              | true                | false                               |

## Facilities

The facilities described below are defined in these files:
* referencedata.facilities.csv,
* referencedata.facility_operators.csv,
* referencedata.facility_types.csv,
* referencedata.supported_programs.csv,
* referencedata.supervisory_nodes.csv,
* referencedata.supply_lines.csv.

| Name (Code)                         | Type              | Programs                        | Geo Zone           | Home Facility for                    | Req Group    |
|-------------------------------------|-------------------|---------------------------------|--------------------|--------------------------------------|--------------|
| Comfort Health Clinic (HC01)        | Health Center     | Family Planning, Essential Meds | Neno District      | administrator, srmanager2, smanager2 | RGFP1, RGEM1 |
| Kankao Health Facility (HF01)       | Health Center     | Family Planning, Essential Meds | Zomba District     | srmanager4, smanager4                | RGEM1, RGFP2 |
| Nandumbo Health Center (HC02)       | Health Center     | Family Planning                 | Phalombe District  | srmanager1, smanager1                | RGFP1        |
| Kalembo Health Center (HC03)        | Health Center     | Family Planning, Essential Meds | Thyolo District    | srmanager3, smanager3                | RGEM1, RGFP3 |
| Balaka District Hospital (DH01)     | District Hospital | Family Planning, Essential Meds | Balaka (City)      | dsrmanager                           | RGEM1, RGFP2 |
| Ntcheu District Warehouse (WH01)    | Warehouse         | Family Planning                 | Ntcheu District    | devadmin, wclerk1                    |              |
| Balaka District Warehouse (WH02)    | Warehouse         | Family Planning, Essential Meds | Balaka (City)      | wclerk2                              |              |
| Mozambique Central Warehouse (W001) | Warehouse         | EPI                             | Mozambique Country | vwclerk1                             |              |
| Depósito Provincial Niassa (P001)   | Provincial Store  | EPI                             | Niassa Province    | rivo                                 |              |
| Depósito Distrital Cuamba (D001)    | District Store    | EPI                             | Cuamba District    | divo1, vsrmanager2                   | RGEPI1       |
| Depósito Distrital Lichinga (D002)  | District Store    | EPI                             | Lichinga District  | divo2                                | RGEPI1       |
| Lusaka Province Store (ZM-PS-L1)    | Province Store    | CHAZ                            | Southern Region    |                                      |              |
| Lusaka Province Warehouse (ZM-W-L1) | Warehouse         | CHAZ                            | Southern Region    |                                      |              |
| Lusaka District Store (ZM-DS-L1)    | District Store    | CHAZ                            | Balaka District    |                                      | RG-CHAZ-1    |
| Lusaka District Warehouse (ZM-W-L2) | Warehouse         | CHAZ                            | Balaka District    |                                      |              |
| Roma, Lusaka (ZM-HC-L4)             | Health Center     | CHAZ                            | Balaka             | chaz                                 | RG-CHAZ-2    |


There are 16 facilities in the Cuamba district, all health centers. Though all of them can be used to demo, it is recommended to use the following facility:

| Name (Code)   | Type          | Programs | Geo Zone        | Req Group |
|---------------|---------------|----------|-----------------|-----------|
| Cuamba (N003) | Health Center | EPI      | Cuamba District | RGEPI2    |

There are 25 facilities in the Lichinga district, all health centers. Though all of them can be used to demo, it is recommended to use the following facility:

| Name (Code)     | Type          | Programs | Geo Zone          | Req Group |
|-----------------|---------------|----------|-------------------|-----------|
| Assumane (N036) | Health Center | EPI      | Lichinga District | RGEPI3    |

There are 11 facilities in the Lusaka city, all health centers. Though all of them can be used to demo the requisition split feature, it is recommended to use the following facility:

| Name (Code)             | Type         | Programs | Geo Zone      | Req Group |
|-------------------------|--------------|----------|---------------|-----------|
| Roma, Lusaka (ZM-HC-L4) |Health Center | CHAZ     | Balaka        | RG-CHAZ-2 |

Additionally, there are many other facilities in other districts, but they are not used except to populate the system.

## Products

Defined in
* referencedata.orderables.csv
* referencedata.program_orderables.csv
* referencedata.lots.csv


| Name (code)                                              | Category                          | Type                      | Program         | VVM   | Lot (expires)                                    |
|----------------------------------------------------------|-----------------------------------|---------------------------|-----------------|-------|--------------------------------------------------|
| Levora (C100)                                            | Oral Contraceptive                | Commodity Type            | Family Planning | false |                                                  |
| Ortho-Novum (C200)                                       | Oral Contraceptive                | Commodity Type            | Family Planning | false |                                                  |
| Depo-Estradiol (C300)                                    | Injectable Hormonal Contraceptive | Commodity Type            | Family Planning | false |                                                  |
| Male Condom (C400)                                       | Barrier Method                    | Commodity Type            | Family Planning | false |                                                  |
| Implanon  (C500)                                         | Implantable Contraceptive         | Commodity Type            | Family Planning | false |                                                  |
| Levonorgestrel (C600)                                    | Implantable Contraceptive         | Commodity Type            | Family Planning | false |                                                  |
| Acetylsalicylic Acid (C1)                                | Antibiotics                       | Trade Item                | Essential Meds  | false |                                                  |
| Glibenclamide (C2)                                       | Antibiotics                       | Trade Item                | Essential Meds  | false |                                                  |
| Streptococcus Pneumoniae Vaccine (C3)                    | Vaccines                          | Trade Item                | Essential Meds  | false |                                                  |
| Streptococcus Pneumoniae Vaccine II (C4)                 | Vaccines                          | Commodity Type            | Essential Meds  | false |                                                  |
| Depo-Estradiol (C300)                                    | Injectable Hormonal Contraceptive | Orderable                 | New program     | false |                                                  |
| BCG (bcg20)                                              | Vaccines                          | Commodity Type, Orderable | EPI             | true  | BCGI2017A (2019-06-01), BCGI2017B (2019-07-01)   |
| Polio                                                    | Vaccines                          | Commodity Type            | EPI             | true  |                                                  |
| Polio (20 dose) (polio20)                                | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| IPV (ipv5)                                               | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Pentavalent                                              | Vaccines                          | Commodity Type            | EPI             | true  |                                                  |
| Pentavalent (1 dose) (penta1)                            | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Pentavalent (10 dose) (penta10)                          | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| PCV (pcv10)                                              | Vaccines                          | Commodity Type, Orderable | EPI             | true  |                                                  |
| Rotavirus (rota1)                                        | Vaccines                          | Commodity Type, Orderable | EPI             | true  | ROTAM2017A (2019-06-01), ROTAM2017B (2019-07-01) |
| Measles (measles10)                                      | Vaccines                          | Commodity Type, Orderable | EPI             | true  |                                                  |
| Tetanus (tetanus10)                                      | Vaccines                          | Commodity Type, Orderable | EPI             | true  |                                                  |
| Syringe 5ml (syringe5ml)                                 | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Syringe 0.5ml (syringe05ml)                              | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Syringe 0.05ml (syringe005ml)                            | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Safety Box (safetybox)                                   | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Gas (Cylinder) (gas)                                     | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Diluent BCG (bcg20dil)                                   | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Diluent Measles (measles10dil)                           | Vaccines                          | Orderable                 | EPI             | true  |                                                  |
| Nadolol (nadolol)                                        | Barrier method                    | Orderable                 | CHAZ            | false |                                                  |
| Acetaminophen And Codeine (acetaminophen-codeine)        | Oral contraceptive                | Orderable                 | CHAZ            | false |                                                  |
| Gesteira (gesteira)                                      | Barrier method                    | Orderable                 | CHAZ            | false |                                                  |
| Utah Juniper (utah-juniper)                              | Anti-fungal                       | Orderable                 | CHAZ            | false |                                                  |
| nasal (nasal)                                            | Anti-fungal                       | Orderable                 | CHAZ            | false |                                                  |
| Glytone essentials rejuvenate daily (glytone-rejuvenate) | Analgesics                        | Orderable                 | CHAZ            | false |                                                  |
| Ampicillin (ampicillin)                                  | Implantable contraceptive         | Orderable                 | CHAZ            | false |                                                  |
| Fever - Infection (Fever-infection)                      | Injectable hormonal contraceptive | Orderable                 | CHAZ            | false |                                                  |
| ZERIT (zerit)                                            | Vaccines                          | Orderable                 | CHAZ            | false |                                                  |
| Lovenox (lovenox)                                        | Oral contraceptive                | Orderable                 | CHAZ            | false |                                                  |
| Duloxetine (duloxetine)                                  | Analgesics                        | Orderable                 | CHAZ            | false |                                                  |
| RELENZA (relenza)                                        | Oral contraceptive                | Orderable                 | CHAZ            | false |                                                  |

Currently, all the products are generally approved at all the facility types. In the future we
could add more differentiation by types of facilities in 
* referencedata.facility_type_approved_products.csv.

## Requisition Groups and Schedules

Defined in
* referencedata.processing_schedules.csv
* referencedata.requisition_groups.csv
* referencedata.requisition_group_members.csv
* referencedata.requisition_group_program_schedules.csv

| Name (code)                            | Schedule                                    | Facilities             |
|----------------------------------------|---------------------------------------------|------------------------|
| RG Family Planning 1 (RGFP1)           | Monthly (Jan2017, Feb2017, ...)             | HC01, HC02             |
| RG Essential Meds 1 (RGEM1)            | Quarterly (Q1, Q2, Q3, Q4 of 2017 and 2018) | HC01, HC03, DH01, HF01 |
| RG Family Planning 2 (RGFP2)           | Quarterly (Q1, Q2, Q3, Q4 of 2017 and 2018) | DH01, HF01             |
| RG Family Planning 3 (RGFP3)           | Monthly A (Jan2018, Feb2018, ...)           | HC03                   |
| RG EPI 1 (districts) (RGEPI1)          | Quarterly                                   | D001, D002             |
| RG EPI 2 (health facilities) (RGEPI2)  | Monthly                                     | N003                   |
| RG EPI 3 (health facilities) (RGEPI3)  | Monthly                                     | N036                   |
| RG CHAZ 1 (districts) (RG-CHAZ-1)      | Monthly                                     | ZM-DS-L1               |
| RG CHAZ 2 (health centers) (RG-CHAZ-2) | Monthly                                     | ZM-HC-L4               |
    
There are also 2 unused schedules, SCH004 and SCH005.

## Roles, Users and Rights

Defined in
* referencedata.users.csv
* referencedata.roles.csv
* referencedata.role_rights.csv
* referencedata.role_assignments.csv

1. Storeroom Manager
  * rights:
    * Requisitions View
    * Requisitions Delete
    * Requisitions Create
    * Proofs of Delivery Manage
  * users:
    * administrator - has this role for Family Planning & Essential Meds at SN1/FP approval point and SN2/EM approval point
    * srmanager2, srmanager4 - has this role for Family Planning & Essential Meds at home facility
    * srmanager1 - has this role for Family Planning at home facility
    * srmanager3 - has this role for Essential Meds & Family Planning at home facility
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * vsrmanager1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
    * chaz - has this role for CHAZ at Lusaka district approval point (CHAZ), Lusaka province
        approval point (CHAZ) and home facility
2. Store Manager
  * rights:
    * Requisitions View
    * Requisitions Delete
    * Requisitions Authorize
  * users:
    * administrator - has this role for Family Planning & Essential Meds at home facility, SN1/FP approval point
        and SN2/EM approval point
    * smanager1 - has this role for Family Planning at home facility
    * smanager2 - has this role for Family Planning at home facility and SN1/FP approval point
        & for Essential Meds at home facility and SN2/EM approval point
    * smanager3 - has this role for Essential Meds & Family Planning at home facility
    * smanager4 - has this role for Family Planning & Essential Meds at home facility
    * divo1 - has this role for EPI at home facility
    * divo2 - has this role for EPI at home facility
    * rivo - has this role for EPI at home facility
    * vsrmanager1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
    * chaz - has this role for CHAZ at Lusaka district approval point (CHAZ), Lusaka province
        approval point (CHAZ) and home facility
3. District Storeroom Manager
  * rights:
    * Requisitions View
    * Requisitions Approve
  * users:
    * administrator - has this role for Family Planning & Essential Meds at SN2/EM approval
        point and SN1/FP approval point
    * dsrmanager - has this role for Family Planning & Essential Meds at SN2/EM approval
        point, SN1.1/FP approval sub point and SN1.2/FP approval sub point 2
    * chaz - has this role for CHAZ at Lusaka district approval point (CHAZ), Lusaka province
        approval point (CHAZ) and home facility
4. Program Supervisor
  * rights:
    * Requisitions View
    * Requisitions Approve
  * users:
    * administrator - has this role for Family Planning & Essential Meds at SN2/EM approval
        point and SN1/FP approval point
    * psupervisor - has this role for Family Planning & Essential Meds at SN1/FP approval
        point supervisory node
    * divo1 - has this role for EPI at SN-CUAMBA-DIST/Cuamba district approval point
    * rivo - has this role for EPI at SN-NIASSA-PROV/Niassa province approval point
    * chaz - has this role for CHAZ at Lusaka district approval point (CHAZ), Lusaka province
        approval point (CHAZ) and home facility
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
    * chaz - has this role at Lusaka Province Warehouse, Lusaka District Warehouse
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
    * chaz - has this role for CHAZ at Lusaka district approval point (CHAZ), Lusaka province
        approval point (CHAZ) and home facility
8. Stock Viewer
  * rights:
    * Stock Cards View
  * users:
    * wclerk1 - has this role for Family Planning at home facility
9. Requisition Viewer
  * rights:
    * Requisitions View
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
    * Manage stock sources
    * Manage stock organizations
    * Manage programs
    * Manage stock destinations
    * Manage stock card line item reasons
    * View rights
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
13. Reporting Personnel
  * rights:
    * Edit report templates
    * View reports
  * users:
    * administrator


Passwords for these user accounts come from the
[Authentication Service's demo data](https://github.com/OpenLMIS/openlmis-auth/tree/master/src/main/resources/db/demo-data).

Rights themselves come from the 
[bootstrap data](https://github.com/OpenLMIS/openlmis-referencedata/blob/master/src/main/resources/db/migration/20170206205310272__initial_bootstrap_data.sql). **NOTE:** This SQL script does not have all of 
the rights, as additional rights have been added to later migrations.

## Supervisory Nodes and Supply Lines

Defined in
* referencedata.supervisory_nodes.csv
* referencedata.supply_lines.csv
* referencedata.requisition_groups.csv

| Name (code)                                                 | Req group | Program         | Supply Lines | Facility | Parent              |
|-------------------------------------------------------------|-----------|-----------------|--------------|----------|---------------------|
| FP approval point (SN1)                                     | RGFP1     | Family Planning | WH01         | HC01     |                     |
| FP Approval sub point (SN1.1)                               | RGFP2     | Family Planning | DH01         |          | SN1                 |
| FP Approval sub point 2 (SN1.2)                             | RGFP3     | Family Planning | DH01         |          | SN1                 |
| EM approval point (SN2)                                     | RGEM1     | Essential Meds  | WH02         | DH01     |                     |
| Niassa province approval point (SN-NIASSA-PROV)             | RGEPI1    | EPI             | W001         | P001     |                     |
| Cuamba district approval point (SN-CUAMBA-DIST)             | RGEPI2    | EPI             | D001         | D001     | SN-NIASSA-PROV      |
| Lichinga district approval point (SN-LICHINGA-DIST)         | RGEPI3    | EPI             | D002         |          | SN-NIASSA-PROV      |
| Lusaka province approval point (CHAZ) (SN-LUSAKA-PROV-CHAZ) | RG-CHAZ-1 | CHAZ            | ZM-W-L1      | ZM-PS-L1 |                     |
| Lusaka district approval point (CHAZ) (SN-LUSAKA-DIST-CHAZ) | RG-CHAZ-2 | CHAZ            | ZM-W-L2      | ZM-DS-L1 | SN-LUSAKA-PROV-CHAZ |

## Ideal Stock Amounts

Defined in
* referencedata.ideal_stock_amounts.csv.

Only in the EPI program. These amounts are defined for:
 
* Health facilities (all 41)
  * All monthly periods in 2017 and 2018
  * All seven commodity types
  * Values range from 1,000 - 10,000
* District stores (2)
  * All quarterly periods in 2017 and 2018
  * All seven commodity types 
  * Values range from 10,000 - 99,999

## Supply Partners

Defined in
* referencedata.supply_partners.csv
* referencedata.supply_partner_associations.csv
* referencedata.supply_partner_association_facilities.csv
* referencedata.supply_partner_association_orderables.csv

1. CHAZ
  * has two associations
    * for CHAZ program at Lusaka province approval point (CHAZ)
    * for CHAZ program at Lusaka district approval point (CHAZ)
    * both contain all facilities from a related requisition groups
    * each supports only half of available orderables for the CHAZ program

# Additional Information

The directory `demo-datasets/` holds IDs that are expected to exist from demo-data.
Therefore changing ID's in demo-data may need for these files to be re-generated.

The directory `schemas/` holds copies of the Mockaroo schemas used for generating
data. When the set of schemas on Mockaroo changes, please revision them here.

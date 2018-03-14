insert into orderables (type, id, dispensingunit, fullproductname, packroundingthreshold, netcontent, code, roundtozero, manufactureroftradeitem)
values ('TRADE_ITEM', '4d1115de-0f60-408a-8a1e-44401e20a5b0', 'unit', 'productname1', 10, 20, 'Code1', false, 'manufacturer1');

insert into orderables (type, id, dispensingunit, fullproductname, packroundingthreshold, netcontent, code, roundtozero, classificationSystem, classificationId)
values ('COMMODITY_TYPE', '23856848-63c9-4807-9470-603b2ddc33fa', 'unit', 'parentname', 10, 20, 'CodeParent', false, 'cSys', 'cId1');
insert into orderables (type, id, dispensingunit, fullproductname, packroundingthreshold, netcontent, code, roundtozero, classificationSystem, classificationId, parentid)
values ('COMMODITY_TYPE', '4d1115de-0f60-408a-8a1e-44401e20a5b1', 'unit', 'productname2', 10, 20, 'Code2', false, 'cSys', 'cId2', '23856848-63c9-4807-9470-603b2ddc33fa');

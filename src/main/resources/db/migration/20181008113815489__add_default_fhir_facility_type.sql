INSERT INTO referencedata.facility_types (id, code, name, displayOrder, active)
SELECT 'ae9715b4-2a72-4769-8121-e3894aec5b70', 'FHIR-FT', 'FHIR''s facility type', MAX(displayOrder) + 1, TRUE
FROM referencedata.facility_types;

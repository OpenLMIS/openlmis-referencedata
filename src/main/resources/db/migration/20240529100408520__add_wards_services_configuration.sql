CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Add new geographic level
INSERT INTO referencedata.geographic_levels (id, code, levelnumber, "name")
VALUES (
    '533a0771-bf2b-414a-91b2-6824d7df281d',
    'local',
    (SELECT COALESCE(MAX(levelnumber), 0) + 1 FROM referencedata.geographic_levels),
    'local'
);

-- Add new facility type
INSERT INTO referencedata.facility_types (id, active, code, description, displayorder, "name")
VALUES (
    '974b4867-c0d7-4a53-b350-e95b8e7a8d82',
    true,
    'WS',
    'Represents wards and services.',
    (SELECT COALESCE(MAX(displayorder), 0) + 1 FROM referencedata.facility_types),
    'Ward/Service'
);

-- Create new geographic zone for all facilities and update facilities with the new geographic zone ID
DO $$
DECLARE
    facility RECORD;
    new_geo_zone_id UUID;
BEGIN
    FOR facility IN SELECT * FROM referencedata.facilities LOOP
        new_geo_zone_id := uuid_generate_v4();
        
        INSERT INTO referencedata.geographic_zones (
            id, catchmentpopulation, code, latitude, longitude, "name", levelid, parentid, boundary, extradata
        ) VALUES (
            new_geo_zone_id,
            NULL,
            'gz-' || facility.code,
            NULL,
            NULL,
            facility."name",
            '533a0771-bf2b-414a-91b2-6824d7df281d',
            facility.geographiczoneid,
            NULL,
            NULL
        );

        UPDATE referencedata.facilities
        SET geographiczoneid = new_geo_zone_id
        WHERE id = facility.id;
    END LOOP;
END $$;

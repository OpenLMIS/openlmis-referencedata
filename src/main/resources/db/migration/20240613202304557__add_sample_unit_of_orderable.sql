INSERT INTO unit_of_orderables (id, name, description, displayOrder, factor)
    SELECT 'c86e7d33-f8f8-4e0d-b540-89b16ffd71f2','Single Dose','single dose - set for data stored before adding unit of orderables', 1, 1
WHERE
    NOT EXISTS (
        SELECT id FROM unit_of_orderables WHERE name = 'Single Dose' OR id ='c86e7d33-f8f8-4e0d-b540-89b16ffd71f2'
    );

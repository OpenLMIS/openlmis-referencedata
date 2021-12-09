DO
$$
    DECLARE
        assignment RECORD;
    BEGIN
        FOR assignment IN SELECT facilityid, programid, userid
                          FROM referencedata.right_assignments
                          WHERE rightname = 'REQUISITION_APPROVE'
            LOOP
                INSERT INTO referencedata.right_assignments (id, rightname, facilityid, programid, userid)
                VALUES (uuid_generate_v4(),
                        'ORDER_CREATE',
                        assignment.facilityid,
                        assignment.programid,
                        assignment.userid);
            END LOOP;
    END
$$;
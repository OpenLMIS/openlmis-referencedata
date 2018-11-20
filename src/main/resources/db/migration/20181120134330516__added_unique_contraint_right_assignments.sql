DELETE FROM referencedata.right_assignments ra1 USING referencedata.right_assignments ra2
WHERE ra1.ctid < ra2.ctid
  AND ra1.rightname = ra2.rightname
  AND ra1.facilityid = ra2.facilityid
  AND ra1.programid = ra2.programid
  AND ra1.userid = ra2.userid;

ALTER TABLE ONLY referencedata.right_assignments
    ADD CONSTRAINT right_assignment_unq UNIQUE (rightname, facilityid, programid, userid);
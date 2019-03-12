ALTER TABLE ONLY roles DROP CONSTRAINT IF EXISTS uk_ofx66keruapi6vyqpv6f2or37;
CREATE UNIQUE INDEX unq_role_name ON roles (LOWER(name));

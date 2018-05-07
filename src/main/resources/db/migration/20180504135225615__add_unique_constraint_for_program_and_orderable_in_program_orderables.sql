CREATE UNIQUE INDEX unq_orderableid_programid
ON program_orderables(orderableid, programid)
WHERE (active = TRUE)
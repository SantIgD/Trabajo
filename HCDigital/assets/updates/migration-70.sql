-------------------------------------------
-- Migration script for database version 70
-------------------------------------------

ALTER TABLE hcd_atencion ADD COLUMN atencionServerID INTEGER;

UPDATE hcd_atencion SET atencionServerID = 0;
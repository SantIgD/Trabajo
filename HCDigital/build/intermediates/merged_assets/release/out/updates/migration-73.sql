-------------------------------------------
-- Migration script for database version 73
-------------------------------------------

ALTER TABLE hcd_atencion ADD COLUMN syncHeader SMALLINT;

UPDATE hcd_atencion SET syncHeader = 0;
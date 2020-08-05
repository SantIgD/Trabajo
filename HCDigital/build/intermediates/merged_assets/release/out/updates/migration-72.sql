-------------------------------------------
-- Migration script for database version 72
-------------------------------------------

ALTER TABLE hcd_atencion ADD COLUMN syncfirmaDigital SMALLINT;

UPDATE hcd_atencion SET syncfirmaDigital = 0;
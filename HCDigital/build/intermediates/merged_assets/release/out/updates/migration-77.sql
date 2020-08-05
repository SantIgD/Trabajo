-------------------------------------------
-- Migration script for database version 77
-------------------------------------------

ALTER TABLE hcd_atencion ADD COLUMN idEstado SMALLINT;

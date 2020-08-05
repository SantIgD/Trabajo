-------------------------------------------
-- Migration script for database version 77
-------------------------------------------

ALTER TABLE hcd_atencionCerrada ADD COLUMN jsonPreGuardado STRING;
ALTER TABLE hcd_atencionCerrada ADD COLUMN jsonPostGuardado STRING;

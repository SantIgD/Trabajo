-------------------------------------------
-- Migration script for database version 64
-------------------------------------------
-- application: HCDigital
-- autor: tecso.coop
-- date: 08/04/2014

ALTER TABLE hcd_atencion ADD COLUMN despachador_id INTEGER;
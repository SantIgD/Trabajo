-------------------------------------------
-- Migration script for database version 58
-------------------------------------------
-- application: HCDigital
-- autor: tecso.coop
-- date: 04/09/2013

ALTER TABLE hcd_atencionValor ADD COLUMN imagen BLOB;
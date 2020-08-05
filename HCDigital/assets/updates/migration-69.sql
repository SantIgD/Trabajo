-------------------------------------------
-- Migration script for database version 69
-------------------------------------------

ALTER TABLE hcd_atencionValor ADD COLUMN thumbnail BLOB;

UPDATE hcd_atencionValor SET thumbnail = imagen;
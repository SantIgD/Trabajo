-------------------------------------------
-- Migration script for database version 71
-------------------------------------------

ALTER TABLE hcd_atencionValor ADD COLUMN syncImagen SMALLINT;

UPDATE hcd_atencionValor SET syncImagen = 0;
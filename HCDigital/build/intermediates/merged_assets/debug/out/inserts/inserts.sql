--Declare DATETIME('NOW')	DateTime
--Set DATETIME('NOW') = CURRENT_TIMESTAMP
--
--delete from apm_tablaVersion

-- apm_tablaVersion - (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (1, 'hcd_entidadbusqueda', 0, 'Tecso', DATETIME('NOW'), 0, 1)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (2, 'hcd_estadoatencion', 0, 'Tecso', DATETIME('NOW'), 0, 1)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (3, 'hcd_motivocierreatencion', 0, 'Tecso', DATETIME('NOW'), 0, 1)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (4, 'hcd_atencion', 0, 'Tecso', DATETIME('NOW'), 0, 1)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (5, 'hcd_atencionvalor', 0, 'Tecso', DATETIME('NOW'), 0, 1)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (6, 'hcd_erroratencion', 0, 'Tecso', DATETIME('NOW'), 0, 1)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (7, 'apm_aplicacionParametro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (8, 'hcd_despachador', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (18, 'apm_aplicacionTabla', 0, 'Tecso', DATETIME('NOW'), 0, 0)

INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (25, 'hcd_ritmoElectro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (26, 'hcd_segmentoElectro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (27, 'hcd_derivacionesSegmentoElectro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (28, 'hcd_ondaTElectro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (29, 'hcd_derivacionesOndaTElectro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (30, 'hcd_bloqueoRamaElectro', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (31, 'apm_condicionAlerta', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (32, 'apm_gestionAccion', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (33, 'hcd_score', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (34, 'hcd_reglaCondicion', 0, 'Tecso', DATETIME('NOW'), 0, 0)
INSERT INTO apm_tablaVersion (id, tabla, lastVersion, modificationUser, modificationTimeStamp, deleted, version) Values (35, 'hcd_regla', 0, 'Tecso', DATETIME('NOW'), 0, 0)
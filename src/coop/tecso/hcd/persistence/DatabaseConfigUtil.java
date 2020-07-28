package coop.tecso.hcd.persistence;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

import coop.tecso.hcd.entities.AccionHC;
import coop.tecso.hcd.entities.AplicacionTablaHC;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.AtencionCerrada;
import coop.tecso.hcd.entities.AtencionValor;
import coop.tecso.hcd.entities.BloqueoRamaElectro;
import coop.tecso.hcd.entities.CondicionAlertaHC;
import coop.tecso.hcd.entities.DerivacionesOndaTElectro;
import coop.tecso.hcd.entities.DerivacionesSegmElectro;
import coop.tecso.hcd.entities.Despachador;
import coop.tecso.hcd.entities.EntidadBusqueda;
import coop.tecso.hcd.entities.ErrorAtencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.entities.MotivoCierreAtencion;
import coop.tecso.hcd.entities.OndaTElectro;
import coop.tecso.hcd.entities.Regla;
import coop.tecso.hcd.entities.ReglaCondicion;
import coop.tecso.hcd.entities.RitmoElectro;
import coop.tecso.hcd.entities.Score;
import coop.tecso.hcd.entities.SegmentoElectro;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;
import coop.tecso.udaa.domain.base.TablaVersion;

public final class DatabaseConfigUtil extends OrmLiteConfigUtil {

	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("db_config.txt", PERSISTENT_CLASSES);
	}

	/**
	 * Classes to be persisted.
	 */
	public static final Class<?> [] PERSISTENT_CLASSES =  new Class<?> []{
		Atencion.class,
		AtencionValor.class,
		EstadoAtencion.class,
		MotivoCierreAtencion.class,
		EntidadBusqueda.class,
		TablaVersion.class,
		ErrorAtencion.class,
		AplicacionParametro.class,
		Despachador.class,
		DerivacionesOndaTElectro.class,
		DerivacionesSegmElectro.class,
		OndaTElectro.class,
		RitmoElectro.class,
		SegmentoElectro.class,
		BloqueoRamaElectro.class,
		AplicacionTablaHC.class,
		CondicionAlertaHC.class,
		AtencionCerrada.class,
		AccionHC.class,
		Score.class,
		Regla.class,
		ReglaCondicion.class
	};

/**
* Classes to be migrated.
*/
	public static final Class<?> [] IMMUTABLE_CLASSES =  new Class<?> []{
		Atencion.class,
		AtencionValor.class,
		AtencionCerrada.class
	};

}
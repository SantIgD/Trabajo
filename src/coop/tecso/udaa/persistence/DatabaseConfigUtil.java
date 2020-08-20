package coop.tecso.udaa.persistence;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionSync;
import coop.tecso.udaa.domain.aplicaciones.AplicacionTabla;
import coop.tecso.udaa.domain.aplicaciones.AplicacionTipoBinario;
import coop.tecso.udaa.domain.base.TablaVersion;
import coop.tecso.udaa.domain.domicilio.Barrio;
import coop.tecso.udaa.domain.domicilio.Calle;
import coop.tecso.udaa.domain.domicilio.Ciudad;
import coop.tecso.udaa.domain.domicilio.Provincia;
import coop.tecso.udaa.domain.domicilio.SucursalDomicilio;
import coop.tecso.udaa.domain.domicilio.Zona;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.notificaciones.EstadoNotificacion;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.domain.notificaciones.TipoNotificacion;
import coop.tecso.udaa.domain.padron.TipoDocumento;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.PerfilAcceso;
import coop.tecso.udaa.domain.perfiles.PerfilAccesoAplicacion;
import coop.tecso.udaa.domain.perfiles.PerfilAccesoUsuario;
import coop.tecso.udaa.domain.perfiles.Seccion;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.Sucursal;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.domain.seguridad.UsuarioAppDM;
import coop.tecso.udaa.domain.trazabilidad.UbicacionGPS;

public final class DatabaseConfigUtil extends OrmLiteConfigUtil {

	public static void main(String[] args) throws SQLException, IOException {
		writeConfigFile("db_config.txt", PERSISTENT_CLASSES);		
	}
	
	/**
	 * Classes to be persisted.
	 */
	static final Class<?> [] PERSISTENT_CLASSES =  new Class<?> [] {
		Aplicacion.class,
		AplicacionBinarioVersion.class,		
		AplicacionParametro.class,
		AplicacionPerfil.class,
		AplicacionPerfilSeccion.class,
		AplicacionTabla.class,
		AplicacionTipoBinario.class,
		AplPerfilSeccionCampo.class,
		AplPerfilSeccionCampoValor.class,
		AplPerfilSeccionCampoValorOpcion.class,
		Campo.class,
		CampoValor.class,
		CampoValorOpcion.class,
		DispositivoMovil.class,
		EstadoNotificacion.class,
		Notificacion.class,
		PerfilAcceso.class,
		PerfilAccesoAplicacion.class,
		PerfilAccesoUsuario.class,
		Seccion.class,
		Sucursal.class,
		TablaVersion.class,
		TipoNotificacion.class,
		UsuarioApm.class,
		UsuarioAppDM.class,
		UbicacionGPS.class,
		DetalleReporteError.class,
		ReporteError.class,
		AplicacionSync.class,
		Barrio.class,
		Calle.class,
		Zona.class,
		SucursalDomicilio.class,
		Provincia.class,
		Ciudad.class,
		TipoDocumento.class
	};

	/**
	 * Classes to be migrated.
	 */
	static final Class<?> [] IMMUTABLE_CLASSES =  new Class<?> [] {
		Barrio.class,
		Calle.class,
		Zona.class,
		SucursalDomicilio.class,
		Provincia.class,
		Ciudad.class,
		TipoDocumento.class
	};
}
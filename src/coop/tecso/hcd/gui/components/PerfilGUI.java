package coop.tecso.hcd.gui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.CepoController;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.Campo;

@SuppressWarnings("WeakerAccess")
public final class PerfilGUI extends Component {

	private AlertDispatcher alertDispatcher;

	// Entidad que representa a la principal entidad de negocio de la aplicacion (la entidad que tiene asociada un perfil para su formulario de registro)
	private AbstractEntity bussEntity;

	private CepoController cepoController;

	// MARK: - Constructores

	public PerfilGUI(Context context) {
		this(context,true);
	}

	public PerfilGUI(Context context, boolean enabled) {
		super();
		this.context = context;
		this.enabled = enabled;

		// Lanzador de Alertas
		HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
		this.alertDispatcher = appState.getAlertDispatcher();
		this.alertDispatcher.onInit(this, context);
	}

	// MARK: -  Getter y Setters
	public AbstractEntity getBussEntity() {
		return bussEntity;
	}
	public void setBussEntity(AbstractEntity bussEntity) {
		this.bussEntity = bussEntity;
	}
	public AlertDispatcher getAlertDispatcher() {
		return alertDispatcher;
	}

	private CampoGUI campoGUISelected;
	public CampoGUI getCampoGUISelected() {
		return campoGUISelected;
	}

	public void setCampoGUISelected(CampoGUI campoGUI) {
		this.campoGUISelected = campoGUI;
	}

	// MARK: - Metodos

	@Override
	public void addValue(Value value) { }

	@Override
	public List<Value> values() {
		if (this.components == null) {
			return new ArrayList<>();
		}

		List<Value> values = new ArrayList<>();

		for (Component section: this.components) {
			SeccionGUI seccion = (SeccionGUI) section;
			if (seccion.isVisible()) {
				values.addAll(seccion.values());
			}
		}

		return values;
	}

	public List<Value> dirtyValues() {
		if (this.components == null) {
			return new ArrayList<>();
		}

		List<Value> values = new ArrayList<>();
		for (Component section: this.components) {
			SeccionGUI seccion = (SeccionGUI) section;
			if (seccion.isVisible()) {
				values.addAll(seccion.dirtyValues());
			}
		}
		return values;
	}

	@Override
	public boolean isDirty() {
		if (super.isDirty()) {
			return true;
		}

		// Recorremos los elementos de la perfil verificando si alguno fue modificado
		for (Component section: this.components) {
			SeccionGUI seccion = (SeccionGUI) section;
			if (seccion.isDirty()) {
				return true;
			}
		}

		return super.isDirty();
	}

	@Override
	public boolean validate() {
		for (Component section: this.components) {
			SeccionGUI seccion = (SeccionGUI) section;
			if (seccion.isVisible() && !seccion.validate()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public View build() {
		// Se define un layout lineal vertica para ubicar: 'Label EditText'
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// Se agregan los componentes view correspondientes a las secciones al layout contenedor
		if (this.components != null) {
			for (Component section: this.components) {
				layout.addView(section.getView());
			}
		}

		this.view = layout;
		this.view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
			this.cepoController.showNeededGUIComponents();
		});

		this.cepoController = new CepoController(this);

		return this.view;
	}

	@Override
	public View redraw() {
		if (this.components != null) {
			for (Component section: this.components) {
				section.redraw();
			}
		}
		return this.view;
	}

	@Override
	public View disable() {
		if (this.components != null) {
			for (Component section: this.components) {
				section.disable();
			}
		}
		return this.view;
	}

	/**
	 * Se ejecuta el evaluador de alertas sobre todo el formulario. Util para evaluar valores precargados o iniciales.
	 */
	public void runEvalAll() {
		this.alertDispatcher.evalAll(this);
	}

	/**
	 * Evalúa las alertas sobre el formulario
	 * @return true si hay alguna alerta.
	 */
	public boolean runEvalAllCierre() {
		return this.alertDispatcher.evalAllCierre(this);
	}

	/**
	 * Muestra la seccion oculta para el idSeccion indicado
	 */
	public void mostrarSeccion(int idSeccion) {
		if (this.components == null) {
			return;
		}

		for (Component component : this.components) {
			SeccionGUI seccion = (SeccionGUI) component;
			if (seccion.isOpcional()) {
				AplicacionPerfilSeccion aplPerfilSeccion = (AplicacionPerfilSeccion) seccion.getEntity();
				if (aplPerfilSeccion.getSeccion().getId() == idSeccion) {
					seccion.mostrar();
				}
			}
		}
	}

	/**
	 * Oculta la seccion para el idSeccion indicado
	 */
	public void ocultarSeccion(int idSeccion) {
		if (this.components == null) {
			return;
		}

		for (Component component : this.components) {
			SeccionGUI seccion = (SeccionGUI) component;
			if (seccion.isOpcional()) {
				AplicacionPerfilSeccion aplPerfilSeccion = (AplicacionPerfilSeccion) seccion.getEntity();
				if (aplPerfilSeccion.getSeccion().getId() == idSeccion) {
					seccion.ocultar();
				}
			}
		}
	}

	/**
	 * Retorna el nombre del template asociado al perfil.
	 */
	public String getPrintingTemplate() {
		AplicacionPerfil aplPerfil = (AplicacionPerfil) this.entity;
		// Template principal
		return aplPerfil.getPrintingTemplate();
	}

	/**
	 * Retorna un mapa con las secciones visibles del form
	 */
	public Map<String, Boolean> getMapSeccionesVisibles() {
		Map<String, Boolean> mSeccion = new HashMap<>();

		for (Component co: this.components) {
			SeccionGUI seccion = (SeccionGUI) co;
			AplicacionPerfilSeccion aplSeccion = (AplicacionPerfilSeccion) seccion.getEntity();

			if (!seccion.isVisible()) {
				continue;
			}

			String key = "seccion:"+aplSeccion.getId();
			mSeccion.put(key, seccion.isVisible());
		}

		return mSeccion;
	}

	/**
	 * Retorna un mapa con los values del form formateados para impresion
	 */
	public Map<String, String> getValuesForReport() {
		HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();

		return Utils.fillValueForReport(values(), appState.getListEntidadBusqueda());
	}

	/**
	 * @param sKeys String con los campos relacionados separados por coma
	 * @return String con el resumen Epicrisis
	 */
	public String getEpicrisisFromKeys(String sKeys) {
		HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
		return Utils.fillValueForEpicrisis(values(), appState.getListEntidadBusqueda(), sKeys);
	}

	/**
	 * Inicializar secciones opcionales habilitadas al levantar IM
	 */
	public void actualizarSeccionesOpcionales() {
		if (this.components == null) {
			return;
		}
		for (Component section: this.components) {
			if (section.getComponents() == null) {
				continue;
			}

			for (Component field: section.getComponents()) {
				CampoGUI campo = (CampoGUI) field;
				if (campo.getTratamiento().equals(Tratamiento.SO)) {
					SectionsCheckListGUI scl = (SectionsCheckListGUI) campo;
					scl.updateDisplay();
				}
			}
		}
	}

	public void actualizarSoloLecturaCondicional() {
		if (this.components == null) {
			return;
		}

		for (Component section: this.components) {
			if (section.getComponents() != null) {
				for (Component field: section.getComponents()) {
					CampoGUI campo = (CampoGUI) field;

					if (campo.isSoloLectura() && campo.isCondicional()) {
						Component compValorCondicional = this.getComponentForCampoID(campo.getCampoCondicional());
						if (compValorCondicional != null) {
							compValorCondicional.condCampoValorIDs.add(campo.getIdCampo());
						}

						campo.evalCondicionalSoloLectura(campo.getCampoCondicional(), campo.getIdCampo());
					}
				}
			}
		}
	}

	// ----------------

	/**
	 * Retorna el valor del campo con ID pasado como parametro.
	 */
	public Value getValorForCampoID(int campoID) {
		List<Value> values = getValoresForCampoID(campoID);
		if (CollectionUtils.isEmpty(values)) {
			return null;
		} else {
			return values.get(0);
		}
	}

	public List<Value> getValoresForCampoID(int campoID) {
		Component campo = getComponentForCampoID(campoID);
		if (campo != null && campo.getEntity() instanceof AplPerfilSeccionCampo) {
			return campo.values();
		} else {
			return new ArrayList<>();
		}
	}

	public Component getComponentForCampoID(int campoID) {
		if (this.components == null) {
			return null;
		}

		for (Component seccion: this.components) {
			for (Component campo : seccion.getComponents()) {
				AbstractEntity entity = campo.getEntity();
				if (entity.getId() == campoID) {
					return campo;
				}
			}
		}
		return null;
	}

	// ----------------

	/**
	 * Retorna el valor del campo con ID pasado como parametro.
	 * La diferencia con el método getValorForCampoID es que recupera el campo por id
	 * y no por el id del perfil en el cual se encuentra el campo.
	 */
	public Value getValorByCampoID(int campoID) {
		List<Value> values = getValoresByCampoID(campoID);

		if (CollectionUtils.isEmpty(values)) {
			return null;
		} else {
			return values.get(0);
		}
	}

	/**
	 * La diferencia con el método getValoresForCampoID es que recupera los valores por el id del campo
	 * y no por el id del perfil en el cual se encuentra el campo.
	 */
	public List<Value> getValoresByCampoID(int campoID) {
		Component campo = getComponentByCampoID(campoID);

		if (campo == null) {
			return new ArrayList<>();
		} else {
			return campo.values();
		}
	}

	/**
	 * La diferencia con el método getComponentForCampoID es que recupera el componente por el id del campo
	 * y no por el id del perfil en el cual se encuentra el campo.
	 */
	public Component getComponentByCampoID(int campoID) {
		if (this.components == null) {
			return null;
		}

		for (Component seccion: this.components) {
			for (Component campo : seccion.getComponents()) {
				AbstractEntity entity = campo.getEntity();
				if (entity instanceof AplPerfilSeccionCampo) {
					AplPerfilSeccionCampo aplPerfilSeccionCampo = (AplPerfilSeccionCampo) entity;
					Campo currentCampo = aplPerfilSeccionCampo.getCampo();
					if (currentCampo.getId() == campoID) {
						return campo;
					}
				}
			}
		}
		return null;
	}



}

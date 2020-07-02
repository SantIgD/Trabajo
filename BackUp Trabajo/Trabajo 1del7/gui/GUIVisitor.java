package coop.tecso.hcd.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.Context;

import android.text.TextUtils;
import android.util.Log;
import coop.tecso.hcd.entities.EntidadBusqueda;
import coop.tecso.hcd.gui.components.AnclajeGUI;
import coop.tecso.hcd.gui.components.AttacherGUI;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.CheckListGUI;
import coop.tecso.hcd.gui.components.CheckListTxtGUI;
import coop.tecso.hcd.gui.components.acvscore.ACVABCDDScoreGUI;
import coop.tecso.hcd.gui.components.acvscore.ACVHelper;
import coop.tecso.hcd.gui.components.acvscore.ACVLAPSSScoreGUI;
import coop.tecso.hcd.gui.components.acvscore.ACVPersistenceComboGUI;
import coop.tecso.hcd.gui.components.acvscore.ACVSuspicionComboGUI;
import coop.tecso.hcd.gui.components.difresscore.DifRespHelper;
import coop.tecso.hcd.gui.components.ecg.ComboECGInterpretadoGUI;
import coop.tecso.hcd.gui.components.ComboGUI;
import coop.tecso.hcd.gui.components.Component;
import coop.tecso.hcd.gui.components.DataSearchGUI;
import coop.tecso.hcd.gui.components.DateGUI;
import coop.tecso.hcd.gui.components.difresscore.ComboDifResGUI;
import coop.tecso.hcd.gui.components.difresscore.DifResScoreGUI;
import coop.tecso.hcd.gui.components.DynamicListGUI;
import coop.tecso.hcd.gui.components.ElectroGUI;
import coop.tecso.hcd.gui.components.EmailGUI;
import coop.tecso.hcd.gui.components.LabelGUI;
import coop.tecso.hcd.gui.components.LabelTextGUI;
import coop.tecso.hcd.gui.components.LinkGUI;
import coop.tecso.hcd.gui.components.ListGUI;
import coop.tecso.hcd.gui.components.NavGUI;
import coop.tecso.hcd.gui.components.PDFListGUI;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.components.PhoneGUI;
import coop.tecso.hcd.gui.components.difresscore.ReDifResScoreGUI;
import coop.tecso.hcd.gui.components.ResumenGUI;
import coop.tecso.hcd.gui.components.difresscore.SatOxigNOScoreGUI;
import coop.tecso.hcd.gui.components.difresscore.SatOxigSIScoreGUI;
import coop.tecso.hcd.gui.components.ecg.ScoreECGGUI;
import coop.tecso.hcd.gui.components.SearchListsDirectGUI;
import coop.tecso.hcd.gui.components.SearchListsGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.gui.components.SectionsCheckListGUI;
import coop.tecso.hcd.gui.components.SectionsComboGUI;
import coop.tecso.hcd.gui.components.SignGUI;
import coop.tecso.hcd.gui.components.TextGUI;
import coop.tecso.hcd.gui.components.TextGUI.Teclado;
import coop.tecso.hcd.gui.components.TimeGUI;
import coop.tecso.hcd.gui.helpers.FilaTablaBusqueda;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.PerfilVisitor;
import coop.tecso.udaa.domain.perfiles.Seccion;

public final class GUIVisitor implements PerfilVisitor {

	private final String TAG = getClass().getSimpleName();

	private Context context;
	private Component component;

	private Map<String, List<FilaTablaBusqueda>> mapEntBus;
	private Map<String, List<Value>> mapInitialValues;
	private List<Value> values;
	private List<Component> seccionList = new ArrayList<>();
	private List<Component> opcionList = new ArrayList<>();
	private List<Component> valorList = new ArrayList<>();
	private List<Component> campoList = new ArrayList<>();

	private HashSet<Integer> covidShowableSectionIDs = new HashSet<>();

	private boolean enabled = true;
	private boolean haveInitialValue = false;

	public GUIVisitor(Context context) {
		this.context = context;
	}

	public Component buildComponents(AplicacionPerfil perfil,
			List<EntidadBusqueda> listEntidadBusqueda, List<Value> listValue,
			boolean enabled) {
		this.mapEntBus = Utils.getMapEntidadBusqueda(listEntidadBusqueda);
		this.mapInitialValues = Utils.fillInitialValuesMaps(listValue);
		this.haveInitialValue = true;
		this.values = listValue;
		this.enabled = enabled;
		for (String key: mapEntBus.keySet()) {
			Log.i(TAG, "Entidad de busqueda: " + key);
		}

		this.fillCovidOptionalSectionsIDs();

		this.component = new PerfilGUI(context, enabled);
		perfil.accept(this);
		return component;
	}

	@Override
	public void visit(AplicacionPerfil perfil) {
		Log.i(TAG, "visit: Perfil");
		this.component.setEntity(perfil);
		this.component.setComponents(this.seccionList);
		this.component.build();

		this.seccionList = new ArrayList<>();
	}

	@Override
	public void visit(AplicacionPerfilSeccion perfilSeccion) {
		// Log.i(TAG, "visit: Seccion");
		Seccion seccion = perfilSeccion.getSeccion();
		SeccionGUI component = new SeccionGUI(context, enabled);
		if (seccion == null) {
			return;
		}

		boolean opcional = this.isOptionalSection(perfilSeccion);

		component.setEntity(perfilSeccion);
		component.setEtiqueta(seccion.getDescripcion());
		component.setNoDesplegar(perfilSeccion.isNoDesplegar());
		component.setOpcional(opcional);
		component.setComponents(this.campoList);
		component.build();

		this.seccionList.add(component);
		this.campoList = new ArrayList<>();
	}

	@Override
	public void visit(AplPerfilSeccionCampo perfilSeccionCampo) {
		// Log.i(TAG, "visit: Campo");
		boolean enabledComponent = enabled && !perfilSeccionCampo.isSoloLectura();

		Campo campo = perfilSeccionCampo.getCampo();
		if (campo == null) {
			return;
		}

		Tratamiento tratamiento = Tratamiento.getByCod(campo.getTratamiento());
		if (tratamiento.equals(Tratamiento.DESCONOCIDO) && campo.getTratamientoDefault() != null) {
			tratamiento = Tratamiento.getByCod(campo.getTratamientoDefault());
		}
		
		CampoGUI component;
		int caracteres = perfilSeccionCampo.getCaracteres();

		switch (tratamiento) {
		case LABEL: //Texto
			component = new LabelTextGUI(context, enabled);
			break;
		case TA: // Alfanumerico
			component = new TextGUI(context, Teclado.ALFANUMERICO, false, enabledComponent, caracteres);
			break;
		case TAM: // Alfanumerico Multilinea
			component = new TextGUI(context, Teclado.ALFANUMERICO, true, enabledComponent, caracteres);
			break;
		case TNE: // Entero
			component = new TextGUI(context, Teclado.NUMERICO, false,enabledComponent, caracteres);
			break;
		case TND: // Decimal
			component = new TextGUI(context, Teclado.DECIMAL, false, enabledComponent, caracteres);
			break;
		case TN2: // Numerico Extendido
			component = new TextGUI(context, Teclado.NUMERICO_EXTENDIDO, false,	enabledComponent, caracteres);
			break;
		case TF: // Fecha
			component = new DateGUI(context, enabledComponent);
			break;
		case TT: // Hora
			component = new TimeGUI(context, enabledComponent);
			break;
		case OP: // Opciones simple seleccion (Combo)
            component = instantiateComboGUI(perfilSeccionCampo, enabledComponent);
			break;
		case LO: // Lista de Opciones
		case LOIMPR:
			component = new CheckListGUI(context, enabledComponent);
			break;
		case LOR:
			component = new CheckListTxtGUI(context, enabledComponent);
			break;
		case SCORE: // Lista opciones pegadas al formulario (No abre popup)
			component = new ScoreECGGUI(context,enabledComponent);
			break;
		case SCRDIFRES:
			component = new DifResScoreGUI(context, enabledComponent);
			break;
		case SCRSATOXSI:
			component = new SatOxigSIScoreGUI(context, enabledComponent);
			break;
		case SCRSATOXNO:
			component = new SatOxigNOScoreGUI(context, enabledComponent);
			break;
        case SCRDIFRESR:
            component = new ReDifResScoreGUI(context, enabledComponent);
            break;
        case SCRACV:
            component = instantiateACVScoreGUI(perfilSeccionCampo, enabledComponent);
            break;
        case LC: // Lista de Campos Estatica
			component = new ListGUI(context, enabledComponent);
			break;
		case LD: // Lista de Campos Dinamica
			component = new DynamicListGUI(context, enabledComponent);
			break;
		case LB: // Lista de Busqueda (Lista de entidades tomadas de tabla
					// busqueda)
			component = new SearchListsGUI(context, enabledComponent);
			break;
		case LNK: // Link
			component = new LinkGUI(context, enabledComponent);
			break;
		case NAV: // Google Navigation
			component = new NavGUI(context, enabledComponent);
			break;
		case SO: // Secciones Opcionales (SectionsCheckList)
			component = new SectionsCheckListGUI(context, enabledComponent);
			break;
		case SOC:  // Secciones Opcionales (SectionsCombo)
			component = new SectionsComboGUI(context, enabledComponent);
			((SectionsComboGUI)component).setOpcionesInvalidas(getOpcionesInvalidas(perfilSeccionCampo.getOpcionesInvalidas()));
			break;
		case PIC:  // Adjunto Imagen
			AttacherGUI attacherGUI = new AttacherGUI(context, enabledComponent, caracteres);
			attacherGUI.setEscala((float)perfilSeccionCampo.getResolucion()/100);
			component = attacherGUI;
			break;
		case FIR:  // Firma Digital
			SignGUI signGUI = new SignGUI(context, enabledComponent);
			signGUI.setEscala((float)perfilSeccionCampo.getResolucion()/100);
			component = signGUI;
			break;
		case LBD: // Lista de Busqueda  Directa
		case LBDIMPR:
			component = new SearchListsDirectGUI(context, enabledComponent);
			break;
		case BU:  // Busqueda en Tabla (EntidadBusqueda)
			component = new DataSearchGUI(context, enabledComponent);
			break;
		case EMAIL: //Email
			component = new EmailGUI(context, enabledComponent);
			break;
		case ECG:
			component = new ElectroGUI(context, enabledComponent);
			break;
		case RES:
			component = new ResumenGUI(context, enabledComponent);
			break;
		case TEL:
			component = new PhoneGUI(context, enabledComponent);
			break;
		case ANC:
			component = new AnclajeGUI(context, enabledComponent);
			break;
		case PDF:
			component = new PDFListGUI(context, enabledComponent);
			break;
		default:
			// Tratamiento no implementado
			component = new CampoGUI(context);
			break;
		}

		component.setIdCampo(perfilSeccionCampo.getId());
		component.setPerfilGUI((PerfilGUI) this.component);
		component.setEntity(perfilSeccionCampo);
		component.setEtiqueta(campo.getEtiqueta());
		component.setObligatorio(perfilSeccionCampo.isObligatorio());
		component.setMinCaracteres(perfilSeccionCampo.getMinCaracteres());
		component.setMinNumeros(perfilSeccionCampo.getMinNumeros());
		component.setMaxNumeros(perfilSeccionCampo.getMaxNumeros());
		component.setCondicional(perfilSeccionCampo.isCondicional());
		component.setCondicion(perfilSeccionCampo.getCondicion());		
		component.setCampoCondicional(perfilSeccionCampo.getCampoCondicional());
		component.setValorCondicional(perfilSeccionCampo.getValorCondicional());
		component.setSoloLectura(perfilSeccionCampo.isSoloLectura());
		component.setValorDefault(campo.getValorDefault());
		component.setTratamiento(tratamiento);

		component.setTablaBusqueda(mapEntBus.get(campo.getEntidadBusqueda()));
		component.setMapEntBus(this.mapEntBus);
		component.setEntidadBusqueda(campo.getEntidadBusqueda());

		component.setComponents(this.valorList);
		component.setOrden(perfilSeccionCampo.getOrden());

		// Reemplaza la entidad de busqueda condicional si es necesario
		for (Value valor: this.values) {
			if (component.shouldApplyEntidadBusquedaCondicional(valor)) {
			    String eb = component.getEntidadBusquedaCondVal();
                component.setEntidadBusqueda(eb);
			}
		}

		// Carga de valor inicial (precargado)
		if (haveInitialValue) {
			String key = perfilSeccionCampo.getId() + "|0|0";
			component.setInitialValues(mapInitialValues.get(key));
		}

		component.build();
		this.campoList.add(component);
		this.valorList = new ArrayList<>();
	}

	@Override
	public void visit(AplPerfilSeccionCampoValor perfilSeccionCampoValor) {
		if (perfilSeccionCampoValor == null) {
			return;
		}

		// Log.i(TAG, "visit: Valor");
		boolean enabledComponent = enabled
				&& !perfilSeccionCampoValor.getAplPerfilSeccionCampo().isSoloLectura();
		CampoValor campoValor = perfilSeccionCampoValor.getCampoValor();

		Tratamiento tratamiento = Tratamiento.getByCod(campoValor.getTratamiento());
		if (tratamiento.equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null) {
			tratamiento = Tratamiento.getByCod(campoValor.getTratamientoDefault());
		}

		CampoGUI component;
		int caracteres = perfilSeccionCampoValor.getCaracteres();
		switch (tratamiento) {
		case NA: // Etiqueta (Se utiliza para Opciones seleccionadas)
			component = new LabelGUI(context, enabled);
			break;
		case TA: // Alfanumerico
			component = new TextGUI(context, Teclado.ALFANUMERICO, false,
					enabledComponent, caracteres);
			break;
		case TAM: // Alfanumerico Multilinea
			component = new TextGUI(context, Teclado.ALFANUMERICO, true,
					enabledComponent, caracteres);
			break;
		case TNE: // Entero
			component = new TextGUI(context, Teclado.NUMERICO, false,
					enabledComponent, caracteres);
			break;
		case TND: // Decimal
			component = new TextGUI(context, Teclado.DECIMAL, false,
					enabledComponent, caracteres);
			break;
		case TN2: // Numerico Extendido
			component = new TextGUI(context, Teclado.NUMERICO_EXTENDIDO, false,
					enabledComponent, caracteres);
			break;
		case TF: // Fecha
			component = new DateGUI(context, enabledComponent);
			break;
		case TT: // Hora
			component = new TimeGUI(context, enabledComponent);
			break;
		case OP: // Opciones simple seleccion (Combo)
			component = new ComboGUI(context, enabledComponent);
			((ComboGUI) component).setOpcionesInvalidas(getOpcionesInvalidas(perfilSeccionCampoValor.getOpcionesInvalidas()));
			break;
		case BU: // Busqueda en Tabla (EntidadBusqueda)
			component = new DataSearchGUI(context, enabledComponent);
			break;
		case LNK: // Link
			component = new LinkGUI(context, enabledComponent);
			break;
		case PIC:  // Adjunto Imagen
			AttacherGUI attacherGUI = new AttacherGUI(context, enabledComponent, caracteres);
			attacherGUI.setEscala((float)perfilSeccionCampoValor.getResolucion()/100);
			component = attacherGUI;
			break;
		case FIR:  // Firma Digital
			SignGUI signGUI = new SignGUI(context, enabledComponent);
			signGUI.setEscala((float)perfilSeccionCampoValor.getResolucion()/100);
			component = signGUI;
			break;
		case LBD: // Lista de Busqueda Directa
		case LBDIMPR:
			component = new SearchListsDirectGUI(context, enabledComponent);
			break;
		case ECG: // Electrocardiograma
			component = new ElectroGUI(context,enabledComponent);
			break;

		default:
			// Tratamiento no implementado
			component = new CampoGUI(context);
			break;
		}

		component.setPerfilGUI((PerfilGUI) this.component);
		component.setEntity(perfilSeccionCampoValor);
		component.setEtiqueta(campoValor.getEtiqueta());
		component.setObligatorio(perfilSeccionCampoValor.isObligatorio());
		component.setMinCaracteres(perfilSeccionCampoValor.getMinCaracteres());
		component.setMinNumeros(perfilSeccionCampoValor.getMinNumeros());
		component.setMaxNumeros(perfilSeccionCampoValor.getMaxNumeros());
		component.setValorDefault(campoValor.getValorDefault());

		component.setTratamiento(Tratamiento.getByCod(campoValor.getTratamiento()));
		if(component.getTratamiento().equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null){
			component.setTratamiento(Tratamiento.getByCod(campoValor.getTratamientoDefault()));
		}

		component.setTablaBusqueda(mapEntBus.get(campoValor.getEntidadBusqueda()));
		component.setComponents(this.opcionList);
		// Carga de valor inicial (precargado)
		if (haveInitialValue) {
			String key = perfilSeccionCampoValor.getAplPerfilSeccionCampo()
					.getId() + "|" + perfilSeccionCampoValor.getId() + "|0";
			component.setInitialValues(mapInitialValues.get(key));
		}

		component.build();
		this.valorList.add(component);
		this.opcionList = new ArrayList<>();
	}

	@Override
	public void visit(AplPerfilSeccionCampoValorOpcion perfilSeccionCampoValorOpcion) {
		// Log.i(TAG, "visit: Opcion");
		boolean enabledComponent = enabled
				&& !perfilSeccionCampoValorOpcion
						.getAplPerfilSeccionCampoValor()
						.getAplPerfilSeccionCampo().isSoloLectura();
		CampoValorOpcion campoValorOpcion = perfilSeccionCampoValorOpcion
				.getCampoValorOpcion();
		
		Tratamiento tratamiento = Tratamiento.getByCod(campoValorOpcion.getTratamiento());
		if(tratamiento.equals(Tratamiento.DESCONOCIDO) && campoValorOpcion.getTratamientoDefault() != null){
			tratamiento = Tratamiento.getByCod(campoValorOpcion.getTratamientoDefault());
		}

		CampoGUI component;
		int caracteres = perfilSeccionCampoValorOpcion.getCaracteres();
		switch (tratamiento) {
		case NA: // Etiqueta (Se utiliza para Opciones seleccionadas)
			component = new LabelGUI(context, enabled);
			break;
		case TA: // Alfanumerico
			component = new TextGUI(context, Teclado.ALFANUMERICO, false,
					enabledComponent, caracteres);
			break;
		case TAM: // Alfanumerico Multilinea
			component = new TextGUI(context, Teclado.ALFANUMERICO, true,
					enabledComponent, caracteres);
			break;
		case TNE: // Entero
			component = new TextGUI(context, Teclado.NUMERICO, false,
					enabledComponent, caracteres);
			break;
		case TND: // Decimal
			component = new TextGUI(context, Teclado.DECIMAL, false,
					enabledComponent, caracteres);
			break;
		case TN2: // Numerico Extendido
			component = new TextGUI(context, Teclado.NUMERICO_EXTENDIDO, false,
					enabledComponent, caracteres);
			break;
		case TF: // Fecha
			component = new DateGUI(context, enabledComponent);
			break;
		case TT: // Hora
			component = new TimeGUI(context, enabledComponent);
			break;
		case BU: // Busqueda en Tabla (EntidadBusqueda)
			component = new DataSearchGUI(context, enabledComponent);
			break;
		case LNK: // Link
			component = new LinkGUI(context, enabledComponent);
			break;
		default:
			// Tratamiento no implementado
			component = new CampoGUI(context);
			break;
		}

		component.setPerfilGUI((PerfilGUI) this.component);
		component.setEntity(perfilSeccionCampoValorOpcion);
		component.setEtiqueta(campoValorOpcion.getEtiqueta());
		component.setObligatorio(campoValorOpcion.isObligatorio());
		component.setMinCaracteres(perfilSeccionCampoValorOpcion.getMinCaracteres());
		component.setMinNumeros(perfilSeccionCampoValorOpcion.getMinNumeros());
		component.setMaxNumeros(perfilSeccionCampoValorOpcion.getMaxNumeros());
		component.setValorDefault(campoValorOpcion.getValorDefault());
		
		component.setTratamiento(Tratamiento.getByCod(campoValorOpcion.getTratamiento()));
		if(component.getTratamiento().equals(Tratamiento.DESCONOCIDO) && campoValorOpcion.getTratamientoDefault() != null){
			component.setTratamiento(Tratamiento.getByCod(campoValorOpcion.getTratamientoDefault()));
		}

		component.setTablaBusqueda(mapEntBus.get(campoValorOpcion
				.getEntidadBusqueda()));
		component.setComponents(new ArrayList<>());
		// Carga de valor inicial (precargado)
		if (haveInitialValue) {
			String key = perfilSeccionCampoValorOpcion
					.getAplPerfilSeccionCampoValor().getAplPerfilSeccionCampo()
					.getId()
					+ "|"
					+ perfilSeccionCampoValorOpcion
							.getAplPerfilSeccionCampoValor().getId()
					+ "|"
					+ perfilSeccionCampoValorOpcion.getId();
			component.setInitialValues(mapInitialValues.get(key));
		}

		component.build();
		this.opcionList.add(component);
	}

	// MARK: - Internal

	private List<Integer> getOpcionesInvalidas(String opcionesInvalidas){
		if (opcionesInvalidas == null) {
			return null;
		}

		List<Integer> invalidIds = new ArrayList<>();
		String ids[] = opcionesInvalidas.split(",");
		for (String id: ids) {
			invalidIds.add(Integer.parseInt(id));
		}

		return invalidIds;
	}

	private boolean isComboSeccionECGInterpretado(int id){
		Integer idComboSeccionECG = ParamHelper.getInteger(ParamHelper.CAMPO_PATRON_ECG_INTERPRETADO_ID,null);

		return id == idComboSeccionECG;
	}

	private CampoGUI instantiateComboGUI(AplPerfilSeccionCampo perfilSeccionCampo, boolean enabledComponent) {
		CampoGUI component;

		List<Integer> invalidOptions = getOpcionesInvalidas(perfilSeccionCampo.getOpcionesInvalidas());

		int antecedenteInsufRespCampoID = DifRespHelper.antecedenteInsufRespCampoID();
		int derivaPacienteCampoID = DifRespHelper.derivaPacienteCampoID();

		int idCampo = perfilSeccionCampo.getCampo().getId();
		if (isComboSeccionECGInterpretado(idCampo)){
			component = new ComboECGInterpretadoGUI(context, enabledComponent);
			((ComboECGInterpretadoGUI) component).setOpcionesInvalidas(invalidOptions);
		}
		else if (idCampo == antecedenteInsufRespCampoID || idCampo == derivaPacienteCampoID) {
			component = new ComboDifResGUI(context, enabledComponent);
			((ComboGUI) component).setOpcionesInvalidas(invalidOptions);
		}
		else if (idCampo == ACVHelper.suspicionComboID()) {
			component = new ACVSuspicionComboGUI(context, enabledComponent);
			((ComboGUI) component).setOpcionesInvalidas(invalidOptions);
		}
		else if (idCampo == ACVHelper.persistenceComboID()) {
			component = new ACVPersistenceComboGUI(context, enabledComponent);
			((ComboGUI) component).setOpcionesInvalidas(invalidOptions);
		}
		else {
			component = new ComboGUI(context, enabledComponent);
			((ComboGUI) component).setOpcionesInvalidas(invalidOptions);
		}

		return component;
	}

	private CampoGUI instantiateACVScoreGUI(AplPerfilSeccionCampo perfilSeccionCampo, boolean enabledComponent) {
		int campoID = perfilSeccionCampo.getCampo().getId();
		if (campoID == ACVHelper.abcddScoreID()) {
			return new ACVABCDDScoreGUI(context, enabledComponent);
		}
		else if (campoID == ACVHelper.lappsScoreID()) {
			return new ACVLAPSSScoreGUI(context, enabledComponent);
		}
		else {
			return new CampoGUI(context);
		}
	}

	// MARK: - Covid Sections

	private void fillCovidOptionalSectionsIDs() {
		int campoID = ParamHelper.getInteger(ParamHelper.COVID_OPTIONAL_SECTION_FIELD_DATA_ID, -1);
		String key = campoID + "|0|0";

		List<Value> values = mapInitialValues.get(key);
		if (CollectionUtils.isEmpty(values)) {
			return;
		}

		String valor = values.get(0).getValor();
		if (TextUtils.isEmpty(valor)) {
			return;
		}

		String[] components = valor.split(",");

		for (String component: components) {
			try {
				int id = Integer.parseInt(component);
				this.covidShowableSectionIDs.add(id);
			} catch (Exception ignore) {}
		}
	}

	private boolean isOptionalSection(AplicacionPerfilSeccion perfilSeccion) {
		if (covidShowableSectionIDs.contains(perfilSeccion.getId())) {
			return false;
		} else {
			return perfilSeccion.isOpcional();
		}
	}

}
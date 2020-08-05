package coop.tecso.hcd.gui.helpers;

public enum Tratamiento {
	
	TA("TA"), 	// Teclado Alfanumerico
	TAM("TAM"), // Teclado Alfanumerico Multilinea
	TNE("TNE"), // Numerico Entero
	TND("TND"), // Numerico Decimal
	TN2("TN2"), // Numerico Extendido (Teclado de telefono. Permite cargar punto decimal y barra)
	TF("TF"),  	// Fecha
	TT("TT"), 	// Tiempo
	LO("LO"),  	// Lista de opciones (CheckList)
	SCORE("SCORE"), // Lista de checks obtenidos de la tabla hcd_score
	SCRDIFRES("SCRDIFRES"), // Score para dificultades respiratorias
	SCRSATOXSI("SCRSATOXSI"),
	SCRSATOXNO("SCRSATOXNO"),
	SCRDIFRESR("SCRDIFRESR"),
	SCRACV("SCRACV"), // Score ACV
	LOIMPR("LOIMPR"),  // Lista de opciones (CheckList) Impresión
	LB("LB"),  	// Lista de Busqueda (Lista de entidades tomadas de tabla busqueda)
	LC("LC"),  	// Lista de Campos Estatica
	LD("LD"),  	// Lista de Campos Dinamica 
	OP("OP"),  	// Opciones simple seleccion (Combo)
	BU("BU"),  	// Busqueda en Tabla (EntidadBusqueda)
	NA("NA"),  	// Opcion Simple (Checked)
	LNK("LNK"), // Link
	NAV("NAV"), // Google Navigation
	SO("SO"),  	// Secciones Opcionales (SectionsCheckList)
	PIC("PIC"),  	// Secciones Opcionales (SectionsCheckList)
	FIR("FIR"),		// Firma
	SOC("SOC"),  	// Secciones Opcionales Combo (SectionsCombo)
	PAD("PAD"),		// Consulta en padron de inhibidos
	CBU("CBU"),		// CBU
	OP2("OP2"),		// Opcion simple extendida (Combo) - Llena el combo con valores de una tabla particular
	DOM("DOM"),		// Domicilio
	LBD("LBD"),  	// Lista de Busqueda Directa
	LBDIMPR("LBDIMPR"),  	// Lista de Busqueda Directa Impresión
	EMAIL("EMAIL"),	// E-Mail
	ECG("ECG"), // Electrocardiograma
	RES("RES"), // Resumen
	TEL("TEL"),
	LABEL("LABEL"), // Texto
	ANC("ANC"),
	PDF("PDF"),
	LOR("LOR"),
	DESCONOCIDO("--");  // Para casos en que se informe un tratamiento desconocido por la aplicacion
	
	private String cod;
	
	public String getCod() {
		return cod;
	}

	public void setCod(String cod) {
		this.cod = cod;
	}

	private Tratamiento(String cod) {
		this.cod = cod;
	}
	
	public static Tratamiento getByCod(String cod) {
		// Clear blanks
		cod = cod.trim();

		Tratamiento[] tratamientos = Tratamiento.values();
		for (int i = 0; i < tratamientos.length; i++) {
			if (tratamientos[i].getCod().equals(cod)) {
				return tratamientos[i];
			}
		}
		return DESCONOCIDO;
	}	
}
package coop.tecso.hcd.entities;

import java.util.List;

public final class Condicion {

	private List<Condicion> subCondiciones;
	
	private String operador;
	
	private String paraEvaluar;

	public List<Condicion>  getSubCondiciones() {
		return subCondiciones;
	}

	public void setSubCondiciones(List<Condicion>  subCond) {
		this.subCondiciones = subCond;
	}

	public String getOperador() {
		return operador;
	}

	public void setOperador(String operador) {
		this.operador = operador;
	}

	public String getParaEvaluar() {
		return paraEvaluar;
	}

	public void setParaEvaluar(String paraEvaluar) {
		this.paraEvaluar = paraEvaluar;
	}
}
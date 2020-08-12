package coop.tecso.hcd.gui.components;

import android.content.Context;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.AccionHC;
import coop.tecso.hcd.entities.Condicion;
import coop.tecso.hcd.entities.CondicionAlertaHC;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.EvalEvent;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.AlertDispatcherHelper;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;

/**
 * AlertDispatcherImpl - Implementacion AlertDispatcher de HCDigital
 * con la logica para el disparo de alertas al area de notificaciones.
 * 
 * @author tecso.coop
 *
 */
@SuppressWarnings({"StringBufferReplaceableByString", "IfCanBeSwitch", "SimpleDateFormat"})
public final class AlertDispatcherImpl implements AlertDispatcher {
	
	/**
	 * Metodo llamado por cada campo del formulario para evaluar 
	 * si debe aplicar alguna accion.
	 */
	@Override
	public void eval(CampoGUI campo, EvalEvent eventEvent) {
		PerfilGUI perfilGUI = campo.getPerfilGUI();
		helper.refreshAllValues(perfilGUI);
		
		int campoID = helper.getCampoID(campo);
		
		Map<CampoGUI, ArrayList<Integer>> mapOptions = new HashMap<CampoGUI, ArrayList<Integer>>();
		List<AccionHC> listAcciones = helper.obtenerAcciones(perfilGUI.getEntity().getId());		
		
		for (AccionHC accion : listAcciones) {
			try {
				if (accion.getAplPerfilSeccionCampoID() != campoID)	{
				    continue;
                }
					
                for (String campoSplitted: accion.getOpciones().split(",")) {

                    for (Value val: campo.values()) {
                        if(val.getCodigoEntidadBusqueda().equals(campoSplitted)) {

                            for (String accionSplitted : accion.getAcciones().toLowerCase().split("and")) {
                                CampoGUI campoGui = (CampoGUI)perfilGUI.getComponentForCampoID(Integer.parseInt(accionSplitted.split("\\.")[0].trim()));

                                if (campoGui == null) {
                                    continue;
                                }
                                ArrayList<Integer> intList = new ArrayList<Integer>();
                                for (String opcion : getBeetween(accionSplitted.split("\\.")[1], "{", "}").split(",")) {
                                    intList.add(Integer.parseInt(opcion));
                                }

                                if (!mapOptions.containsKey(campoGui)) {
                                    mapOptions.put(campoGui, intList);
                                } else {
                                    ArrayList<Integer> listLoaded = mapOptions.get(campoGui);
                                    mapOptions.remove(campoGui);

                                    for	(int opcion : intList) {
                                        if (!listLoaded.contains(opcion)) {
                                            listLoaded.add(opcion);
                                        }
                                    }

                                    mapOptions.put(campoGui, listLoaded);
                                }
                            }

                            break;
                        }
                    }
                }

			}
			catch (Exception ignore) {}
		}
		
		try {
			for	(Map.Entry<CampoGUI, ArrayList<Integer>> accion : mapOptions.entrySet()) {
				if (accion.getKey() instanceof CheckListGUI) {
					((CheckListGUI)accion.getKey()).loadOptions(accion.getValue());
				}
			}
		}
		catch (Exception ignore) {}
	}

	/**
	 * Metodo llamado para la evaluacion de alertas de todo el formulario. Este metodo se puede utilizar al inicio para los valores precargados.
	 */
	@Override
	public void evalAll(PerfilGUI perfilGUI) {
		helper.refreshAllValues(perfilGUI);

		this.ejecutarAlerta(perfilGUI, "A1");
		this.ejecutarAlerta(perfilGUI, "A2");
		this.ejecutarAlerta(perfilGUI, "A3");
	}

	/**
	 * 02-12-2013 - R. Iván G. Vesco
	 * Metodo llamado para la evaluacion de alertas de todo el formulario. 
	 * Este metodo se puede utilizar al intentar cerrar el IMD.
	 */
	@Override
	public boolean evalAllCierre(PerfilGUI perfilGUI) {
		boolean result = false;
		try {
			helper.refreshAllValues(perfilGUI);

			List<CondicionAlertaHC> listCondicionesAlertas = helper.obtenerCondicionesAlertas(perfilGUI.getEntity().getId());		

			for (CondicionAlertaHC condicionAlerta : listCondicionesAlertas) {					
				result = this.evalCondicion(perfilGUI, condicionAlerta);
				
				if (result){
					break;
				}
			}
		}
		catch (Exception e) {
			String message = "Ha ocurrido un error al evaluar las condiciones de alertas - " + e.getMessage();			
			UDAACoreServiceImpl udaaService = new UDAACoreServiceImpl(appState);
			udaaService.generateACRA_LOG(message, "CONDICION ALERTA");
		}
		
		if (!result) {
			helper.clearNofiticationArea();
		}
		
		return result;
	}
	
	private void ejecutarAlerta(PerfilGUI perfilGUI, String codAlerta) {
		try {
			List<CondicionAlertaHC> listCondicionesAlertas = helper.obtenerCondicionesAlertas(perfilGUI.getEntity().getId(), codAlerta);		
	
			for (CondicionAlertaHC condicionAlerta : listCondicionesAlertas) {		
				this.evalCondicion(perfilGUI, condicionAlerta);
			}
		} catch (Exception e) {
			String message = "Ha ocurrido un error al evaluar las condiciones de la alerta: " + codAlerta + " - " + e.getMessage();			
			UDAACoreServiceImpl udaaService = new UDAACoreServiceImpl(appState);
			udaaService.generateACRA_LOG(message, "CONDICION ALERTA");
		}
	}
	
	private boolean evalCondicion(PerfilGUI perfilGUI, CondicionAlertaHC condicionAlerta){
		try {
			List<Condicion> condicionesCompleta = getCondicion(condicionAlerta.getCondicion());
			for (Condicion condCompleta : condicionesCompleta) {
				if (evaluarCondicion(perfilGUI, condCompleta)) {
					if (condicionAlerta.getCampoFoco() != 0) {
						CampoGUI campoGui = (CampoGUI)perfilGUI.getComponentForCampoID(condicionAlerta.getCampoFoco());
						
						if (campoGui != null) {
							String condicionAlertaCode = condicionAlerta.getAlertaHcd().getCodigo();
							boolean IsNewAlert = helper.addMedicalNotification(condicionAlertaCode);
							campoGui.setFocus();
							
							if (IsNewAlert) {
								return true;
							}

							break;
						} else {
							throw new Exception("Campo Foco id: " + condicionAlerta.getCampoFoco() + " inexistente");
						}
					} else {
						helper.addMedicalNotification(condicionAlerta.getAlertaHcd().getCodigo());
					}

					String camposMarcados = condicionAlerta.getCamposMarcados();
					if (!TextUtils.isEmpty(camposMarcados)) {
						for (String campoID : camposMarcados.split(",")) {
							int campoIDInt = Integer.parseInt(campoID);
							
							if (campoIDInt != 0) {
								CampoGUI campoMarcar = (CampoGUI)perfilGUI.getComponentForCampoID(campoIDInt);
								
								if (campoMarcar != null) {
									if (!campoMarcar.isObligatorio()) {
										campoMarcar.setObligatorio(true);
										campoMarcar.validate();
										campoMarcar.setObligatorio(false);
									}
									else {
										campoMarcar.validate();
									}
								}
							}
						}
					}
					
					if (condicionAlerta.getBloqueante()) {
						return true;
					}
				}
			}
		}
		catch (Exception e) {
			String message = "Condicion de alerta (" + condicionAlerta.getNombre() + ") configurada incorrectamente - " + e.getMessage();
			
			UDAACoreServiceImpl udaaService = new UDAACoreServiceImpl(appState);
			udaaService.generateACRA_LOG(message, "CONDICION ALERTA");
		}
		return false;
	}
	
	private List<Condicion> getCondicion(String condicionAlerta) {
		List<Condicion> condiciones = new ArrayList<>();
		
        for (String condicion : condicionAlerta.split("#")) {
            Condicion cond = this.generarSubCondiciones(condicion);
            condiciones.add(cond);
        }
		return condiciones;
	}

    private Condicion generarSubCondiciones (String condicionCompleta) {
        Condicion condicion = new Condicion();

        String[] condicionesAND = condicionCompleta.trim().split("AND");

        if (condicionesAND.length == 1) {
        	String[] condicionesOR = condicionesAND[0].trim().split("OR");
            if (condicionesOR.length == 1) {
                condicion.setParaEvaluar(condicionesAND[0]);
                return condicion;
            }
            else {
                condicion.setOperador("OR");
                condicion.setSubCondiciones(new ArrayList<>());
                for (String subCondOR : condicionesOR) {
                    Condicion subCondicionOR = new Condicion();
                    subCondicionOR.setParaEvaluar(subCondOR);
                    condicion.getSubCondiciones().add(subCondicionOR);
                }
            }
        }
        else {
            condicion.setOperador("AND");
            condicion.setSubCondiciones(new ArrayList<>());
            for(String subCond : condicionesAND) {                
                Condicion subCondAND = new Condicion();
                String[] subCondicionesOR = subCond.split("OR");

                if(subCondicionesOR.length == 1) {
                    subCondAND.setParaEvaluar(subCond);
                }
                else {
                	subCondAND.setOperador("OR");
                	subCondAND.setSubCondiciones(new ArrayList<>());
                    for (String subCondOR : subCondicionesOR) {
                        Condicion subCondicionOR = new Condicion();
                        subCondicionOR.setParaEvaluar(subCondOR);
                        subCondAND.getSubCondiciones().add(subCondicionOR);
                    }
                }
                condicion.getSubCondiciones().add(subCondAND);
            }

        }
        return condicion;
    }
    
	private boolean evaluarCondicion(PerfilGUI perfilGUI, Condicion condActual) throws Exception {
		if (condActual.getParaEvaluar() != null) {
			//Campos
			String campoString = getBeetween(condActual.getParaEvaluar(), "{", "}");
			int campo = Integer.parseInt(campoString.split(Pattern.quote("."))[0]);
			int campoValor = 0;
			int campoValorOpcion = 0;		
			if (campoString.split(Pattern.quote(".")).length >= 2) {
				campoValor = Integer.parseInt(campoString.split(Pattern.quote("."))[1]);
			}
			if (campoString.split(Pattern.quote(".")).length == 3) {
				campoValorOpcion = Integer.parseInt(campoString.split(Pattern.quote("."))[2]);
			}
	 
			//Valor
			String valor = getBeetween(condActual.getParaEvaluar(), "[", "]");
			
			//Operador
			String operador = deleteBeetween(condActual.getParaEvaluar(), "{", "}");
			operador = deleteBeetween(operador, "[", "]");
			operador = operador.trim();
			
			List<Value> valoresGui = perfilGUI.getValoresForCampoID(campo);
			
			if (valoresGui != null && valoresGui.size() > 0) {
				for(Value valorGui : valoresGui) {
					if(valorGui.getCampo().getId() == campo) {
						String userValor = null;
						if (valorGui.getValor() != null) {
							userValor = valorGui.getValor();
						}
						else if (valorGui.getCodigoEntidadBusqueda() != null) {
							userValor = valorGui.getCodigoEntidadBusqueda();
						}

						boolean resultValidation = false;
						
						if (campoValor == 0) {
							resultValidation = evalWithTratamiento(Tratamiento.getByCod(valorGui.getCampo().getCampo().getTratamiento()), operador, userValor, valorGui.getCampo().getId(), valor, campo);
						}
						else if (valorGui.getCampoValor().getId() == campoValor) {
							if (campoValorOpcion == 0) {
								resultValidation = evalWithTratamiento(Tratamiento.getByCod(valorGui.getCampoValor().getCampoValor().getTratamiento()), operador, userValor, valorGui.getCampoValor().getId(), valor, campoValor);
							}
							else if (valorGui.getCampoValorOpcion().getId() == campoValorOpcion) {
								resultValidation = evalWithTratamiento(Tratamiento.getByCod(valorGui.getCampoValorOpcion().getCampoValorOpcion().getTratamiento()), operador, userValor, valorGui.getCampoValorOpcion().getId(), valor, campoValorOpcion);
							}
						}
						
						if (resultValidation) {
							return true;
						}
					}
				}
			} else if (TextUtils.isEmpty(valor) || valor.equals("0") || valor.toLowerCase().equals("false")) {
				return true;
			}
			
			return false;
		} else {
			List<Boolean> respSubcond = new ArrayList<>();
			for(Condicion subCond : condActual.getSubCondiciones()) {
				respSubcond.add(evaluarCondicion(perfilGUI, subCond));
			}
			
			if (condActual.getOperador().equals("OR")) {
				for (Boolean resp : respSubcond) {
					if (resp) {
					    return true;
                    }
				}
				return false;
			}
			else {				
				for (Boolean resp : respSubcond) {
					if (!resp) {
					    return false;
                    }
				}
				return true;
			}
		}
	}
	
	private boolean evalWithTratamiento(Tratamiento tratamiento, String operador, String valorOriginal, int idValorOriginal, String valorEvaluar, int idValorEvaluar) throws Exception {
		boolean result = false;
		
		switch(tratamiento) {
		case TA:
		case TAM:
		case LNK:
		case NAV:
		case PAD:
		case CBU:
		case DOM:
		case EMAIL:
		case BU:
		case LB:
		case LBD:
		case LBDIMPR:
		case RES:
            if (operador.equals("=")) {
				if (valorOriginal.equals(valorEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals("<>")) {
				if (!valorOriginal.equals(valorEvaluar)) {
					result = true;
				}
			}
			else {
				throw new Exception("Tratamiento: " + tratamiento.toString() + " Operador: " + operador);
			}
			break;
		case TNE:
		case TND:
		case TN2:
			if (TextUtils.isEmpty(valorOriginal)) {
				valorOriginal = "0";
			}
			
			if (operador.equals("=")) {
				if (Integer.parseInt(valorOriginal) == Integer.parseInt(valorEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals("<>")) {
				if (Integer.parseInt(valorOriginal) != Integer.parseInt(valorEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals("<")) {
				if (Integer.parseInt(valorOriginal) < Integer.parseInt(valorEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals(">")) {
				if (Integer.parseInt(valorOriginal) > Integer.parseInt(valorEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals("<=")) {
				if (Integer.parseInt(valorOriginal) <= Integer.parseInt(valorEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals(">=")) {
				if (Integer.parseInt(valorOriginal) >= Integer.parseInt(valorEvaluar)) {
					result = true;
				}
			}
			else {
				throw new Exception("Tratamiento: " + tratamiento.toString() + " Operador: " + operador);
			}
			break;
		case TF:
		case TT:
			if (TextUtils.isEmpty(valorOriginal)) {
                break;
            }
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
			Date dateOriginal = simpleDateFormat.parse(valorOriginal);
			Date dateEvaluar;
			try {
				dateEvaluar = simpleDateFormat.parse(valorEvaluar);
			}
			catch(Exception e) {
				//Si el valor a evaluar no es una fecha, entonces se parsea por años y meses, separados por .
				int Years;
				int Months = 0;	
				
				String[] parts = valorEvaluar.split(Pattern.quote("."));

				if (parts.length > 1) {
					Years = Integer.parseInt(parts[0]);
					Months = Integer.parseInt(parts[1]);
				}
				else {
					Years = Integer.parseInt(valorEvaluar);
				}
				
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				
				c.add(Calendar.YEAR, (Years * -1));
				
				if (Months != 0) {
					c.add(Calendar.MONTH, (Months * -1));
				}
				
				//Se invierten las fechas solo por una cuestion de comparacion correcta
				dateEvaluar = dateOriginal;
				dateOriginal = c.getTime();				
			}
			
			if (operador.equals("=")) {
				if (dateOriginal == dateEvaluar) {
					result = true;
				}
			}
			else if (operador.equals("<>")) {
				if (dateOriginal != dateEvaluar) {
					result = true;
				}
			}
			else if (operador.equals("<")) {
				if (dateOriginal.before(dateEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals(">")) {
				if (dateOriginal.after(dateEvaluar)) {
					result = true;
				}
			}
			else if (operador.equals("<=")) {
				if (dateOriginal.before(dateEvaluar) || dateOriginal == dateEvaluar) {
					result = true;
				}
			}
			else if (operador.equals(">=")) {
				if (dateOriginal.after(dateEvaluar) || dateOriginal == dateEvaluar) {
					result = true;
				}
			}
			else {
				throw new Exception("Tratamiento: " + tratamiento.toString() + " Operador: " + operador);
			}
			break;
		case FIR:
			if (operador.equals("=")) {
				if (valorEvaluar.length() == valorOriginal.length()) {
					if (idValorOriginal == idValorEvaluar) {
						result = true;
					}
				}
			}
			else {
				throw new Exception("Tratamiento: " + tratamiento.toString() + " Operador: " + operador);
			}
			break;
		case OP2:
		case OP:
		case LO:
		case LOIMPR:
		case LC:
		case LD:
		case SO:
		case SOC:
		case ECG:
		case PIC:
		case NA:
			if (operador.equals("=")) {
				if (valorEvaluar.toLowerCase().equals("true")) {
					if (idValorOriginal == idValorEvaluar) {
						result = true;
					}
				} else if (valorEvaluar.toLowerCase().equals("false")) {
					if (idValorOriginal != idValorEvaluar) {
						result = true;
					}
				} else {
					throw new Exception("Tratamiento: " + tratamiento.toString() + " Valor: " + valorEvaluar);
				}
			}
			else {
				throw new Exception("Tratamiento: " + tratamiento.toString() + " Operador: " + operador);
			}
			break;
		default:
			break;
		}		
		
		return result;
	}

	
	private AlertDispatcherHelper helper;
	protected HCDigitalApplication appState;

	@Override
	public void onInit(PerfilGUI perfilGUI, Context context) {
		this.helper = new AlertDispatcherHelper(perfilGUI);
		this.appState = (HCDigitalApplication) context.getApplicationContext();
	}

	@Override
	public void onFinish() {}

	/**
	 * formato de val:
	 * 		"EjeX|EjeY|NombreDeProvincia|NombreDeCiudad|Calle|Altura|SufijoYDescripcionDeBarrio"
	 *     o "idCampo|idCampoValor"
	 * 
	 * @return String
	 * "google.navigation:q=housenumber+street+city+state+zip+country"
	 * "google.navigation:q=" + point.getLatitude() + "," + point.getLongitude()
	 */
    @Override
	public String parseNavigationURI(String val) {
		String uri = "";
		String id = "google.navigation:q=";

		if (TextUtils.isEmpty(val)) {
			return uri;
		}
		
		if (val.contains("&")) {
			String[] arrVal = val.split("&");
			for (String valores : arrVal) {

				// viene por parametro con idCampo
				if (!valores.contains("|")) {
					String domicilio = (String) helper.getFormatedValue(Integer.parseInt(valores));
					if (!TextUtils.isEmpty(domicilio)) {
						uri = id;
						uri += domicilio.replace(" ", "+");
					}

				}
						
				// viene por parametro con idCampo|idCampoValor
				String[] arrVal2 = valores.split("\\|");
				if (arrVal2.length == 2 && Utils.isInteger(arrVal2[0]) && Utils.isInteger(arrVal2[1])) {
					String domicilio = (String) helper.getFormatedValue(Integer.parseInt(arrVal2[0]), Integer.parseInt(arrVal2[1]));
					if (!TextUtils.isEmpty(domicilio)) {
						uri = id;
						uri += domicilio.replace(" ", "+");
					}
				}

			}
			
			return uri;
			
		} else {
			// viene por parametro con idCampo
			if (!val.contains("|")) {
				String domicilio = (String) helper.getFormatedValue(Integer.parseInt(val));
				if (!TextUtils.isEmpty(domicilio)) {
					uri = id;
					uri += domicilio.replace(" ", "+");
				}
				return uri;
			}
					
			// viene por parametro con idCampo|idCampoValor
			String[] arrVal = val.split("\\|");
			if (arrVal.length == 2 && Utils.isInteger(arrVal[0]) && Utils.isInteger(arrVal[1])) {
				String domicilio = (String) helper.getFormatedValue(Integer.valueOf(arrVal[0]), Integer.valueOf(arrVal[1]));
				if (!TextUtils.isEmpty(domicilio)) {
					uri = id;
					uri += domicilio.replace(" ", "+");
					return uri; 
				}
			}

			// viene latitud y longitud
			if (!TextUtils.isEmpty(arrVal[0])) {
				uri = id;
				uri += arrVal[0] +","+ arrVal[1];
				return uri;
			}
			
			// viene descripcion del domicilio
			if (!TextUtils.isEmpty(arrVal[2]) || !TextUtils.isEmpty(arrVal[3]) ||
					!TextUtils.isEmpty(arrVal[4]) || !TextUtils.isEmpty(arrVal[5])) {

				StringBuilder values = new StringBuilder();

				values.append(id);
				values.append(arrVal[2]+"+"); //NombreDeProvincia
				values.append(arrVal[3]+"+"); //NombreDeCiudad
				values.append(arrVal[4]+"+"); //Calle
				values.append(arrVal[5]); //Altura
				uri = values.toString().replaceAll(" ", "+");
			}
		} 

		return uri;
	}
	
	private String deleteBeetween(String sentence, String firstParam, String secondParam) {
		return sentence.replaceAll("\\" + firstParam + ".*\\" + secondParam, "");		
	}
	
	private String getBeetween(String sentence, String firstParam, String secondParam) {
		return sentence.substring(sentence.indexOf(firstParam) + 1, sentence.indexOf(secondParam));			
	}

}
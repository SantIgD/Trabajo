package coop.tecso.hcd.helpers;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.dao.ReglaCondicionDAO;
import coop.tecso.hcd.dao.ReglaDAO;
import coop.tecso.hcd.entities.Regla;
import coop.tecso.hcd.entities.ReglaCondicion;
import coop.tecso.hcd.gui.components.CheckListGUI;
import coop.tecso.hcd.gui.components.Component;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.utils.SeccionCampoGUIReference;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;

public class CepoController {

    private PerfilGUI perfilGUI;

    private Map<Regla, List<ReglaCondicion>> reglas = new HashMap<>();

    // MARK: - Init

    public CepoController(PerfilGUI perfilGUI) {
        this.perfilGUI = perfilGUI;

        this.fetchReglas();
    }

    // MARK: - Interface

    public void showNeededGUIComponents() {
        List<Regla> reglas = getMatchedRules();

        for (Regla regla: reglas) {
            showGUIComponent(regla);
        }
    }

    // MARK: - Internal

    private void fetchReglas() {
        Context context = perfilGUI.getContext();
        ReglaDAO reglaDAO = new ReglaDAO(context);
        int aplicacionPerfil = this.getAplicacionPerfil();
        for (Regla regla: reglaDAO.getReglas()) {
            ReglaCondicionDAO reglaCondicionDAO = new ReglaCondicionDAO(context);
            List<ReglaCondicion> reglaCondiciones = reglaCondicionDAO.getReglaCondiciones(regla, aplicacionPerfil);
            if (!reglaCondiciones.isEmpty()) {
                this.reglas.put(regla, reglaCondiciones);
            }
        }
    }

    private int getAplicacionPerfil() {
        AplicacionPerfil aplicacionPerfil = (AplicacionPerfil) perfilGUI.getEntity();
        return aplicacionPerfil.getAplicacion().getId();
    }

    private List<Regla> getMatchedRules() {
        return CollectionUtils.filterKeys(reglas, (Regla rule,List<ReglaCondicion> conditions) -> {  //La lambda implementa el metodo de la interfaz
            for (ReglaCondicion condition : conditions) {
                int campoID = condition.campo;
                Component component = perfilGUI.getComponentForCampoID(campoID);
                if (!conditionFilter(condition,component)){
                    return false;
                }
            }
            return true;
        });
    }

    private List<String> getValues(Component component){ // Adaptado a como recibimos la información util.
        return CollectionUtils.map(component.values(),(value)->{
            String codigoEntidadBusqueda = value.getCodigoEntidadBusqueda();
            if (codigoEntidadBusqueda != null){
                return codigoEntidadBusqueda;
            }else{
                return value.getValor();
            }
        });
    }

    private boolean conditionFilter(ReglaCondicion condition, Component component){
        String operador = condition.operador;
        String[] valores  = condition.valor.split(","); // las comas representan "||"
        List<String> componentValues = getValues(component);

        if (operador.equals("=")) { //modificar si se agregan mas operadores
            return containsValue(componentValues,valores);
        }else{
            return !containsValue(componentValues,valores);
        }
    }

    private boolean containsValue(List<String> componentValues,String[] valores){
        for (String componentValue : componentValues) {
            if (CollectionUtils.contains(valores, componentValue)) {
                return true;
            }
        }
        return false;
    }


    private void showGUIComponent(Regla regla) {
        SeccionCampoGUIReference guiReference = SeccionCampoGUIReference.findMatch(regla, perfilGUI);

        guiReference.seccionGUI.mostrar();

        if (guiReference.campoGUI instanceof CheckListGUI) {
            CheckListGUI checkListGUI = (CheckListGUI) guiReference.campoGUI;
            checkListGUI.setChecked(guiReference.campoValorGUI);
        }

    }

}

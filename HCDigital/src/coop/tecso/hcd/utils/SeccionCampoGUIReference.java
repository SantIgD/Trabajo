package coop.tecso.hcd.gui.utils;

import android.support.annotation.NonNull;

import java.util.List;

import coop.tecso.hcd.entities.Regla;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.Component;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValorOpcion;

@SuppressWarnings("WeakerAccess")
public class SeccionCampoGUIReference {

    // MARK: - Data

    public SeccionGUI seccionGUI;
    public CampoGUI campoGUI;
    public CampoGUI campoValorGUI;
    public CampoGUI campoValorOpcionGUI;

    // MARK: - Static

    @NonNull
    public static SeccionCampoGUIReference findMatch(Regla regla, PerfilGUI perfilGUI) {
        SeccionCampoGUIReference result = new SeccionCampoGUIReference();
        List<Component> sectionsComponents = perfilGUI.getComponents();
        if (CollectionUtils.isEmpty(sectionsComponents)) {
            return result;
        }

        for (Component sectionComponent: sectionsComponents) {
            if (!(sectionComponent instanceof SeccionGUI)) {
                continue;
            }
            SeccionGUI seccionGUI = (SeccionGUI) sectionComponent;

            CampoGUI campoGUI = findCampoMatch(regla, seccionGUI);
            if (campoGUI == null) {
                continue;
            }

            result.seccionGUI = seccionGUI;
            result.campoGUI = campoGUI;
            CampoGUI campoValorGUI = findCampoValorMatch(regla, campoGUI);
            if (campoValorGUI == null) {
                return result;
            }

            result.campoValorGUI = campoValorGUI;
            CampoGUI campoValorOpcionGUI = findCampoValorOpcionMatch(regla, campoValorGUI);
            if (campoValorOpcionGUI == null) {
                return result;
            }

            result.campoValorOpcionGUI = campoValorOpcionGUI;
            return result;
        }

        return null;
    }

    // MARK: - Internal

    private static CampoGUI findCampoMatch(Regla regla, SeccionGUI seccionGUI) {
        List<Component> campos = seccionGUI.getComponents();
        if (CollectionUtils.isEmpty(campos) || regla.campoObligatorio == null) {
            return null;
        }

        int campoID = regla.campoObligatorio;

        for (Component campoComponent: campos) {
            AbstractEntity entity = campoComponent.getEntity();
            if (entity instanceof AplPerfilSeccionCampo) {
                AplPerfilSeccionCampo aplPerfilSeccionCampo = (AplPerfilSeccionCampo) entity;
                if (aplPerfilSeccionCampo.getId() == campoID){
                    return (CampoGUI) campoComponent;
                }
            }
        }

        return null;
    }

    private static CampoGUI findCampoValorMatch(Regla regla, CampoGUI campoGUI) {
        List<Component> campoValores = campoGUI.getComponents();
        if (CollectionUtils.isEmpty(campoValores) || regla.campoValorOblig == null) {
            return null;
        }

        int campoValorID = regla.campoValorOblig;

        for (Component campoValorComponent: campoValores) {
            AbstractEntity entity = campoValorComponent.getEntity();
            if (entity instanceof AplPerfilSeccionCampoValor) {
                AplPerfilSeccionCampoValor aplPerfilSeccionCampoValor = (AplPerfilSeccionCampoValor) entity;
                if (aplPerfilSeccionCampoValor.getId() == campoValorID) {
                    return (CampoGUI) campoValorComponent;
                }
            }
        }

        return null;
    }

    private static CampoGUI findCampoValorOpcionMatch(Regla regla, CampoGUI campoValorGUI) {
        List<Component> campoValoreOpciones = campoValorGUI.getComponents();
        if (CollectionUtils.isEmpty(campoValoreOpciones) || regla.campoValorOpcionOblig == null) {
            return null;
        }

        int campoValorOpcionID = regla.campoValorOpcionOblig;

        for (Component campoValorOpcionComponent: campoValoreOpciones) {
            AbstractEntity entity = campoValorOpcionComponent.getEntity();
            if (entity instanceof AplPerfilSeccionCampoValorOpcion) {
                AplPerfilSeccionCampoValorOpcion aplPerfilSeccionCampoValorOpcion = (AplPerfilSeccionCampoValorOpcion) entity;
                if (aplPerfilSeccionCampoValorOpcion.getId() == campoValorOpcionID) {
                    return (CampoGUI) campoValorOpcionComponent;
                }
            }
        }

        return null;
    }

}
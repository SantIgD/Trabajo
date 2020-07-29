package coop.tecso.hcd.gui.components.ecg;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.LabelGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;

/**
 * Campos Seccion Score
 */
@TargetApi(18)
@SuppressWarnings({"WeakerAccess", "ClickableViewAccessibility"})
public class ScoreECGGUI extends CampoGUI {

    private List<ScoreCheckBox> scoreCheckBoxes;
    private Map<String, String> mapGrupoCheckSeleccionado;
    private HashMap<String,TextView> grupoTextView;

    public ScoreECGGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    // Metodos

    @Override
    public View build() {
        List<CampoScore> campoScoreList = getCampos();

        // Se define un layout lineal vertical para armar el campo desplegable
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        layout.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        this.scoreCheckBoxes = new ArrayList<>();
        this.mapGrupoCheckSeleccionado = new HashMap<>();
        this.grupoTextView = new HashMap<>();

        for (CampoScore campoScore : campoScoreList) {
            // Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
            LinearLayout titleLayout = new LinearLayout(context);
            titleLayout.setOrientation(LinearLayout.HORIZONTAL);
            titleLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
            titleLayout.setOrientation(LinearLayout.HORIZONTAL);
            titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

            // 	 Etiqueta
            TextView label = new TextView(context);
            label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
            label.setTextColor(context.getResources().getColor(R.color.label_text_color));
            label.setText(campoScore.descripcion);
            label.setGravity(Gravity.CENTER_VERTICAL);
            label.setTypeface(null, Typeface.BOLD);
            label.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

            titleLayout.addView(label);

            // Se define un layout de tipo Tabla para contener los distintos Campos
            TableLayout tableLayout = new TableLayout(context);
            tableLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
            tableLayout.setColumnStretchable(1, true);
            tableLayout.setColumnShrinkable(1, true);
            tableLayout.setColumnStretchable(1, true);

            // Espacio separador
            View gap = new View(context);
            gap.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

            // Se agregan partes del componente al layout contenedor
            layout.addView(titleLayout);
            layout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
            layout.addView(tableLayout);

            List<CampoScoreOpcion> campoScoreOpcionList = campoScore.opciones;

            CampoScoreOpcion cpoSreOption = campoScore.opciones.get(0);
            if (cpoSreOption != null) {
                this.grupoTextView.put(cpoSreOption.grupo, label);
            }

            for (CampoScoreOpcion campoScoreOpcion : campoScoreOpcionList) {
               final ScoreCheckBox scoreCheckBox = getCustomCheckBox(campoScoreOpcion);
                scoreCheckBox.tituloAgrupacion = campoScore.descripcion;
                scoreCheckBox.labelGUI.setEnabled(enabled);
                tableLayout.addView(scoreCheckBox.labelGUI.getView());
               this.scoreCheckBoxes.add(scoreCheckBox);
            }
        }

        layout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int idNoCmbECGInterpretado = getIdNoCmbECGInterpretadoParametro();
            int idSeleccionadCmbECGInterpretado = getIdCampoValorSeleccionadoComboECGInterpretado();
            boolean isUserSucursalRosario = (appState.getCurrentUser().getSucursal().getId() == 1);
            boolean isUserSucursalCordoba = (appState.getCurrentUser().getSucursal().getId() == 2);
            boolean isCmbECGInterpretadoNo = (idNoCmbECGInterpretado == idSeleccionadCmbECGInterpretado);
            SeccionGUI seccionGUI = getSeccionGUI();

            if (ECGHelper.isObligatorioPatronECGInterpretado(appState)) {
                if (isUserSucursalRosario) {
                    if (isCmbECGInterpretadoNo) {
                        seccionGUI.mostrar();
                    } else {
                        seccionGUI.ocultar();
                    }
                }
                if (isUserSucursalCordoba) {
                    seccionGUI.mostrar();
                    ECGHelper.changeValueCmbElectroSI(context);
                }
            } else {
                seccionGUI.ocultar();
                ECGHelper.enableCmbEsElectrocardiograma(context);
            }
        });

        this.view = layout;

        chequearCamposIniciales();

        return this.view;
    }

    private void chequearCamposIniciales() {
        List<Value> values = this.getInitialValues();

        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        this.values = values;
        String checksSeleccionadosString = values.get(0).getValor();
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(checksSeleccionadosString).getAsJsonArray();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement jsonElement =  jsonArray.get(i);
            ScoreCheckBox scoreCheckBox = getCustomCheckBoxByJson(jsonElement);
            if (scoreCheckBox != null) {
                scoreCheckBox.checkBox.setChecked(true);
                this.mapGrupoCheckSeleccionado.put(scoreCheckBox.grupo, scoreCheckBox.json);
            }
        }
    }

    private ScoreCheckBox getCustomCheckBoxByJson(JsonElement jsonElement) {
        if (CollectionUtils.isEmpty(scoreCheckBoxes)) {
            return null;
        }

        for (ScoreCheckBox scoreCheckBox : this.scoreCheckBoxes) {
            String json = jsonElement.getAsString();
            if (scoreCheckBox.json.equals(json)) {
               return scoreCheckBox;
            }
        }

        return null;
    }

    @Override
    public List<Value> values() {
        this.values = new ArrayList<>();
        AplPerfilSeccionCampo campo = (AplPerfilSeccionCampo)this.entity;
        JSONArray jsonArray = new JSONArray(mapGrupoCheckSeleccionado.values());
        String valor = jsonArray.toString();
        Value value = new Value(campo, null, null, valor, null, null, null);
        this.values.add(value);
        return this.values;
    }

    private ScoreCheckBox getCustomCheckBox(CampoScoreOpcion campoScoreOpcion) {
        return new ScoreCheckBox(campoScoreOpcion, this);
    }

    class ScoreCheckBox {
        private String tituloAgrupacion;
        public LabelGUI labelGUI;
        public CheckBox checkBox;
        public String grupo;
        public String json;
        public int peso;

        public ScoreCheckBox(final CampoScoreOpcion campoScoreOpcion, final ScoreECGGUI scoreECGGUI) {
            this.labelGUI = new LabelGUI(context);
            this.labelGUI.setEtiqueta(campoScoreOpcion.descripcion);
            this.labelGUI.build();
            this.labelGUI.setEnabled(true);
            this.labelGUI.getCheckBox().setChecked(false);

            this.checkBox = labelGUI.getCheckBox();
            this.grupo = campoScoreOpcion.grupo;
            this.json = toJSON(campoScoreOpcion);
            this.peso = campoScoreOpcion.peso;


            this.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uncheckAll(grupo);
                    ScoreCheckBox.this.checkBox.setChecked(true);
                    String seleccionado = toJSON(campoScoreOpcion);
                    scoreECGGUI.mapGrupoCheckSeleccionado.put(grupo, seleccionado);


                    if (isAllGroupsSelected()) {
                        int puntosSumados = getSumaPuntosCheckboxes();
                        buildAlertSumaScore(puntosSumados);
                    }
                }

                private void uncheckAll(String grupo) {
                    for (ScoreCheckBox scoreCheckBox : scoreECGGUI.scoreCheckBoxes) {
                        if (scoreCheckBox.grupo.equals(grupo)) {
                            scoreCheckBox.checkBox.setChecked(false);
                        }
                    }
                }

            });

        }

        private String toJSON(CampoScoreOpcion campoScoreOpcion) {
            JSONObject jsonObject= new JSONObject();
            try {
                jsonObject.put("opcion", campoScoreOpcion.descripcion);
                jsonObject.put("grupo", campoScoreOpcion.grupo);
                jsonObject.put("peso", campoScoreOpcion.peso);
                jsonObject.put("orden", campoScoreOpcion.orden);

                return jsonObject.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    private List<CampoScore> getCampos() {
        SQLHelper sqlHelper = new SQLHelper(context);
        sqlHelper.openDatabase();

        int idSucursal = appState.getCurrentUser().getSucursal().getId();
        Integer scoreType = ParamHelper.getInteger(ParamHelper.SCORE_TIPO_CARDIO, 1);
        String selection = "idSucursal = "+idSucursal + " AND tipo = " + scoreType;
        String[] columnas = {"idSucursal","grupo","titulo","ordenTitulo","opcion","ordenOpcion", "peso"};
        Cursor cursor = sqlHelper.query("hcd_score", columnas , selection, null,null,null, "ordenTitulo, ordenOpcion");

        Map<String, CampoScore> mapScore = new HashMap<>();

        while (cursor.moveToNext()) {
           int currentIdSucursal = cursor.getInt(0);
           String grupo = cursor.getString(1);
           String titulo = cursor.getString(2);
           int ordenTitulo = cursor.getInt(3);
           String opcion = cursor.getString(4);
           int ordenOpcion = cursor.getInt(5);
           int peso = cursor.getInt(6);

            if (!mapScore.containsKey(grupo)) {
                CampoScore campoScore = new CampoScore();
                campoScore.descripcion = titulo;
                campoScore.orden = ordenTitulo;
                campoScore.idSucursal = currentIdSucursal;

                CampoScoreOpcion campoScoreOpcion = new CampoScoreOpcion();
                campoScoreOpcion.descripcion = opcion;
                campoScoreOpcion.orden = ordenOpcion;
                campoScoreOpcion.peso = peso;
                campoScoreOpcion.grupo = grupo;

                ArrayList<CampoScoreOpcion> campoScoreOpcionList = new ArrayList<CampoScoreOpcion>();
                campoScoreOpcionList.add(campoScoreOpcion);

                campoScore.opciones = campoScoreOpcionList;

                mapScore.put(grupo, campoScore);
            } else {
                CampoScoreOpcion campoScoreOpcion = new CampoScoreOpcion();
                campoScoreOpcion.descripcion = opcion;
                campoScoreOpcion.orden = ordenOpcion;
                campoScoreOpcion.peso = peso;
                campoScoreOpcion.grupo = grupo;

                mapScore.get(grupo).opciones.add(campoScoreOpcion);
            }

        }
        sqlHelper.closeDatabase();

        ArrayList<CampoScore> campos = new ArrayList<>(mapScore.values());

        CollectionUtils.sort(campos, (campo1, campo2) -> campo1.orden - campo2.orden);
        for (CampoScore campo: campos) {
            CollectionUtils.sort(campo.opciones, (opcion1, opcion2) -> opcion1.orden - opcion2.orden);
        }

        return campos;
    }

    static class CampoScore {
        public int idSucursal;
        public String descripcion;
        public int orden;
        public ArrayList<CampoScoreOpcion> opciones;
    }

    static class CampoScoreOpcion {
        public String descripcion;
        public int peso;
        public int orden;
        public String grupo;
    }

    private Map<String, List<ScoreCheckBox>> getMapGrupoListaCheckbox() {
        Map<String, List<ScoreCheckBox>> mapGrupoListCheckBox = new HashMap<>();

        for (ScoreCheckBox scoreCheckBox : this.scoreCheckBoxes) {
            if (!mapGrupoListCheckBox.containsKey(scoreCheckBox.grupo)) {
                List<ScoreCheckBox> scoreCheckBoxes = new ArrayList<>();
                scoreCheckBoxes.add(scoreCheckBox);
                mapGrupoListCheckBox.put(scoreCheckBox.grupo, scoreCheckBoxes);
            }else{
                List<ScoreCheckBox> list = mapGrupoListCheckBox.get(scoreCheckBox.grupo);
                list.add(scoreCheckBox);
                mapGrupoListCheckBox.put(scoreCheckBox.grupo, list);
            }
        }

        return mapGrupoListCheckBox;
    }

    private boolean validateAllGroupsSelected() {
        boolean isValid = true;
        Map<String, List<ScoreCheckBox>> mapGrupoListCheckBox = getMapGrupoListaCheckbox();

        if (CollectionUtils.isEmpty(mapGrupoListCheckBox)) {
            return false;
        }
        for (String grupo: mapGrupoListCheckBox.keySet()) {
            List<ScoreCheckBox> checkboxes = mapGrupoListCheckBox.get(grupo);

            TextView textView = grupoTextView.get(grupo);

            if (!isGroupSelected(checkboxes)) {
                MarcarErrorCampoObligatorio(textView);
                isValid = false;
            } else {
                DesmarcarErrorCampoObligatorio(textView);

            }
        }
        return isValid;
    }

    private boolean isAllGroupsSelected() {
        Map<String, List<ScoreCheckBox>> mapGrupoListCheckBox = getMapGrupoListaCheckbox();

        if (CollectionUtils.isEmpty(mapGrupoListCheckBox)) {
            return false;
        }

        for (String grupo: mapGrupoListCheckBox.keySet()) {
            List<ScoreCheckBox> checkboxes = mapGrupoListCheckBox.get(grupo);

            if (!isGroupSelected(checkboxes)) {
                return false;
            }
        }
        return true;
    }

    private int getSumaPuntosCheckboxes() {
        if (CollectionUtils.isEmpty(this.scoreCheckBoxes)) {
            return 0;
        }

        int puntos = 0;
        for (ScoreCheckBox scoreCheckBox : this.scoreCheckBoxes) {
            if (scoreCheckBox.checkBox.isChecked()) {
                puntos += scoreCheckBox.peso;
            }
        }

        return puntos;
    }

    private boolean isGroupSelected(List<ScoreCheckBox> scoreCheckBoxes) {
        boolean isGroupSelected = false;

        for (ScoreCheckBox scoreCheckBox : scoreCheckBoxes) {
            if (scoreCheckBox.checkBox.isChecked()) {
                isGroupSelected = true;
                break;
            }
        }

        return isGroupSelected;
    }

    @Override
    public boolean validate() {
        boolean valid = true;
        int idValorSeleccionadoCmbECGInterpretado = getIdCampoValorSeleccionadoComboECGInterpretado();
        int idValorNoCmbECGInterpretado = getIdNoCmbECGInterpretadoParametro();
        boolean valorIsNo = idValorSeleccionadoCmbECGInterpretado == idValorNoCmbECGInterpretado;
        boolean isUserSucursalRosario = (appState.getCurrentUser().getSucursal().getId() == 1);
        boolean isUserSucursalCordoba = (appState.getCurrentUser().getSucursal().getId() == 2);

        if (isUserSucursalRosario) {
            if (valorIsNo){
                valid = this.validateAllGroupsSelected();
            }
        }

        if (isUserSucursalCordoba) {
            boolean isObligatorio = ECGHelper.isObligatorioPatronECGInterpretado(appState);
            if (isObligatorio) {
                valid = this.validateAllGroupsSelected();
            }
        }

        return valid;
    }

    private int getIdCampoValorSeleccionadoComboECGInterpretado() {
        String idComboDeSeccionECG = ParamHelper.getString(ParamHelper.CAMPO_PATRON_ECG_INTERPRETADO_ID,null);
        Value value = appState.getForm().getValorByCampoID(Integer.parseInt(idComboDeSeccionECG));
        return value.getCampoValor().getCampoValor().getId();
    }

    private int getIdNoCmbECGInterpretadoParametro() {
        return ParamHelper.getInteger(ParamHelper.ID_VALOR_NO_CMB_ECGINTERPRETADO,0);
    }

    private void buildAlertSumaScore(int puntosSumados) {
        boolean isUserSucursalRosario = (appState.getCurrentUser().getSucursal().getId() == 1);
        boolean isUserSucursalCordoba = (appState.getCurrentUser().getSucursal().getId() == 2);
        final Dialog alert = new Dialog(context);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.alerta_con_divisor);
        TextView tvTitulo = alert.findViewById(R.id.titulo);
        TextView tvCuerpo = alert.findViewById(R.id.cuerpo);
        ImageView imgDivisor = alert.findViewById(R.id.divisor);

        String mensaje = "";
        if (puntosSumados <= 3 ) {
            if (isUserSucursalCordoba) {
                mensaje = ParamHelper.getString(ParamHelper.MSJ_ALERTA_SCORE_RIESGO_BAJO_CORDOBA,"RESULTADO DEL SCORE: [X] PUNTOS - Riesgo Bajo");
                mensaje = mensaje.replace("[X]",String.valueOf(puntosSumados));
            } else if (isUserSucursalRosario) {
                mensaje = ParamHelper.getString(ParamHelper.MSJ_ALERTA_SCORE_RIESGO_BAJO_ROSARIO,"RIESGO BAJO - El paciente no presenta riesgos");
            }
            imgDivisor.setBackgroundColor(Color.parseColor("#FF4AB955"));
            tvTitulo.setTextColor(Color.parseColor("#FF4AB955"));
        } else {
            if (isUserSucursalCordoba) {
                mensaje = ParamHelper.getString(ParamHelper.MSJ_ALERTA_SCORE_RIESGO_ALTO_CORDOBA,"RESULTADO DEL SCORE: [X] PUNTOS - RIESGO ALTO Derivación a Centro De Alta Complejidad");
                mensaje = mensaje.replace("[X]",String.valueOf(puntosSumados));
            } else if (isUserSucursalRosario) {
                mensaje = ParamHelper.getString(ParamHelper.MSJ_ALERTA_SCORE_RIESGO_ALTO_ROSARIO,"RIESGO ALTO - Derive a centro asistencial para completar evaluación");
            }
            imgDivisor.setBackgroundColor(Color.RED);
            tvTitulo.setTextColor(Color.RED);
        }

        String[] arrayTituloCuerpo = mensaje.split("-");
        tvTitulo.setText(arrayTituloCuerpo[0].trim());
        tvCuerpo.setText(arrayTituloCuerpo[1].trim());

        Button btnAceptar = alert.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(v -> alert.dismiss());

        alert.show();
    }
    public void MarcarErrorCampoObligatorio (TextView label){
        if (!label.hasFocus()) {
            label.setError("Seleccione una opción");
        }
    }

    public void DesmarcarErrorCampoObligatorio (TextView label) {
        label.setError(null);
    }
}

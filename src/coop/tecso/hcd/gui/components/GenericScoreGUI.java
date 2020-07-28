package coop.tecso.hcd.gui.components;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;

@SuppressWarnings("WeakerAccess")
public class GenericScoreGUI extends CampoGUI {

    private List<ScoreCheckBox> scoreCheckBoxes = new ArrayList<>();
    private Map<String, String> mapGrupoCheckSeleccionado;
    private Map<String,TextView> grupoTextView = new HashMap<>();
    private TextView keyEmptyGroup = null;
    private boolean keyEmptyGroupIsClean = true;

    public GenericScoreGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    // MARK: - Metodos

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
        this.mapGrupoCheckSeleccionado = new HashMap<>();

        for (CampoScore campoScore : campoScoreList){
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



        layout.getViewTreeObserver().addOnGlobalLayoutListener(this::globalLayoutDidChange);

        this.view = layout;
        chequearCamposIniciales();
        return this.view;
    }

    @Override
    public List<Value> values() {
        AplPerfilSeccionCampo campo = (AplPerfilSeccionCampo) this.entity;
        JSONArray jsonArray = new JSONArray(mapGrupoCheckSeleccionado.values());
        String valor = jsonArray.toString();
        Value value = new Value(campo, null, null, valor, null, null, null);

        this.values = Collections.singletonList(value);
        return this.values;
    }
    
    // MARK: - Overridable

    protected void onScoreFilled(int puntosSumados) {}

    protected Cursor getCamposCursor(SQLHelper sqlHelper) { return null; }

    protected void globalLayoutDidChange() {}

    // MARK: - Internal

    private void chequearCamposIniciales(){
        List<Value> values = this.getInitialValues();

        if (CollectionUtils.isEmpty(values)) {
            return;
        }

        this.values = values;
        String checksSeleccionadosString = values.get(0).getValor();
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(checksSeleccionadosString).getAsJsonArray();

        for (JsonElement jsonElement: jsonArray){
            ScoreCheckBox scoreCheckBox = getCustomCheckBoxByJson(jsonElement);
            if (scoreCheckBox != null) {
                scoreCheckBox.checkBox.setChecked(true);
                this.mapGrupoCheckSeleccionado.put(scoreCheckBox.grupo, scoreCheckBox.json);
            }
        }
    }

    private ScoreCheckBox getCustomCheckBoxByJson(JsonElement jsonElement){
        if (CollectionUtils.isEmpty(scoreCheckBoxes)) {
            return null;
        }

        for (ScoreCheckBox scoreCheckBox : this.scoreCheckBoxes) {
            String json = jsonElement.getAsString();
            if(scoreCheckBox.json.equals(json)){
                return scoreCheckBox;
            }
        }

        return null;
    }

    private ScoreCheckBox getCustomCheckBox(CampoScoreOpcion campoScoreOpcion){
        return new ScoreCheckBox(campoScoreOpcion, this);
    }

    private List<CampoScore> getCampos(){
        SQLHelper sqlHelper = new SQLHelper(context);
        sqlHelper.openDatabase();

        Cursor cursor = this.getCamposCursor(sqlHelper);

        Map<String, CampoScore> mapScore = new HashMap<>();

        while (cursor.moveToNext()){
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

                ArrayList<CampoScoreOpcion> campoScoreOpcionList = new ArrayList<>();
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

    private Map<String, List<ScoreCheckBox>> getMapGrupoListaCheckbox(){
        Map<String, List<ScoreCheckBox>> mapGrupoListCheckBox = new HashMap<>();

        for (ScoreCheckBox scoreCheckBox : this.scoreCheckBoxes) {
            if (!mapGrupoListCheckBox.containsKey(scoreCheckBox.grupo)) {
                List<ScoreCheckBox> scoreCheckBoxes = new ArrayList<>();
                scoreCheckBoxes.add(scoreCheckBox);
                mapGrupoListCheckBox.put(scoreCheckBox.grupo, scoreCheckBoxes);
            } else {
                List<ScoreCheckBox> list = mapGrupoListCheckBox.get(scoreCheckBox.grupo);
                list.add(scoreCheckBox);
                mapGrupoListCheckBox.put(scoreCheckBox.grupo, list);
            }
        }

        return mapGrupoListCheckBox;
    }



    public boolean isAllGroupsSelected(){
        Map<String, List<ScoreCheckBox>> mapGrupoListCheckBox = getMapGrupoListaCheckbox();
        if (CollectionUtils.isEmpty(mapGrupoListCheckBox)) {
            return false;
        }

        for (String grupo: mapGrupoListCheckBox.keySet()) {
            List<ScoreCheckBox> checkBoxes = mapGrupoListCheckBox.get(grupo);

            if (!isGroupSelected(checkBoxes)) {
                return false;
            }
        }

        return true;
    }

    // Recorre todos los checkbox en busca de los incompletos, marcándolos con error de ser así.
    // Es llamada desde LAPSS y ABC (ambos de ACV).

    public boolean ValidateAllGroupsSelected(){
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



    public int getSumaPuntosCheckboxes(){
        if (CollectionUtils.isEmpty(this.scoreCheckBoxes)) {
            return 0;
        }

        int puntos = 0;
        for (ScoreCheckBox scoreCheckBox : this.scoreCheckBoxes) {
            if(scoreCheckBox.checkBox.isChecked()){
                puntos += scoreCheckBox.peso;
            }
        }

        return puntos;
    }

    private boolean isGroupSelected(List<ScoreCheckBox> scoreCheckBoxes){
        boolean isGroupSelected = false;

        for (ScoreCheckBox scoreCheckBox : scoreCheckBoxes){
            if(scoreCheckBox.checkBox.isChecked()){
                isGroupSelected = true;
                break;
            }
        }

        return isGroupSelected;
    }







    // MARK: - Private Classes

    public class CampoScore {
        public int idSucursal;
        public String descripcion;
        public int orden;
        public ArrayList<CampoScoreOpcion> opciones;
    }

    private class CampoScoreOpcion {
        public String descripcion;
        public int peso;
        public int orden;
        public String grupo;
    }

    private class ScoreCheckBox {

        public String tituloAgrupacion;
        public LabelGUI labelGUI;
        public CheckBox checkBox;
        public String grupo;
        public String json;
        public int peso;

        public ScoreCheckBox(final CampoScoreOpcion campoScoreOpcion, final GenericScoreGUI scoreGUI){
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
                    mapGrupoCheckSeleccionado.put(grupo, seleccionado);


                    if(isAllGroupsSelected()){
                        int puntosSumados = getSumaPuntosCheckboxes();
                        onScoreFilled(puntosSumados);
                    }
                }

                private void uncheckAll(String grupo){
                    for (ScoreCheckBox scoreCheckBox : scoreCheckBoxes) {
                        if(scoreCheckBox.grupo.equals(grupo)){
                            scoreCheckBox.checkBox.setChecked(false);
                        }
                    }
                }

            });

        }

        private String toJSON(CampoScoreOpcion campoScoreOpcion){
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

    public void MarcarErrorCampoObligatorio (TextView label){
        if (!label.hasFocus()) {
            label.setError("Seleccione una opcion");
        }
    }

    public void DesmarcarErrorCampoObligatorio (TextView label){
        label.setError(null);
    }

}

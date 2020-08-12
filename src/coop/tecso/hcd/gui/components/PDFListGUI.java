package coop.tecso.hcd.gui.components;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.PdfRendererActivity;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.CollectionUtils;

@TargetApi(21)
public class PDFListGUI extends CampoGUI{

    public PDFListGUI(Context context, boolean enabled){
        super(context, enabled);
    }

    @Override
    public View build() {

        // Se define un layout lineal vertical para armar el campo desplegable
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setFocusable(true);
        layout.setFocusableInTouchMode(true);
        layout.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

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
        label.setText(this.getEtiqueta());
        label.setGravity(Gravity.CENTER_VERTICAL);
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

        List<Value> values = this.getInitialValues();

        if (!CollectionUtils.isEmpty(values)) {
            for (final Value value : values) {
                LinearLayout descripcionIconoLayout = new LinearLayout(context);
                descripcionIconoLayout.setOrientation(LinearLayout.HORIZONTAL);
                descripcionIconoLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
                descripcionIconoLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                TextView tv = new TextView(context);
                tv.setText(value.getValor());
                ImageButton btnPDF = new ImageButton(context);
                btnPDF.setBackgroundResource(R.drawable.ic_pdf);
                btnPDF.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                btnPDF.setEnabled(enabled);


                btnPDF.setOnClickListener(v -> {
                    if (Build.VERSION.SDK_INT < 21) {
                        buildAlertDialogVersionNoCompatible();
                    } else {
                        Intent intent = new Intent(context, PdfRendererActivity.class);
                        intent.putExtra("pdf", value.getExtraFile());
                        context.startActivity(intent);
                    }
                });

                LinearLayout btnPDFLayout = new LinearLayout(context);
                btnPDFLayout.setGravity(Gravity.END);
                btnPDFLayout.addView(btnPDF);
                btnPDFLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                descripcionIconoLayout.addView(tv);
                descripcionIconoLayout.addView(btnPDFLayout);
                tableLayout.addView(descripcionIconoLayout);
            }
        }

        this.view = layout;

        return this.view;
    }

    private AlertDialog buildAlertDialogVersionNoCompatible(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Versión incompatible");
        builder.setMessage("Versión incompatible para realizar esta operación");
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.accept, (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

}

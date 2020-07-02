package coop.tecso.hcd.gui.components;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import coop.tecso.hcd.R;

public class AlertaConDivisor {

    // MARK: - Data

    private Context context;

    public int color;

    public String title;

    public String message;

    // MARK: - Init

    public AlertaConDivisor(Context context) {
        this.context = context;
    }

    // MARK: - Interface

    public void setText(String text, String param) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        if (param != null) {
            text = text.replace("[X]", param);
        }

        String[] components = text.split("-");
        this.title = components[0].trim();

        if (components.length > 1) {
            this.message = components[1].trim();
        }
    }

    public void setText(String text) {
        this.setText(text, null);
    }

    public void show(Runnable buttonAction) {
        final Dialog alert = new Dialog(context);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.alerta_con_divisor);

        TextView tvTitulo = alert.findViewById(R.id.titulo);
        tvTitulo.setText(title);
        tvTitulo.setTextColor(color);

        TextView tvCuerpo = alert.findViewById(R.id.cuerpo);
        if (!TextUtils.isEmpty(message)) {
            tvCuerpo.setText(message);
        } else {
            tvCuerpo.setVisibility(View.GONE);
        }

        ImageView imgDivisor = alert.findViewById(R.id.divisor);
        imgDivisor.setBackgroundColor(color);

        Button btnAceptar = alert.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(v -> {
            if (buttonAction != null) {
                buttonAction.run();
            }

            alert.dismiss();
        });

        alert.show();
    }

    public void show() {
        this.show(null);
    }
}

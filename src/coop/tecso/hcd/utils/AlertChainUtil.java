package coop.tecso.hcd.utils;

/**
 * Clase pensada para permitir ejecutar una acción después de mostrar
 * varias alertas y haberlas procesado.
 */
public class AlertChainUtil {

    // MARK: - Data

    private int alertCounter = 0;

    private Runnable endRunnable;

    // MARK: - Interface

    public void onShowAlert() {
        this.alertCounter += 1;
    }

    public void onEndAlert() {
        if (alertCounter == 0) {
            return;
        }

        this.alertCounter -= 1;

        if (alertCounter == 0 && endRunnable != null) {
            this.endRunnable.run();
            this.endRunnable = null;
        }
    }

    public void runAtEnd(Runnable runnable) {
        if (alertCounter == 0) {
            runnable.run();
        } else {
            this.endRunnable = runnable;
        }
    }

}
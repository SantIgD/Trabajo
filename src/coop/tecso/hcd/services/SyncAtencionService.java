package coop.tecso.hcd.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.AtencionValor;
import coop.tecso.hcd.integration.UDAACoreService;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.util.DeviceContext;

public final class SyncAtencionService extends Service {

	private static final String LOG_TAG = SyncAtencionService.class.getSimpleName();

	private HCDigitalApplication appState;

	private static Timer timer;

	@Override
	public void onCreate() {
		this.appState = (HCDigitalApplication) getApplicationContext();

        ServiceStarter.startForeground(this);

		Long period = ParamHelper.getLong(ParamHelper.SYNC_TIMER_PERIOD, 30000L);
		timer = new Timer();
		timer.schedule(new MainTask(), 0, period);
		Log.i(LOG_TAG, String.format("**STARTING SERVICE** - period: %s seg. ",	period / 1000));
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		if (timer != null) {
			Log.i(LOG_TAG, "**SERVICE STOPPED**");
			timer.cancel();
			timer.purge();
			timer = null;
		}

		// send broadcast
		enviarComandosPS(Constants.REFRESH, Constants.REFRESH);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	// MARK: - Private

	@SuppressWarnings("SameParameterValue")
	private void enviarComandosPS(String commando , String message) {
		Intent intentBro = new Intent(Constants.ACTION_REFRESH);
		intentBro.putExtra(commando, message);
		appState.sendBroadcast(intentBro);
	}

	private class MainTask extends TimerTask {

		@Override
		public void run() {
			String threadName = Thread.currentThread().getName();
			Log.d(LOG_TAG, "Running process..." + threadName);

			// Check Internet connection
			if (!DeviceContext.hasConnectivity(appState)) {
				Log.e(LOG_TAG, "**Hasn't internet connectivity**");
				return;
			}

			List<Atencion> listaAtenciones = appState.getHCDigitalDAO().getListAtencionToSync();
			if (listaAtenciones.isEmpty()) {
				Log.d(LOG_TAG, "**EMPTY SYNC LIST**");
				stopSelf();
				return;
			}

			UDAACoreServiceImpl webService = new UDAACoreServiceImpl(SyncAtencionService.this);

			boolean isTransferTypePartial = this.isTransTypePartial(webService);
			Log.d( LOG_TAG, "Transmittion Type = " + isTransferTypePartial );

			List<String> ids = new ArrayList<>();

			for (Atencion atencionLazy: listaAtenciones) {
				Atencion atencion = appState.getHCDigitalDAO().getAtencionById(atencionLazy.getId());

				if (!atencion.isSyncHeader()) {
					int atencionServerID = syncAtencionToServer(webService, atencion);
					if (atencionServerID == 0) { // no se sincronizo
						if (isTransferTypePartial)
							continue;
						else
							return;
					}

					atencion.setAtencionServerID(atencionServerID);
					atencion.setSyncHeader(true);
					atencion.setIdEstado(atencion.getEstadoAtencion().getId());
					appState.getHCDigitalDAO().updateAtencion(atencion);
					appState.getHCDigitalDAO().updateAtencionCerrada(atencion.getId(), atencion.getEstadoAtencion().getId(), true, false);
				}

				if (atencion.isSyncHeader() && atencion.getAtencionServerID() > 0) {
					if (atencion.getFirmaDigital() != null) {
						boolean statusFirma = syncAtencionFirma(webService, atencion);
						if (!statusFirma) {
							if (isTransferTypePartial)
								continue;
							else
								return;
						}

						atencion.setSyncfirmaDigital(true);
						appState.getHCDigitalDAO().updateAtencion(atencion);
					}

					boolean statusAtencionValores = syncAtencionValoresToServer(webService, atencion, isTransferTypePartial);
					if (!statusAtencionValores) {
						return;
					}
				}

				if (atencion.isSyncHeader() && atencion.allImagesSincronized()) {
					boolean statusEstadoAtencion = syncAtencionEstado(webService, atencion);
					if (!statusEstadoAtencion) {
						if (isTransferTypePartial)
							continue;
						else
							return;
					}

					atencion.setDeleted(true);
					appState.getHCDigitalDAO().updateAtencion(atencion);
					ids.add(Integer.toString(atencion.getId()));
					appState.getHCDigitalDAO().updateAtencionCerrada(atencion.getId(), atencion.getEstadoAtencion().getId(), true, true);
				}
			}
			appState.removeGestures(ids);

			Intent intentRefreshList = new Intent(Constants.ACTION_REFRESH_LIST);
			sendBroadcast(intentRefreshList);
		}

		private boolean isTransTypePartial(UDAACoreService webService) {
			try {
				return webService.isTransTypePartial();
			}
			catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		private int syncAtencionToServer(UDAACoreServiceImpl webService, Atencion atencion) {
			try {
				return webService.syncAtencionToServer(atencion);
			} catch (Exception e) {
				Log.d(LOG_TAG, "**ERROR**", e);
				return 0;
			}
		}

		private boolean syncAtencionFirma(UDAACoreServiceImpl webService, Atencion atencion) {
			try {
				return webService.syncAtencionFirma(atencion);
			} catch (Exception e) {
				Log.d(LOG_TAG, "**ERROR**", e);
				return false;
			}
		}

		private boolean syncAtencionValorToServer(UDAACoreServiceImpl webService, Atencion atencion, AtencionValor atencionValor) {
			try {
				return webService.syncAtencionValorToServer(atencionValor, atencion);
			} catch (Exception e) {
				Log.d(LOG_TAG, "**ERROR**", e);
				return false;
			}
		}

		private boolean syncAtencionValoresToServer(UDAACoreServiceImpl webService, Atencion atencion, boolean isTransferTypePartial) {
			//lista de atencionesvalor a sincronizar
			List<AtencionValor> listAVToSync = appState.getHCDigitalDAO().findAllToSync(atencion);

			for (AtencionValor atencionValor: listAVToSync) {
				boolean status = syncAtencionValorToServer(webService, atencion, atencionValor);
				if (!status) {
					if (isTransferTypePartial)
						continue;
					else
						return false;
				}

				atencionValor.setSyncImagen(true);
				appState.getHCDigitalDAO().updateAtencionValor(atencionValor);
			}

			return true;
		}

		private boolean syncAtencionEstado(UDAACoreServiceImpl webService, Atencion atencion) {
			try {
				return webService.syncAtencionEstado(atencion);
			} catch (Exception e) {
				Log.d(LOG_TAG, "**ERROR**", e);
				return false;
			}
		}

	}

}
package coop.tecso.hcd.base;

import java.util.TimerTask;

import android.content.Context;
import coop.tecso.hcd.activities.TabHostActivity;



public final class SaveIMTask extends TimerTask {
	TabHostActivity tabHostActivity;
	public SaveIMTask(Context context){
		tabHostActivity = (TabHostActivity)context;
	}
	
	@Override
	public void run() {
		tabHostActivity.doGuardarIMs();
	}
}
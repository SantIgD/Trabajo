package coop.tecso.hcd.gui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;

public final class SoftKeyboardHandledLinearLayout extends LinearLayout {

	private boolean isKeyboardShown = false;
	private SoftKeyboardVisibilityChangeListener listener;
	boolean rezising = false;
	int lastOrientation = getResources().getConfiguration().orientation;
	 
	public SoftKeyboardHandledLinearLayout(Context context) {
		super(context);
	}
	 
	public SoftKeyboardHandledLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		listener.softKeyboardHide();
		if (isKeyboardShown) {
			isKeyboardShown = false;
			listener.onSoftKeyboardHide();
		}
	};
	
	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			// Keyboard is hidden <<< RIGHT
			if (isKeyboardShown) {
				rezising = true;
				isKeyboardShown = false;
				listener.onSoftKeyboardHide();
				lastOrientation = getResources().getConfiguration().orientation;
				rezising = false;
				listener.softKeyboardHide();
				return true;
			}
		}
		return super.dispatchKeyEventPreIme(event);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
		final int actualHeight = getHeight();
		final int proposedWidth = MeasureSpec.getSize(widthMeasureSpec);
		final int actualWidth = getWidth();
		int actualOrientation = getResources().getConfiguration().orientation;
		
		if (!rezising) {			
			if (actualOrientation != lastOrientation) {
				listener.softKeyboardHide();
			}
			if (actualWidth == proposedWidth) {
				rezising = true;
				if (!isKeyboardShown && (actualHeight - proposedHeight > 100)) {
						isKeyboardShown = true;
						listener.onSoftKeyboardShow();
				} else {
					if (isKeyboardShown && proposedHeight - actualHeight > 100) {
						isKeyboardShown = false;
						listener.onSoftKeyboardHide();
						listener.softKeyboardHide();
					}
				}
				rezising = false;
			}
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		lastOrientation = getResources().getConfiguration().orientation;
	}
	
	public void setOnSoftKeyboardVisibilityChangeListener(SoftKeyboardVisibilityChangeListener listener) {
		this.listener = listener;
	}

	// Callback
	public interface SoftKeyboardVisibilityChangeListener {
		void onSoftKeyboardShow();
		void onSoftKeyboardHide();
		void softKeyboardHide();
	}	
}
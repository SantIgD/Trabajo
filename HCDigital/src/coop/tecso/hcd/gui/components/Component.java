package coop.tecso.hcd.gui.components;

import java.util.List;

import android.content.Context;
import android.view.View;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.udaa.domain.base.AbstractEntity;

/**
 * Componente basico para la vista
 * 
 * @author tecso.coop
 *
 */
@SuppressWarnings("unused")
public abstract class Component {

	protected AbstractEntity entity;
	protected List<Component> components;
	protected Context context;

	protected View view;

	protected boolean dirty = false;
	protected boolean enabled = true;

	public AbstractEntity getEntity() {
		return entity;
	}

	public void setEntity(AbstractEntity entity) {
		this.entity = entity;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	// Metodos 
	public abstract List<Value> values();
	public abstract View build();
	public abstract View redraw();
	public abstract void addValue(Value value);
	
	public boolean validate(){
		return true;
	}

	public View getView(){
		return view;
	}

	public boolean isDirty() {
		return dirty;
	}

	public View disable() {
		if(this.enabled){
			this.enabled = false;
			this.redraw();
		}
		return view;
	}

	public void clearData() {
		
	}
	
	public View enable() {
		if(!this.enabled){
			this.enabled = true;
			this.redraw();
		}
		return view;
	}

	protected List<Integer> condCampoValorIDs; 
	
	protected List<Integer> getCondCampoValorIDs() {
		return condCampoValorIDs;
	}

	protected void setCondCampoValorIDs(List<Integer> condCampoValorIDs) {
		this.condCampoValorIDs = condCampoValorIDs;
	}

}
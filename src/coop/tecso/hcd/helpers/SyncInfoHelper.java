package coop.tecso.hcd.helpers;

import java.util.ArrayList;
import java.util.List;

public class SyncInfoHelper {
	
	private List<String> listOfElements;
	
	public SyncInfoHelper() {
		this.listOfElements = new ArrayList<>();
	}

	public void addElement(String element) {
		this.listOfElements.add(element);
	}

	public List<String> getListOfElements() {
		return listOfElements;
	}
}

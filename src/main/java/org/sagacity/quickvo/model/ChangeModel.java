package org.sagacity.quickvo.model;

/**
 * 
 * @author zhong
 *
 */
public class ChangeModel {
	private String result;

	private boolean modified = false;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	
}

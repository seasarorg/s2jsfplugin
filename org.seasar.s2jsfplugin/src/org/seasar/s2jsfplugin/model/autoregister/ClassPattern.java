package org.seasar.s2jsfplugin.model.autoregister;

/**
 * shortClassNames‚ğ‚ ‚Æ‚©‚çæ“¾‚Å‚«‚é‚æ‚¤‚É‚µ‚½ClassPattern‚ÌŠg’£ƒNƒ‰ƒXB
 * 
 * @author Naoki Takezoe
 */
public class ClassPattern extends
		org.seasar.framework.container.autoregister.ClassPattern {

	private String shortClassName;
	
	public ClassPattern() {
		super();
	}

	public ClassPattern(String packageName, String shortClassNames) {
		super(packageName, shortClassNames);
	}

	public void setShortClassNames(String shortClassName) {
		this.shortClassName = shortClassName;
		super.setShortClassNames(shortClassName);
	}
	
	public String getShortClassNames(){
		return this.shortClassName;
	}
	
	
}

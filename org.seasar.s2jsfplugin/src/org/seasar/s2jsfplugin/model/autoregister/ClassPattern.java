package org.seasar.s2jsfplugin.model.autoregister;

/**
 * shortClassNames�����Ƃ���擾�ł���悤�ɂ���ClassPattern�̊g���N���X�B
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

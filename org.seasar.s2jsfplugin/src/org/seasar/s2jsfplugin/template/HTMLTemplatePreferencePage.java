package org.seasar.s2jsfplugin.template;

import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * The preference page for HTML code completion templates.
 * 
 * @author Naoki Takezoe
 */
public class HTMLTemplatePreferencePage extends TemplatePreferencePage  implements IWorkbenchPreferencePage {

	/**
	 * Constructor.
	 */
	public HTMLTemplatePreferencePage() {
		try {
			setPreferenceStore(S2JSFPlugin.getDefault().getPreferenceStore());
			setTemplateStore(HTMLTemplateManager.getInstance().getTemplateStore());
			setContextTypeRegistry(HTMLTemplateManager.getInstance().getContextTypeRegistry());
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	protected boolean isShowFormatterSetting() {
		return false;
	}
	
	public boolean performOk() {
		boolean ok = super.performOk();
		S2JSFPlugin.getDefault().savePluginPreferences();
		return ok;
	}

}

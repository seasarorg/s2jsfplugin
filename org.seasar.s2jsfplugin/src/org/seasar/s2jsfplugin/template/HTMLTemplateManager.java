package org.seasar.s2jsfplugin.template;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * 
 * @author Naoki Takezoe
 */
public class HTMLTemplateManager {
	
	private static final String CUSTOM_TEMPLATES_KEY 
		= S2JSFPlugin.S2JSF_PLUGIN_ID + ".customtemplates";
	
	private static HTMLTemplateManager instance;
	private TemplateStore fStore;
	private ContributionContextTypeRegistry fRegistry;
	
	private HTMLTemplateManager(){
	}
	
	public static HTMLTemplateManager getInstance(){
		if(instance==null){
			instance = new HTMLTemplateManager();
		}
		return instance;
	}
	
	public TemplateStore getTemplateStore(){
		if (fStore == null){
			fStore = new ContributionTemplateStore(getContextTypeRegistry(), 
					S2JSFPlugin.getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
			try {
				fStore.load();
			} catch (IOException e){
				Util.logException(e);
			}
		}
		return fStore;
	}

	public ContextTypeRegistry getContextTypeRegistry(){
		if (fRegistry == null){
			fRegistry = new ContributionContextTypeRegistry();
			fRegistry.addContextType(HTMLContextType.CONTEXT_TYPE);
//			fRegistry.addContextType(JavaScriptContextType.CONTEXT_TYPE);
		}
		return fRegistry;
	}

	public IPreferenceStore getPreferenceStore(){
		return S2JSFPlugin.getDefault().getPreferenceStore();
	}

	public void savePluginPreferences(){
		S2JSFPlugin.getDefault().savePluginPreferences();
	}
	
}

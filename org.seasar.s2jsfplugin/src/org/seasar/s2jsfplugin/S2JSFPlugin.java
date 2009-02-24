/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.s2jsfplugin;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.seasar.s2jsfplugin.pref.ColorProvider;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Naoki Takezoe
 */
public class S2JSFPlugin extends AbstractUIPlugin {
	
	public static final String ICON_TAG      = "_icon_tag";
	public static final String ICON_ATTR     = "_icon_attribute";
	public static final String ICON_VALUE    = "_icon_value";
	public static final String ICON_CLASS    = "_icon_class";
	public static final String ICON_IF       = "_icon_interface";
	public static final String ICON_FIELD    = "_icon_field";
	public static final String ICON_METHOD   = "_icon_method";
	public static final String ICON_FILE     = "_icon_file";
	public static final String ICON_FOLDER   = "_icon_folder";
	public static final String ICON_BUTTON   = "_icon_button";
	public static final String ICON_CHECK    = "_icon_check";
	public static final String ICON_DOCTYPE  = "_icon_doctype";
	public static final String ICON_PASSWD   = "_icon_password";
	public static final String ICON_RADIO    = "_icon_radio";
	public static final String ICON_SELECT   = "_icon_select";
	public static final String ICON_TABLE    = "_icon_table";
	public static final String ICON_LINK     = "_icon_link";
	public static final String ICON_BODY     = "_icon_body";
	public static final String ICON_FORM     = "_icon_form";
	public static final String ICON_HTML     = "_icon_html";
	public static final String ICON_IMAGE    = "_icon_image";
	public static final String ICON_TITLE    = "_icon_title";
	public static final String ICON_TEXT     = "_icon_text";
	public static final String ICON_TEXTAREA = "_icon_textarea";
	public static final String ICON_COMMENT  = "_icon_comment";
	public static final String ICON_HIDDEN   = "_icon_hidden";
	public static final String ICON_TEMPLATE = "_icon_template";
	
	public static final String PREF_COLOR_TAG     = "_pref_color_tag";
	public static final String PREF_COLOR_COMMENT = "_pref_color_comment";
	public static final String PREF_COLOR_STRING  = "_pref_color_string";
	public static final String PREF_COLOR_DOCTYPE = "_pref_color_doctype";
	public static final String PREF_COLOR_SCRIPT  = "_pref_color_scriptlet";
	public static final String PREF_EDITOR_TYPE   = "_pref_editor_type";
	public static final String PREF_DTD_URI       = "_pref_dtd_uri";
	public static final String PREF_DTD_PATH      = "_pref_dtd_path";
	public static final String PREF_ASSIST_AUTO   = "_pref_assist_auto";
	public static final String PREF_ASSIST_CHARS  = "_pref_assist_chars";
	public static final String PREF_ASSIST_TIMES  = "_pref_assist_times";
	public static final String PREF_ASSIST_CLOSE  = "_pref_assist_close";
	public static final String PREF_USE_SOFTTAB   = "_pref_use_softtab";
	public static final String PREF_SOFTTAB_WIDTH = "_pref_softtab_width";
	public static final String PREF_COLOR_BG      = "AbstractTextEditor.Color.Background";
	public static final String PREF_COLOR_BG_DEF  = "AbstractTextEditor.Color.Background.SystemDefault";
	public static final String PREF_COLOR_FG      = "__pref_color_foreground";
	public static final String PREF_COLOR_JS_KEYWORD = "__pref_color_js_keyword";
	public static final String PREF_COLOR_JS_STRING  = "__pref_color_js_string";
	public static final String PREF_COLOR_JS_COMMENT = "__pref_color_js_comment";
	public static final String PREF_COLOR_CSS_SELECTOR = "__pref_color_css_selector";
	public static final String PREF_COLOR_CSS_STYLE    = "__pref_color_css_style";
	public static final String PREF_COLOR_CSS_COMMENT  = "__pref_color_css_comment";
//	public static final String PREF_COLOR_FG_DEF  = "AbstractTextEditor.Color.Foreground.SystemDefault";
	public static final String PREF_PAIR_CHAR     = "__pref_pair_character";
	
	//The shared instance.
	private static S2JSFPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;
	//Color Provider
	private ColorProvider colorProvider;
	
	public static final String MAYA_URI  = "http://www.seasar.org/maya";
	public static final String HTML_URI  = "http://java.sun.com/jsf/html";
	public static final String CORE_URI  = "http://java.sun.com/jsf/core";
	public static final String S2JSF_URI = "http://www.seasar.org/jsf";
	
	/** S2JSFプラグインのプラグインID */
	public static final String S2JSF_PLUGIN_ID = "org.seasar.s2jsfplugin";
	/** S2JSFネイチャのID */
	public static final String S2JSF_NATURE = "org.seasar.s2jsfplugin.S2JSFNature";
	/** S2JSFビルダのID */
	public static final String S2JSF_BUILDER = "org.seasar.s2jsfplugin.S2JSFBuilder";
	
	/**
	 * The constructor.
	 */
	public S2JSFPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.seasar.s2jsfplugin.S2JSFPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}
	
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(ICON_TAG,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/tag.gif")));
		reg.put(ICON_ATTR,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/attribute.gif")));
		reg.put(ICON_VALUE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/value.gif")));
		reg.put(ICON_CLASS,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/class.gif")));
		reg.put(ICON_IF,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/interface.gif")));
		reg.put(ICON_FIELD,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/field.gif")));
		reg.put(ICON_METHOD,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/method.gif")));
		reg.put(ICON_FILE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/file.gif")));
		reg.put(ICON_FOLDER,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/folder.gif")));
		reg.put(ICON_BUTTON,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/button.gif")));
		reg.put(ICON_CHECK,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/checkbox.gif")));
		reg.put(ICON_DOCTYPE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/doctype.gif")));
		reg.put(ICON_PASSWD,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/password.gif")));
		reg.put(ICON_RADIO,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/radio.gif")));
		reg.put(ICON_SELECT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/select.gif")));
		reg.put(ICON_TABLE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/table.gif")));
		reg.put(ICON_LINK,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/link.gif")));
		reg.put(ICON_BODY,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/body.gif")));
		reg.put(ICON_FORM,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/form.gif")));
		reg.put(ICON_HTML,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/html.gif")));
		reg.put(ICON_IMAGE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/image.gif")));
		reg.put(ICON_TITLE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/title.gif")));
		reg.put(ICON_TEXT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/text.gif")));
		reg.put(ICON_TEXTAREA,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/textarea.gif")));
		reg.put(ICON_COMMENT,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/comment.gif")));
		reg.put(ICON_HIDDEN,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/hidden.gif")));
		reg.put(ICON_TEMPLATE,ImageDescriptor.createFromURL(getBundle().getEntry("/icons/template.gif")));
	}
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		colorProvider = new ColorProvider(getPreferenceStore());
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public ColorProvider getColorProvider(){
		return this.colorProvider;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static S2JSFPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = S2JSFPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	/**
	 * プロジェクトのS2JSFネイチャを追加します。
	 * 
	 * @param project
	 * @throws CoreException
	 */
	public void addNature(IProject project) throws CoreException {
		if(project.hasNature(S2JSF_NATURE)){
			return;
		}
		IProjectDescription desc = project.getDescription();
		String[] natures = desc.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = S2JSF_NATURE;
		desc.setNatureIds(newNatures);
		project.setDescription(desc,null);
	}
	
	/**
	 * プロジェクトからS2JSFネイチャを削除します。
	 * 
	 * @param project
	 * @throws CoreException
	 */
	public void removeNature(IProject project) throws CoreException {
		IProjectDescription desc = project.getDescription();
		String[] natures = desc.getNatureIds();
		ArrayList list = new ArrayList();
		for(int i=0;i<natures.length;i++){
			if(!natures[i].equals(S2JSF_NATURE)){
				list.add(natures[i]);
			}
		}
		desc.setNatureIds((String[])list.toArray(new String[list.size()]));
		project.setDescription(desc,null);
	}
}

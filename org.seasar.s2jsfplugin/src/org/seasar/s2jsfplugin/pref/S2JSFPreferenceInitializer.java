/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
package org.seasar.s2jsfplugin.pref;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.RGB;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * S2JSFプラグインのプリファレンスイニシャライザ。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFPreferenceInitializer extends AbstractPreferenceInitializer {

	private RGB HTML_COMMENT = new RGB(128,   0,   0);
	private RGB PROC_INSTR   = new RGB(128, 128, 128);
	private RGB STRING       = new RGB(  0, 128,   0);
//	private RGB DEFAULT      = new RGB(  0,   0,   0);
	private RGB TAG          = new RGB(  0,   0, 128);
	private RGB SCRIPT       = new RGB(255,   0,   0);	
	private RGB FOREGROUND   = new RGB(  0,   0,   0);
	private RGB BACKGROUND   = new RGB(255, 255, 255);	
	
	public void initializeDefaultPreferences() {
		IPreferenceStore store = S2JSFPlugin.getDefault().getPreferenceStore();
		store.setDefault(S2JSFPlugin.PREF_COLOR_TAG,StringConverter.asString(TAG));
		store.setDefault(S2JSFPlugin.PREF_COLOR_COMMENT,StringConverter.asString(HTML_COMMENT));
		store.setDefault(S2JSFPlugin.PREF_COLOR_DOCTYPE,StringConverter.asString(PROC_INSTR));
		store.setDefault(S2JSFPlugin.PREF_COLOR_STRING,StringConverter.asString(STRING));
		store.setDefault(S2JSFPlugin.PREF_COLOR_SCRIPT,StringConverter.asString(SCRIPT));
		store.setDefault(S2JSFPlugin.PREF_EDITOR_TYPE,"tab");
		store.setDefault(S2JSFPlugin.PREF_DTD_URI,"");
		store.setDefault(S2JSFPlugin.PREF_DTD_PATH,"");
		store.setDefault(S2JSFPlugin.PREF_ASSIST_AUTO,true);
		store.setDefault(S2JSFPlugin.PREF_ASSIST_CHARS,"</\"");
		store.setDefault(S2JSFPlugin.PREF_ASSIST_CLOSE,true);
		store.setDefault(S2JSFPlugin.PREF_ASSIST_TIMES,0);
		store.setDefault(S2JSFPlugin.PREF_USE_SOFTTAB,false);
		store.setDefault(S2JSFPlugin.PREF_SOFTTAB_WIDTH,2);
		store.setDefault(S2JSFPlugin.PREF_COLOR_BG_DEF,true);
//		store.setDefault(S2JSFPlugin.PREF_COLOR_FG_DEF,true);
		store.setDefault(S2JSFPlugin.PREF_COLOR_BG,StringConverter.asString(BACKGROUND));
		store.setDefault(S2JSFPlugin.PREF_COLOR_FG,StringConverter.asString(FOREGROUND));
		store.setDefault(S2JSFPlugin.PREF_PAIR_CHAR, true);
	}

}

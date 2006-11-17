/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * エディタで使用する色を管理するクラス。
 * 
 * @author Naoki Takezoe
 */
public class ColorProvider {
	
	private Map colorTable = new HashMap(10);
	private Map tokenTable = new HashMap(10);	
	IPreferenceStore store;
	
	public ColorProvider(IPreferenceStore store) {
		this.store = store;
	}
	
	public IToken getToken(String prefKey){
	   Token token = (Token) tokenTable.get(prefKey);
	   if (token == null){
		  String colorName = store.getString(prefKey);
		  RGB rgb = StringConverter.asRGB(colorName);
		  token = new Token(new TextAttribute(getColor(rgb)));
		  tokenTable.put(prefKey, token);
	   }
	   return token;
	}
	
	public void dispose(){
		Iterator e = colorTable.values().iterator();
		while (e.hasNext()){
			((Color) e.next()).dispose();
		}
	}
	
	public Color getColor(String prefKey){
		  String colorName = store.getString(prefKey);
		  RGB rgb = StringConverter.asRGB(colorName);
		  return getColor(rgb);
	}
	
	private Color getColor(RGB rgb) {
		Color color = (Color) colorTable.get(rgb);
		if (color == null){
		   color = new Color(Display.getCurrent(), rgb);
		   colorTable.put(rgb, color);
		}
		return color;
	}

	public boolean affectsTextPresentation(PropertyChangeEvent event){
	   Token token = (Token) tokenTable.get(event.getProperty());
	   return (token != null);
	}

	public void handlePreferenceStoreChanged(PropertyChangeEvent event){
	   String prefKey = event.getProperty();
	   Token token = (Token) tokenTable.get(prefKey);
	   if (token != null){
		  String colorName = store.getString(prefKey);
		  RGB rgb = StringConverter.asRGB(colorName);
		  token.setData(new TextAttribute(getColor(rgb)));
	   }
	}
}

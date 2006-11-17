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
package org.seasar.s2jsfplugin.editor;

import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.swt.graphics.Image;
import org.seasar.s2jsfplugin.S2JSFPlugin;


/**
 * XMLエディタ用のアウトラインページ。
 * 
 * @author Naoki Takezoe
 * @see org.seasar.s2jsfplugin.editor.XMLEditor
 */
public class XMLOutlinePage extends HTMLOutlinePage {
	
	public XMLOutlinePage(XMLEditor editor) {
		super(editor);
	}
	
	protected Image getNodeImage(FuzzyXMLNode element){
		if(element instanceof FuzzyXMLElement){
			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TAG);
		}
		return super.getNodeImage(element);
	}
	
	protected boolean isHTML(){
		return false;
	}
	
}

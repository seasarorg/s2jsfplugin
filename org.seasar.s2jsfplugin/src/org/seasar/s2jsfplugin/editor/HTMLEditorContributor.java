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
package org.seasar.s2jsfplugin.editor;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;

/**
 * 
 * @author Naoki Takezoe
 */
public class HTMLEditorContributor extends MultiPageEditorActionBarContributor {
	
	private HTMLSourceEditorContributer contributer = new HTMLSourceEditorContributer();
	
	public HTMLEditorContributor() {
		super();
		contributer.addActionId(HTMLSourceEditor.ACTION_ESCAPE_HTML);
		contributer.addActionId(HTMLSourceEditor.ACTION_COMMENT);
	}
	
	public void init(IActionBars bars, IWorkbenchPage page) {
		super.init(bars, page);
		contributer.init(bars,page);
	}
	
	public void setActivePage(IEditorPart activeEditor) {
		if(activeEditor instanceof HTMLSourceEditor){
		} else {
		}
	}
	
	public void setActiveEditor(IEditorPart part) {
		if(part instanceof HTMLEditor){
			part = ((HTMLEditor)part).getSourceEditor();
			contributer.setActiveEditor(part);
		}
		super.setActiveEditor(part);
	}
	
	public void dispose(){
		contributer.dispose();
		super.dispose();
	}
}

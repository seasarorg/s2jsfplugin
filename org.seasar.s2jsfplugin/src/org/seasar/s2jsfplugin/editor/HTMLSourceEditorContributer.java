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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * エディタに登録されているアクションをメニューにコントリビュートするためのクラス。
 * 
 * @author Naoki Takezoe
 */
public class HTMLSourceEditorContributer extends TextEditorActionContributor {
	
	private List actionIds = new ArrayList();
	private List actions = new ArrayList();
	
	public void addActionId(String id){
		this.actionIds.add(id);
	}
	
	public void setActiveEditor(IEditorPart part) {
		super.setActiveEditor(part);
		doSetActiveEditor(part);
	}
	
	private void doSetActiveEditor(IEditorPart part) {
		ITextEditor textEditor= null;
		if (part instanceof ITextEditor){
			textEditor = (ITextEditor) part;
		}
		for(int i=0;i<this.actions.size();i++){
			RetargetTextEditorAction action = (RetargetTextEditorAction)actions.get(i);
			IAction targetAction = getAction(textEditor, (String)actionIds.get(i));
			if(targetAction!=null){
				action.setAccelerator(targetAction.getAccelerator());
				action.setAction(targetAction);
			} else {
				action.setAccelerator(SWT.NULL);
				action.setAction(null);
			}
		}
	}
	
	public void init(IActionBars bars) {
		super.init(bars);
		
		IMenuManager menuManager = bars.getMenuManager();
		IMenuManager editMenu = menuManager.findMenuUsingPath("edit");
		editMenu.insertBefore("additions", new Separator("s2jsf"));
		
		if (editMenu != null) {
			for(int i=0;i<actionIds.size();i++){
				RetargetTextEditorAction action = new RetargetTextEditorAction(S2JSFPlugin.getDefault().getResourceBundle(), null);
				this.actions.add(action);
				editMenu.appendToGroup("s2jsf",action);
			}
		}
	}

}

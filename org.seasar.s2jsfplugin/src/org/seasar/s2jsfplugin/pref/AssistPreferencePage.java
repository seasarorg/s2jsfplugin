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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * 補完機能の設定を行うプリファレンスページ
 * 
 * @author Naoki Takezoe
 */
public class AssistPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button checkCloseTag;
	private Button checkEnableAutoActivation;
	private Text   textAutoActivationChars;
	private Text   textAutoActivationDelay;
	
	public AssistPreferencePage() {
		super(S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.CodeAssist")); //$NON-NLS-1$
		setPreferenceStore(S2JSFPlugin.getDefault().getPreferenceStore());
	}
	
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2,false));
		
		checkCloseTag = new Button(composite,SWT.CHECK);
		checkCloseTag.setText(S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.AssistCloseTag"));
		checkCloseTag.setLayoutData(createGridData(2));
		
		checkEnableAutoActivation = new Button(composite,SWT.CHECK);
		checkEnableAutoActivation.setText(S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.AutoActivation"));
		checkEnableAutoActivation.setLayoutData(createGridData(2));
		checkEnableAutoActivation.addSelectionListener(
				new SelectionAdapter(){
					public void widgetSelected(SelectionEvent evt){
						if(checkEnableAutoActivation.getSelection()){
							textAutoActivationChars.setEnabled(true);
							textAutoActivationDelay.setEnabled(true);
						} else {
							textAutoActivationChars.setEnabled(false);
							textAutoActivationDelay.setEnabled(false);
						}
						setValid(doValidate());
					}
				}
		);
		
		createLabel(composite,S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.AutoActivationTrigger"));
		textAutoActivationChars = new Text(composite,SWT.BORDER);
		textAutoActivationChars.setLayoutData(createTextGridData());
		
		createLabel(composite,S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.AutoActivationDelay"));
		textAutoActivationDelay = new Text(composite,SWT.BORDER);
		textAutoActivationDelay.setLayoutData(createTextGridData());
		textAutoActivationDelay.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e){
					setValid(doValidate());
				}
		});
		
		// 初期値をセット
		performDefaults();
		
		return composite;
	}
	
	private boolean doValidate(){
		if(checkEnableAutoActivation.getSelection()){
			try {
				Integer.parseInt(textAutoActivationDelay.getText());
			} catch(Exception ex){
				setErrorMessage(Util.createMessage(
						S2JSFPlugin.getResourceString("Error.Numeric"),
						new String[]{
								S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.Message.AutoActivationDelay")
						}));
				return false;
			}
		}
		setErrorMessage(null);
		return true;
	}
	
	private GridData createTextGridData(){
		GridData gd = new GridData();
		gd.widthHint = 50;
		return gd;
	}
	
	private GridData createGridData(int span){
		GridData gd = new GridData();
		gd.horizontalSpan = span;
		return gd;
	}
	
	private void createLabel(Composite parent,String text){
		Label label = new Label(parent,SWT.NULL);
		label.setText(text);
	}
	
	/* (Javadoc なし)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	public boolean performOk() {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(S2JSFPlugin.PREF_ASSIST_CLOSE ,checkCloseTag.getSelection());
		store.setValue(S2JSFPlugin.PREF_ASSIST_AUTO  ,checkEnableAutoActivation.getSelection());
		store.setValue(S2JSFPlugin.PREF_ASSIST_CHARS ,textAutoActivationChars.getText());
		store.setValue(S2JSFPlugin.PREF_ASSIST_TIMES ,textAutoActivationDelay.getText());
		return true;
	}
	
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		checkCloseTag.setSelection(store.getBoolean(S2JSFPlugin.PREF_ASSIST_CLOSE));
		checkEnableAutoActivation.setSelection(store.getBoolean(S2JSFPlugin.PREF_ASSIST_AUTO));
		textAutoActivationChars.setText(store.getString(S2JSFPlugin.PREF_ASSIST_CHARS));
		textAutoActivationDelay.setText(store.getString(S2JSFPlugin.PREF_ASSIST_TIMES));
		
		if(checkEnableAutoActivation.getSelection()){
			textAutoActivationChars.setEnabled(true);
			textAutoActivationDelay.setEnabled(true);
		} else {
			textAutoActivationChars.setEnabled(false);
			textAutoActivationDelay.setEnabled(false);
		}
	}

}

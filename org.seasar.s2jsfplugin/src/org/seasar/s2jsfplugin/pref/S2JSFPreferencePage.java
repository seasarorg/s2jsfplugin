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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * S2JSFプラグインのプリファレンスページ。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	private ColorFieldEditor colorForeground;
	private SystemColorFieldEditor colorBackground;
	private ColorFieldEditor colorTag;
	private ColorFieldEditor colorComment;
	private ColorFieldEditor colorDoctype;
	private ColorFieldEditor colorString;
	private ColorFieldEditor colorScriptlet;
//	private ColorFieldEditor colorCssProperty;
	private UseSoftTabFieldEditor useSoftTab;
	private SoftTabWidthFieldEditor softTabWidth;
	private RadioGroupFieldEditor editorType;
	private BooleanFieldEditor highlightPair;
	
	public S2JSFPreferencePage() {
		super(S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.CodeAssist"),GRID); //$NON-NLS-1$
		setPreferenceStore(S2JSFPlugin.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}
	
	protected void createFieldEditors() {
		Composite parent = getFieldEditorParent();
		
		colorForeground = new ColorFieldEditor(S2JSFPlugin.PREF_COLOR_FG,
				S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.ForegroundColor"),
				parent); //$NON-NLS-1$
		addField(colorForeground);
		
		colorBackground = new SystemColorFieldEditor(S2JSFPlugin.PREF_COLOR_BG,S2JSFPlugin.PREF_COLOR_BG_DEF,
				S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.BackgroundColor"),
				parent); //$NON-NLS-1$
		addField(colorBackground);
		
		colorTag = new ColorFieldEditor(S2JSFPlugin.PREF_COLOR_TAG,
					S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.TagColor"),
					parent); //$NON-NLS-1$
		addField(colorTag);
		
		colorComment = new ColorFieldEditor(S2JSFPlugin.PREF_COLOR_COMMENT,
					S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.CommentColor"),
					parent); //$NON-NLS-1$
		addField(colorComment);
		
		colorDoctype = new ColorFieldEditor(S2JSFPlugin.PREF_COLOR_DOCTYPE,
					S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.DocTypeColor"),
					parent); //$NON-NLS-1$
		addField(colorDoctype);
		
		colorString = new ColorFieldEditor(S2JSFPlugin.PREF_COLOR_STRING,
					S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.StringColor"),
					parent); //$NON-NLS-1$
		addField(colorString);
		
		colorScriptlet = new ColorFieldEditor(S2JSFPlugin.PREF_COLOR_SCRIPT,
					S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.ScriptColor"),
					parent); //$NON-NLS-1$
		addField(colorScriptlet);
		
		highlightPair = new BooleanFieldEditor(S2JSFPlugin.PREF_PAIR_CHAR,
				S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.PairCharacter"), parent);
		addField(highlightPair);
		
		useSoftTab = new UseSoftTabFieldEditor(S2JSFPlugin.PREF_USE_SOFTTAB,
				S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.UseSoftTab"),
				parent);
		addField(useSoftTab);
	
		softTabWidth = new SoftTabWidthFieldEditor(S2JSFPlugin.PREF_SOFTTAB_WIDTH,
				S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.SoftTabWidth"),
				parent,4);
		softTabWidth.setEnabled(getPreferenceStore().getBoolean(S2JSFPlugin.PREF_USE_SOFTTAB),parent);
		addField(softTabWidth);
		
		editorType =new RadioGroupFieldEditor(S2JSFPlugin.PREF_EDITOR_TYPE,
					S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.EditorType"),1,
					new String[][]{
						{S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.EditorTab"),"tab"},
						{S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.EditorSplitHor"),"horizontal"},
						{S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.EditorSplitVer"),"vertical"},
						{S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.EditorNoPreview"),"noPreview"}
					},parent,true);
		addField(editorType);
	}
	
	/** 「背景色」のフィールドエディタ 。 */
	private class SystemColorFieldEditor extends ColorFieldEditor {
		
		private String booleanName = null;
		private Button colorButton;
		private Button checkbox;
		
		public SystemColorFieldEditor(String colorName, String booleanName, String labelText, Composite parent){
			super(colorName,labelText,parent);
			this.booleanName = booleanName;
		}
		
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			Control control = getLabelControl(parent);
			GridData gd = new GridData();
			gd.horizontalSpan = numColumns - 1;
			control.setLayoutData(gd);
			
			Composite composite = new Composite(parent,SWT.NULL);
			GridLayout layout = new GridLayout(2,false);
			layout.horizontalSpacing = 5;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			composite.setLayout(layout);
			
			colorButton = getChangeControl(composite);
			gd = new GridData();
//			gd.heightHint = convertVerticalDLUsToPixels(colorButton, IDialogConstants.BUTTON_HEIGHT);
			int widthHint = convertHorizontalDLUsToPixels(colorButton, IDialogConstants.BUTTON_WIDTH);
			gd.widthHint = Math.max(widthHint, colorButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
			colorButton.setLayoutData(gd);
			
			checkbox = new Button(composite,SWT.CHECK);
			checkbox.setText(S2JSFPlugin.getResourceString("HTMLEditorPreferencePage.SystemDefault"));
			checkbox.addSelectionListener(new SelectionAdapter(){
				public void widgetSelected(SelectionEvent evt){
					colorButton.setEnabled(!checkbox.getSelection());
				}
			});
		}
		
		protected void doLoad() {
			super.doLoad();
			checkbox.setSelection(getPreferenceStore().getBoolean(booleanName));
			colorButton.setEnabled(!checkbox.getSelection());
		}
		
		protected void doLoadDefault() {
			super.doLoadDefault();
			checkbox.setSelection(getPreferenceStore().getDefaultBoolean(booleanName));
			colorButton.setEnabled(!checkbox.getSelection());
		}

		protected void doStore() {
			super.doStore();
			getPreferenceStore().setValue(booleanName,checkbox.getSelection());
		}
	}
	
	/** 「ソフトタブを使用する」のフィールドエディタ。  */
	private class UseSoftTabFieldEditor extends BooleanFieldEditor {
		
		private Composite parent;
		
		public UseSoftTabFieldEditor(String name, String label, Composite parent) {
			super(name, label, parent);
			this.parent = parent;
		}
		
		protected void valueChanged(boolean oldValue, boolean newValue) {
			super.valueChanged(oldValue, newValue);
			softTabWidth.setEnabled(newValue,parent);
		}
	}
	
	/** 「ソフトタブの幅」のフィールドエディタ。 */
	private class SoftTabWidthFieldEditor extends IntegerFieldEditor {
		public SoftTabWidthFieldEditor(String name, String labelText,
				Composite parent, int textLimit) {
			super(name, labelText, parent, textLimit);
		}
		
		
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			super.doFillIntoGrid(parent, numColumns);
			GridData gd = (GridData)getTextControl().getLayoutData();
			gd.horizontalAlignment = 0;
			gd.widthHint = 40;
		}
	}
}

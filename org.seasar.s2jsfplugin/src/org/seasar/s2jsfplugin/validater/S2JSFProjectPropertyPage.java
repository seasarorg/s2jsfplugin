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
package org.seasar.s2jsfplugin.validater;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * S2JSFのプロジェクトプロパティページ。
 * ネイチャの追加および、プロジェクト固有の設定を行います。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private Button s2jsfProject;
	
	public S2JSFProjectPropertyPage() {
		super();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		
		s2jsfProject = new Button(composite,SWT.CHECK);
		s2jsfProject.setText(S2JSFPlugin.getResourceString("ProjectProperty.Validation"));
		
		try {
			s2jsfProject.setSelection(((IProject)getElement()).hasNature(S2JSFPlugin.S2JSF_NATURE));
		} catch(Exception ex){
			Util.logException(ex);
		}
		return composite;
	}

	public boolean performOk() {
		try {
			if(s2jsfProject.getSelection()){
				S2JSFPlugin.getDefault().addNature((IProject)getElement());
			} else {
				S2JSFPlugin.getDefault().removeNature((IProject)getElement());
			}
		} catch(Exception ex){
			Util.logException(ex);
			return false;
		}
		return true;
	}
}

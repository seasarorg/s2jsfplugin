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

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.FolderSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * S2JSFのプロジェクトプロパティページ。
 * ネイチャの追加および、プロジェクト固有の設定を行います。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
	
	private S2JSFProjectParams params;
	private Button s2jsfProject;
	private Button validateCloseTag;
	private Text root;
	private Text extensions;
	
	public S2JSFProjectPropertyPage() {
		super();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		
		s2jsfProject = new Button(composite,SWT.CHECK);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		s2jsfProject.setLayoutData(gd);
		s2jsfProject.setText(S2JSFPlugin.getResourceString("ProjectProperty.Validation"));
		
		validateCloseTag = new Button(composite,SWT.CHECK);
		gd = new GridData();
		gd.horizontalSpan = 3;
		validateCloseTag.setLayoutData(gd);
		validateCloseTag.setText(S2JSFPlugin.getResourceString("ProjectProperty.ValidationCloseTag"));
		
		Label label = new Label(composite,SWT.NULL);
		label.setText(S2JSFPlugin.getResourceString("ProjectProperty.Root"));
		root = new Text(composite,SWT.BORDER);
		root.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Button button = new Button(composite, SWT.BUTTON1);
		button.setText("...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectFolder();
			}
		});
		
		label = new Label(composite,SWT.NULL);
		label.setText(S2JSFPlugin.getResourceString("ProjectProperty.Extensions"));
		extensions = new Text(composite,SWT.BORDER);
		extensions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		try {
			s2jsfProject.setSelection(getProject().hasNature(S2JSFPlugin.S2JSF_NATURE));
			params = new S2JSFProjectParams(getProject());
			validateCloseTag.setSelection(params.getValidateCloseTag());
			if(params.getRoot()==null || params.getRoot().equals("")){
				root.setText("/");
			} else {
				root.setText(params.getRoot());
			}
			extensions.setText(params.getExtensions());
		} catch(Exception ex){
			Util.logException(ex);
		}
		return composite;
	}
	
	public boolean performOk() {
		try {
			// 設定の保存
			IProject project = getProject();
			
			if(s2jsfProject.getSelection()){
				S2JSFPlugin.getDefault().addNature(project);
			} else {
				S2JSFPlugin.getDefault().removeNature(project);
			}
			params.setValidateCloseTag(validateCloseTag.getSelection());
			params.setRoot(root.getText());
			params.setExtensions(extensions.getText());
			params.save(project);
			
		} catch(Exception ex){
			Util.logException(ex);
			return false;
		}
		return true;
	}
	
	private void selectFolder() {
		try {
			IProject currProject = getProject();
			IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
			IResource init = null;
			if (params.getRoot() != null) {
				init = wsroot.findMember(currProject.getName() + params.getRoot());
			}
			Class[] acceptedClasses = new Class[] { IProject.class, IFolder.class };
			ISelectionStatusValidator validator = new TypedElementSelectionValidator(acceptedClasses, false);
			IProject[] allProjects = wsroot.getProjects();
			ArrayList rejectedElements = new ArrayList(allProjects.length);
			for (int i = 0; i < allProjects.length; i++) {
				if (!allProjects[i].equals(currProject)) {
					rejectedElements.add(allProjects[i]);
				}
			}
			ViewerFilter filter = new TypedViewerFilter(acceptedClasses, rejectedElements.toArray());
			
			ElementTreeSelectionDialog dialog = new FolderSelectionDialog(
					getShell(),
					new WorkbenchLabelProvider(), 
					new WorkbenchContentProvider());
			
			dialog.setTitle(S2JSFPlugin.getResourceString("ProjectProperty.WebRoot"));
			dialog.setMessage(S2JSFPlugin.getResourceString("ProjectProperty.WebRoot"));
			
			dialog.setInput(wsroot);
			dialog.setValidator(validator);
			dialog.addFilter(filter);
			dialog.setInitialSelection(init);
			if (dialog.open() == Dialog.OK) {
				root.setText(getFolderName(dialog.getFirstResult()));
			}
			
		} catch (Throwable t) {
			
		}
	}
	
	private IProject getProject(){
		return (IProject)getElement(); //.getAdapter(IProject.class);
	}
	
	private String getFolderName(Object result) throws CoreException {
		if (result instanceof IFolder) {
			IFolder folder = (IFolder) result;
			String folderName = folder.getLocation().toString();
			String projectPath = getProject().getLocation().toString();
			if (folderName.length() <= projectPath.length()) {
				return folderName;
			} else {
				return folderName.substring(projectPath.length());
			}
		}
		return "/";
	}
}

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
package org.seasar.s2jsfplugin.wizard;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * @author Naoki Takezoe
 */
public class HTMLWizardPage extends WizardPage {
	
	private Text containerText;
	private Text fileText;
	private ISelection selection;
	private String fileName;
	private ResourceBundle resource = S2JSFPlugin.getDefault().getResourceBundle();

	/**
	 * Constructor for SampleNewWizardPage.
	 * @param pageName
	 */
	public HTMLWizardPage(ISelection selection,String fileName) {
		super("wizardPage");
		setTitle(resource.getString("HTMLWizard.Title"));
		setDescription(resource.getString("HTMLWizard.Description"));
		this.selection = selection;
		this.fileName  = fileName;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(resource.getString("Label.Container"));

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText(resource.getString("Button.Browse"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText(resource.getString("Label.FileName"));

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		initialize();
		dialogChanged();
		setControl(container);
	}
	
	/**
	 * Tests if the current workbench selection is a suitable
	 * container to use.
	 */
	private void initialize() {
		if (selection!=null && selection.isEmpty()==false && selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection)selection;
			if (ssel.size()>1) return;
			Object obj = ssel.getFirstElement();
			IContainer container = null;
			IResource  resource  = null;
			if(obj instanceof IResource){
				resource = (IResource)obj;
			} else if(obj instanceof IAdaptable){
				resource = (IResource)((IAdaptable)obj).getAdapter(IResource.class);
			}
			if(resource!=null){
				if(resource instanceof IContainer){
					container = (IContainer)resource;
				} else {
					container = resource.getParent();
				}
			}
			if(container!=null){
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText(fileName);
	}
	
	/**
	 * Uses the standard container selection dialog to
	 * choose the new value for the container field.
	 */
	private void handleBrowse() {
		ContainerSelectionDialog dialog =
			new ContainerSelectionDialog(
				getShell(),
				ResourcesPlugin.getWorkspace().getRoot(),
				false,
				resource.getString("Message.SelectContainer"));
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path)result[0]).toString());
			}
		}
	}
	
	/**
	 * Ensures that both text fields are set.
	 */
	private void dialogChanged() {
		String container = getContainerName();
		String fileName = getFileName();

		if (container.length() == 0) {
			updateStatus(Util.createMessage(resource.getString("Error.Required"),
					new String[]{resource.getString("Message.Container")}));
			return;
		}
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(container);
		if (resource==null || !resource.exists() || !(resource instanceof IContainer) || resource instanceof IWorkspaceRoot) {
			updateStatus(Util.createMessage(
					this.resource.getString("Error.NotExists"),
					new String[]{container}));
			return;
		}
		
		if (fileName.length() == 0) {
			updateStatus(Util.createMessage(this.resource.getString("Error.Required"),
					new String[]{this.resource.getString("Message.FileName")}));
			return;
		}
		
		if (fileName.endsWith(".html") == false) {
			updateStatus(Util.createMessage(this.resource.getString("Error.Extention"),
					new String[]{"html"}));
			return;
		}
		
		IFile file = ((IContainer)resource).getFile(new Path(fileName));
		if(file.exists()){
			updateStatus(Util.createMessage(this.resource.getString("Error.AlreadyExists"),
					new String[]{file.getFullPath().toString()}));
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setMessage(message, DialogPage.ERROR);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}
	public String getFileName() {
		return fileText.getText();
	}
}
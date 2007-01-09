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
package org.seasar.s2jsfplugin.wizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * S2JSF�p��HTML�t�@�C�����쐬����E�B�U�[�h�B
 * 
 * @author Naoki Takezoe
 */
public class HTMLWizard extends Wizard implements INewWizard {

	private HTMLWizardPage page;
	private ISelection selection;
	private String fileName = "newfile.html";
	private ResourceBundle resource = S2JSFPlugin.getDefault().getResourceBundle();

	/**
	 * Constructor for SampleNewWizard.
	 */
	public HTMLWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle(resource.getString("HTMLWizard.Title"));
	}
	
	/**
	 * �f�t�H���g�ŕ\������t�@�C������ݒ肵�܂��B
	 */
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	public void addPages() {
		page = new HTMLWizardPage(selection,fileName);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
//			Throwable realException = e.getTargetException();
			//Util.openErrorDialog(realException);
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */
	private void doFinish(String containerName,String fileName,IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask(Util.createMessage(
				this.resource.getString("Message.Creation"),new String[]{fileName}), 2);
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		IContainer container = (IContainer) resource;
		
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName(this.resource.getString("Message.OpenFile"));
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream() {
		StringBuffer sb = new StringBuffer();
		// HTML�e���v���[�g
		sb.append("<html xmlns:m=\"" + S2JSFPlugin.MAYA_URI + "\">\n");
		sb.append("\t<head>\n");
		sb.append("\t\t<title></title>\n");
		sb.append("\t</head>\n");
		sb.append("\t<body>\n");
		sb.append("\t</body>\n");
		sb.append("</html>\n");
		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}


}

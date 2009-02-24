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
package org.seasar.s2jsfplugin.editor;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * �^�u�`����HTML�G�f�B�^�����B
 * 
 * @author Naoki Takezoe
 */
public class HTMLMultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener,HTMLEditorPart {

	/** HTML�̃\�[�X�G�f�B�^ */
	private HTMLSourceEditor editor;
	/** �v���r���[�p��Browser�E�B�W�F�b�g */
	private Browser browser;
	/** ���b�p�[ */
	private HTMLEditor wrapper;
	
	public HTMLMultiPageEditor(HTMLEditor wrapper,HTMLSourceEditor editor) {
		super();
		this.wrapper = wrapper;
		this.editor  = editor;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public Browser getBrowser() {
		return browser;
	}
	
	public HTMLSourceEditor getSourceEditor() {
		return editor;
	}
	
	/** �P�Ԗڂ̃y�[�W�i�\�[�X�G�f�B�^�j���쐬���܂��B */
	private void createPage0() {
		try {
			int index = addPage(editor, getEditorInput());
			setPageText(index, S2JSFPlugin.getResourceString("MultiPageHTMLEditor.Source")); //$NON-NLS-1$
			setPartName(getEditorInput().getName());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(),
				"Error creating nested text editor",null,e.getStatus()); //$NON-NLS-1$
		}
	}
	
	/** �Q�Ԗڂ̃y�[�W�i�v���r���[�j���쐬���܂��B */
	private void createPage1() {
		if(!isFileEditorInput()){
			return;
		}
		browser = new Browser(getContainer(),SWT.NONE);
		int index = addPage(browser);
		setPageText(index, S2JSFPlugin.getResourceString("MultiPageHTMLEditor.Preview")); //$NON-NLS-1$
	}

	protected IEditorSite createSite(IEditorPart editor) {
		return new SourceEditorSite(this,editor,getEditorSite());
	}
	
	/** �y�[�W���쐬���܂��B */
	protected void createPages() {
		createPage0();
		createPage1();
	}
	
	public void dispose() {
		// �e���|�����t�@�C������������폜����
		if(isFileEditorInput()){
			File tmpFile = editor.getTempFile();
			if(tmpFile.exists()){
				tmpFile.delete();
			}
		}
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	/** �ҏW���̃t�@�C����ۑ����܂��B�\�[�X�G�f�B�^��doSave���\�b�h���Ăяo���܂��B */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	
	/** �ҏW���̃t�@�C����ۑ����܂��B�\�[�X�G�f�B�^��doSaveAs���\�b�h���Ăяo���܂��B */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setInput(editor.getEditorInput());
		setPartName(getEditorInput().getName());
	}
	
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		super.init(site, editorInput);
	}
	
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if(newPageIndex==1){
		    wrapper.updatePreview();
		}
	}
	
	/** �\�[�X�G�f�B�^�ɐ؂�ւ��A�L�����b�g���w��ʒu�Ɉړ����܂��B */
	public void setOffset(int offset){
		setActivePage(0);
		editor.selectAndReveal(offset,0);
	}
	
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}
	
	public Object getAdapter(Class adapter) {
		return editor.getAdapter(adapter);
	}
	
	protected void firePropertyChange(int propertyId) {
		super.firePropertyChange(propertyId);
		wrapper.firePropertyChange2(propertyId);
	}
	
	public boolean isFileEditorInput(){
		return editor.isFileEditorInput();
	}
	
	/** �\�[�X�G�f�B�^�p��IEditorSite */
	private class SourceEditorSite extends MultiPageEditorSite {
		
		private HTMLSourceEditor editor = null;
		private IEditorSite site;
		private ArrayList menuExtenders;
		
		public SourceEditorSite(MultiPageEditorPart multiPageEditor,IEditorPart editor,IEditorSite site) {
			super(multiPageEditor, editor);
			this.site = site;
			this.editor = (HTMLSourceEditor)editor;
		}
		
		public IEditorActionBarContributor getActionBarContributor() {
			return site.getActionBarContributor();
		}
		
		public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
			if(editor != null){
				if (menuExtenders == null) {
					menuExtenders = new ArrayList(1);
				}
				menuExtenders.add(new PopupMenuExtender(menuId, menuManager, selectionProvider, editor));
			}
		}
		
		public void dispose(){
			if (menuExtenders != null) {
				for (int i = 0; i < menuExtenders.size(); i++) {
					((PopupMenuExtender)menuExtenders.get(i)).dispose();
				}
				menuExtenders = null;
			}
			super.dispose();
		}
	}
}

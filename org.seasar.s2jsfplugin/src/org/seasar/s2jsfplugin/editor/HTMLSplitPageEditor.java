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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.PopupMenuExtender;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.seasar.s2jsfplugin.Util;

/**
 * 上下 or 左右二分割のスプリット型HTMLエディタの実装。
 * 
 * @author Naoki Takezoe
 */
public class HTMLSplitPageEditor extends EditorPart implements IResourceChangeListener,HTMLEditorPart {
	
	/** HTMLのソースエディタ */
	private HTMLSourceEditor editor;
	/** プレビュー用のBrowserウィジェット */
	private Browser browser;
	/** ラッパー */
	private HTMLEditor wrapper;
	/** 水平分割かどうか */
	private boolean isHorizontal;
	/** IEditorSite */
	private SplitEditorSite site;
	
	public HTMLSplitPageEditor(HTMLEditor wrapper,boolean isHorizontal,HTMLSourceEditor editor) {
		super();
		this.wrapper = wrapper;
		this.isHorizontal = isHorizontal;
		this.editor = editor;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public Browser getBrowser() {
		return browser;
	}
	
	public HTMLSourceEditor getSourceEditor() {
		return editor;
	}
	
	/** 編集中のファイルを保存します。ソースエディタのdoSaveメソッドを呼び出します。 */
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
		wrapper.updatePreview();
	}

	/** 編集中のファイルを保存します。ソースエディタのdoSaveAsメソッドを呼び出します。 */
	public void doSaveAs() {
		editor.doSaveAs();
		setInput(editor.getEditorInput());
		setPartName(getEditorInput().getName());
		wrapper.updatePreview();
	}

	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		setSite(site);
		setInput(editorInput);
		setPartName(editorInput.getName());
	}

	public boolean isDirty() {
		if(editor!=null){
			return editor.isDirty();
		}
		return false;
	}

	public boolean isSaveAsAllowed() {
		return true;
	}
	
	public void dispose() {		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		site.dispose();
		super.dispose();
	}
	
	public void createPartControl(Composite parent) {
		try {
			// Don't split when EditorInput isn't IFileEditorInput
			if(!(getEditorInput() instanceof IFileEditorInput)){
				editor.init(getEditorSite(), getEditorInput());
				editor.addPropertyListener(new IPropertyListener() {
					public void propertyChanged(Object source, int propertyId) {
						firePropertyChange(propertyId);
					}
				});
				editor.createPartControl(parent);
				return;
			}
			
			SashForm sash = null;
			if(isHorizontal){
				sash = new SashForm(parent,SWT.VERTICAL);
			} else {
				sash = new SashForm(parent,SWT.HORIZONTAL);
			}
			site = new SplitEditorSite(editor, getEditorSite());
			editor.init(site ,getEditorInput());
			editor.addPropertyListener(new IPropertyListener() {
				public void propertyChanged(Object source, int propertyId) {
					firePropertyChange(propertyId);
				}
			});
			editor.createPartControl(sash);
			browser = new Browser(sash,SWT.NONE);
			wrapper.updatePreview();
		} catch (PartInitException e) {
			Util.logException(e);
			ErrorDialog.openError(getSite().getShell(),
				"Error creating nested text editor",null,e.getStatus()); //$NON-NLS-1$
		}
	}

	public void setFocus() {
		editor.setFocus();
	}
	
	public void gotoMarker(IMarker marker) {
		IDE.gotoMarker(editor, marker);
	}
	
	public void setOffset(int offset){
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
	
	/**
	 * 分割エディタ用のIEditorSite実装。
	 */
	private static class SplitEditorSite implements IEditorSite {
		
		private HTMLSourceEditor editor;
		private IEditorSite site;
		private ArrayList menuExtenders;

		
		public SplitEditorSite(HTMLSourceEditor editor, IEditorSite site){
			this.editor = editor;
			this.site = site;
		}
		
		public IEditorActionBarContributor getActionBarContributor() {
			return site.getActionBarContributor();
		}
		
		public IActionBars getActionBars() {
			return site.getActionBars();
		}
		
		public String getId() {
			return site.getId();
		}
		
		public IKeyBindingService getKeyBindingService() {
			return site.getKeyBindingService();
		}
		
		public String getPluginId() {
			return site.getPluginId();
		}
		
		public String getRegisteredName() {
			return site.getRegisteredName();
		}
		
		public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider) {
			site.registerContextMenu(menuManager, selectionProvider);
		}
		
		public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider) {
			if (menuExtenders == null) {
				menuExtenders = new ArrayList(1);
			}
			menuExtenders.add(new PopupMenuExtender(menuId, menuManager, selectionProvider, editor));
		}

		public IWorkbenchPage getPage() {
			return site.getPage();
		}
		
		public ISelectionProvider getSelectionProvider() {
			return site.getSelectionProvider();
		}
		
		public Shell getShell() {
			return site.getShell();
		}
		
		public IWorkbenchWindow getWorkbenchWindow() {
			return site.getWorkbenchWindow();
		}
		
		public void setSelectionProvider(ISelectionProvider provider) {
			site.setSelectionProvider(provider);
		}
		
		public Object getAdapter(Class adapter) {
			return site.getAdapter(adapter);
		}
		
		public void dispose() {
			if (menuExtenders != null) {
				for (int i = 0; i < menuExtenders.size(); i++) {
					((PopupMenuExtender)menuExtenders.get(i)).dispose();
				}
				menuExtenders = null;
			}
		}
		
		// for Eclipse 3.1
		
		public IWorkbenchPart getPart() {
			return editor;
		}

		public void registerContextMenu(MenuManager menuManager, ISelectionProvider selectionProvider, boolean includeEditorInput) {
			this.registerContextMenu(menuManager, selectionProvider);
		}

		public void registerContextMenu(String menuId, MenuManager menuManager, ISelectionProvider selectionProvider, boolean includeEditorInput) {
			this.registerContextMenu(menuId, menuManager, selectionProvider);
		}
		
		// for Eclipse 3.2
		
		public Object getService(Class api) {
			return null;
		}

		public boolean hasService(Class api) {
			return false;
		}
	}
}

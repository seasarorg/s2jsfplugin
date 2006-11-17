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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;

/**
 * HTMLエディタ。
 * 
 * @author Naoki Takezoe
 */
public class HTMLEditor extends EditorPart {

	protected EditorPart editor;
	protected File prevTempFile = null;
	
	public HTMLEditor() {
		super();
		IPreferenceStore store = S2JSFPlugin.getDefault().getPreferenceStore();
		String type = store.getString(S2JSFPlugin.PREF_EDITOR_TYPE);
		if(type.equals("horizontal")){
			editor = new HTMLSplitPageEditor(this,true,createHTMLSourceEditor(getSourceViewerConfiguration()));
		} else if(type.equals("vertical")){
			editor = new HTMLSplitPageEditor(this,false,createHTMLSourceEditor(getSourceViewerConfiguration()));
		} else if(type.equals("tab")){
			editor = new HTMLMultiPageEditor(this,createHTMLSourceEditor(getSourceViewerConfiguration()));
		} else {
			editor = createHTMLSourceEditor(getSourceViewerConfiguration());
			editor.addPropertyListener(new IPropertyListener() {
				public void propertyChanged(Object source, int propertyId) {
					firePropertyChange(propertyId);
				}
			});
		}
	}
	
	protected SourceViewerConfiguration getSourceViewerConfiguration(){
	    HTMLConfiguration config = new HTMLConfiguration(S2JSFPlugin.getDefault().getColorProvider());
	    config.setEditorPart(this);
	    return config;
	}
	
	public HTMLSourceEditor getSourceEditor(){
		if(editor instanceof HTMLSourceEditor){
			return (HTMLSourceEditor)editor;
		} else {
			return ((HTMLEditorPart)editor).getSourceEditor();
		}
	}
	
	protected HTMLSourceEditor createHTMLSourceEditor(SourceViewerConfiguration config){
		return new HTMLSourceEditor(getSourceViewerConfiguration());
	}
	
	/**
	 * プレビューを更新します。
	 */
	public void updatePreview(){
	    try {
	    	// IFileEditorInputでない場合はプレビュー不可
	    	if(!((HTMLEditorPart)editor).isFileEditorInput()){
	    		return;
	    	}
	    	if(editor instanceof HTMLSourceEditor){
	    		return;
	    	}
	    	
			// テンポラリファイルに書き出し
		    HTMLEditorPart editor = (HTMLEditorPart)this.editor;
		    IFileEditorInput input = (IFileEditorInput)this.editor.getEditorInput();
		    String charset = input.getFile().getCharset();
			String html    = editor.getSourceEditor().getDocumentProvider().getDocument(input).get();
			// JSP部分を置換
			//html = HTMLUtil.convertJSP(html);
			
			File tmpFile = editor.getSourceEditor().getTempFile();
			FileOutputStream out = new FileOutputStream(tmpFile);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, charset), true); 
			pw.write(html);
			pw.close();
			
			if(prevTempFile!=null && prevTempFile.equals(tmpFile)){
				editor.getBrowser().refresh();
			} else {
				if(prevTempFile!=null){
					prevTempFile.delete();
				}
				prevTempFile = tmpFile;
				editor.getBrowser().setUrl("file://" + tmpFile.getAbsolutePath()); //$NON-NLS-1$
			}
	    } catch(Exception ex){
	    	Util.logException(ex);
	    }
	}
	
	public void createPartControl(Composite parent) {
		editor.createPartControl(parent);
	}
	public void dispose() {
		editor.dispose();
	}
	public void doSave(IProgressMonitor monitor) {
		editor.doSave(monitor);
	}
	public void doSaveAs() {
		editor.doSaveAs();
	}
//	public boolean equals(Object arg0) {
//		return editor.equals(arg0);
//	}
	public Object getAdapter(Class adapter) {
		return editor.getAdapter(adapter);
	}
	public String getContentDescription() {
		return editor.getContentDescription();
	}
	public IEditorInput getEditorInput() {
		return editor.getEditorInput();
	}
	public IEditorSite getEditorSite() {
		return editor.getEditorSite();
	}
	public String getPartName() {
		return editor.getPartName();
	}
	public IWorkbenchPartSite getSite() {
		return editor.getSite();
	}
	public String getTitle() {
		return editor.getTitle();
	}
	public Image getTitleImage() {
		return editor.getTitleImage();
	}
	public String getTitleToolTip() {
		return editor.getTitleToolTip();
	}
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		editor.init(site, input);
	}
	public boolean isDirty() {
		return editor.isDirty();
	}
	public boolean isSaveAsAllowed() {
		return editor.isSaveAsAllowed();
	}
	public boolean isSaveOnCloseNeeded() {
		return editor.isSaveOnCloseNeeded();
	}
	public void setFocus() {
		editor.setFocus();
	}
	public void setInitializationData(IConfigurationElement config,String propertyName, Object data){
		editor.setInitializationData(config, propertyName, data);
	}
	public void showBusy(boolean busy) {
		editor.showBusy(busy);
	}
	/** ソースエディタに切り替え、キャレットを指定位置に移動します。 */
	public void setOffset(int offset){
		if(editor instanceof HTMLSplitPageEditor){
			((HTMLSplitPageEditor)editor).setOffset(offset);
		} else if(editor instanceof HTMLMultiPageEditor){
			((HTMLMultiPageEditor)editor).setOffset(offset);
		} else if(editor instanceof HTMLSourceEditor){
			((HTMLSourceEditor)editor).selectAndReveal(offset, 0);
		}
	}
	public void firePropertyChange2(int propertyId) {
		super.firePropertyChange(propertyId);
	}

}

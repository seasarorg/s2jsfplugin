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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.internal.Entities;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.assist.XMLAssistProcessor;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * faces-config.xml用のエディタ。
 * 
 * @author Naoki Takezoe
 */
public class XMLEditor extends HTMLSourceEditor {
	
	private XMLOutlinePage outline;
	public static final String ACTION_ESCAPE_XML = "_escape_xml";
	
	public XMLEditor() {
		super(new XMLConfiguration(S2JSFPlugin.getDefault().getColorProvider()));
		XMLConfiguration config = (XMLConfiguration)getSourceViewerConfiguration();
		config.setEditorPart(this);
		outline = new XMLOutlinePage(this);
		
		setAction(ACTION_ESCAPE_XML,new EscapeXMLAction());
	}
	
	protected HTMLHyperlinkProvider createHyperlinkProvider(){
		return new XMLHyperlinkProvider(this);
	}
	
	protected void update() {
		super.update();
		outline.update();
	}
	
	protected void addContextMenuActions(IMenuManager menu){
		menu.add(new Separator(GROUP_HTML));
		addAction(menu,GROUP_HTML,ACTION_ESCAPE_XML);
		addAction(menu,GROUP_HTML,ACTION_COMMENT);
	}
	
	protected void updateSelectionDependentActions() {
		super.updateSelectionDependentActions();
		ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
		if(sel.getText().equals("")){
			getAction(ACTION_ESCAPE_XML).setEnabled(false);
		} else {
			getAction(ACTION_ESCAPE_XML).setEnabled(true);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// ドラッグ＆ドロップ
	////////////////////////////////////////////////////////////////////////////
	
	/** ドラッグ＆ドロップの初期化 */
	protected void initializeDragAndDrop(ISourceViewer viewer) {
		super.initializeDragAndDrop(viewer);
		
		DropTarget target = new DropTarget(viewer.getTextWidget(),DND.DROP_DEFAULT|DND.DROP_COPY);
		LocalSelectionTransfer selTransfer = LocalSelectionTransfer.getInstance();
		Transfer[] types = new Transfer[]{selTransfer};
		target.setTransfer(types);
		target.addDropListener(new NavigationDropListener());
	}
	
	/** ドロップ時の処理を行うリスナ */
	private class NavigationDropListener extends DropTargetAdapter {
		
		public void dragEnter(DropTargetEvent evt){
			evt.detail = DND.DROP_COPY;
			setFocus();
		}
		
		public void dragOver(DropTargetEvent event) {
			Event e = new Event();
			Point p = getSourceViewer().getTextWidget().toControl(event.x, event.y);
			e.x = p.x;
			e.y = p.y;
			e.button = 1;
			getSourceViewer().getTextWidget().notifyListeners(SWT.MouseDown, e);
		}		
		
		public void drop(DropTargetEvent evt){
			for(int i=0;i<evt.dataTypes.length;i++){
				try {
					if (LocalSelectionTransfer.getInstance().isSupportedType(evt.dataTypes[i])){
						IStructuredSelection sel = (IStructuredSelection)evt.data;
						Object obj = sel.getFirstElement();
						if(obj instanceof IFile){
							IFile file = (IFile)obj;
							IProject project = ((IFileEditorInput)getEditorInput()).getFile().getProject();
							S2JSFProjectParams params = new S2JSFProjectParams(project);
							String fileName = file.getName();
							
							if(isHTMLfile(fileName,params) && project.equals(file.getProject())){
								String outcome = fileName.substring(0,fileName.lastIndexOf("."));
								String path = file.getProjectRelativePath().toString();
								String root = params.getRoot();
								if(root.startsWith("/")){
									path = "/" + path;
								}
								String toViewId = "/" + path.substring(root.length());
								addNavigation(toViewId,outcome);
							}
						}
					}
				} catch(Exception ex){
					Util.logException(ex);
				}
			}
		}
		
		private boolean isHTMLfile(String filename, S2JSFProjectParams params){
			String[] exts = params.getExtensions().split(",");
			for(int i=0;i<exts.length;i++){
				if(filename.endsWith("." + exts[i].trim())){
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * ナビゲーションケースを追加します。ナビゲーションルールが存在しない場合は同時に追加します。
	 * 
	 * @param toViewId    to-view-id
	 * @param fromOutCome from-outcome
	 */
	private void addNavigation(String toViewId,String fromOutCome){
		
		String xml = getDocumentProvider().getDocument(getEditorInput()).get();
		FuzzyXMLDocument doc = new FuzzyXMLParser().parse(xml);
		
		// 同じルールがあったら何もしない
		FuzzyXMLNode node = Util.selectXPathNode(doc.getDocumentElement(),
				"/faces-config/navigation-rule/navigation-case[from-outcome='"+fromOutCome+"' and to-view-id='"+toViewId+"']");
		if(node!=null){
			return;
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("\t<navigation-rule>\n");
		sb.append("\t\t<navigation-case>\n");
		sb.append("\t\t\t<from-outcome>").append(Util.escapeHTML(fromOutCome)).append("</from-outcome>\n");
		sb.append("\t\t\t<to-view-id>").append(Util.escapeHTML(toViewId)).append("</to-view-id>\n");
		sb.append("\t\t</navigation-case>\n");
		sb.append("\t</navigation-rule>\n");
		
		try {
			IDocument editorDoc = getSourceViewer().getDocument();
			editorDoc.replace(getSourceViewer().getTextWidget().getCaretOffset(), 0, sb.toString());
		} catch(BadLocationException ex){
			Util.logException(ex);
		}
	}
	
	/**
	 * 現在編集中のドキュメントで宣言されているDTDのURL(システムID)を取得します。
	 * DTDが使用されていない場合はnullを返します。
	 * 
	 * @param xml XMLソース
	 * @return DTDのURL
	 */
	public String getDTD(String xml){
		// PUBLIC Identifier
	    Matcher matcher = patternDoctypePublic.matcher(xml);
	    if(matcher.find()){
	        return matcher.group(2);
	    }
	    // SYSTEM Identifier
	    matcher = patternDoctypeSystem.matcher(xml);
	    if(matcher.find()){
	        return matcher.group(1);
	    }
	    return null;
	}
	
	public Object getAdapter(Class adapter) {
		if(IContentOutlinePage.class.equals(adapter)){
			return outline;
		}
		return super.getAdapter(adapter);
	}
	
	/** DOCTYPE宣言を抽出するための正規表現 */
	private Pattern patternDoctypePublic
		= Pattern.compile("<!DOCTYPE[\\s\r\n]+?[^<]+?[\\s\r\n]+?PUBLIC[\\s\r\n]*?\"(.+?)\"[\\s\r\n]*?\"(.+?)\".*?>",Pattern.DOTALL);
	private Pattern patternDoctypeSystem
		= Pattern.compile("<!DOCTYPE[\\s\r\n]+?[^<]+?[\\s\r\n]+?SYSTEM[\\s\r\n]*?\"(.+?)\".*?>",Pattern.DOTALL);
	
	/**
	 * 補完情報を更新します。
	 */
	protected void updateAssist(S2JSFProject project,IFile file){
		try {
			
			IFileEditorInput input = (IFileEditorInput)getEditorInput();
//			S2JSFProjectParams params = new S2JSFProjectParams(input.getFile().getProject());
			
			String xml = getDocumentProvider().getDocument(input).get();
			String dtd = getDTD(xml);
			
			XMLAssistProcessor assistProcessor = 
				(XMLAssistProcessor)((HTMLConfiguration)getSourceViewerConfiguration()).getAssistProcessor();
			
			InputStream in = null;
			if(dtd!=null){
				FacesConfigDTDResolver resolver = new FacesConfigDTDResolver();
				in = resolver.getInputStream(dtd);
			}
			if(in!=null){
				Reader reader = new InputStreamReader(in);
				assistProcessor.updateDTDInfo(reader);
				reader.close();
			}
			
			assistProcessor.update(null, input.getFile());
			
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	// エディタに追加するアクションs
	////////////////////////////////////////////////////////////////////////////	
	/** 選択範囲のXMLをエスケープするアクション */
	private class EscapeXMLAction extends Action {
		
		public EscapeXMLAction(){
			super(S2JSFPlugin.getResourceString("HTMLEditor.EscapeAction"));
			setEnabled(false);
			setAccelerator(SWT.CTRL|'\\');
		}
		
		public void run() {
			ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			try {
				doc.replace(sel.getOffset(),sel.getLength(),Entities.XML.escape(sel.getText()));
			} catch (BadLocationException e) {
				Util.logException(e);
			}
		}
	}
}

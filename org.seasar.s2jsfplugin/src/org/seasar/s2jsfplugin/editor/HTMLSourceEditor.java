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
package org.seasar.s2jsfplugin.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.aonir.fuzzyxml.internal.FuzzyXMLUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.MatchingCharacterPainter;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.assist.HTMLAssistProcessor;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.pref.ColorProvider;

/**
 * HTMLソースエディタ。
 * 
 * @author Naoki Takezoe
 */
public class HTMLSourceEditor extends TextEditor {

	private ColorProvider colorProvider;
	private HTMLHyperlinkProvider hyperlink;
	private HTMLOutlinePage outline;
	private HTMLCharacterPairMatcher pairMatcher;
	
	public static final String GROUP_HTML         = "_html";
	public static final String ACTION_ESCAPE_HTML = "_escape_html";
	public static final String ACTION_COMMENT     = "_comment";
	
	private boolean useSoftTab;
	private String softTab;
	
	public HTMLSourceEditor(SourceViewerConfiguration config) {
		super();
		colorProvider = S2JSFPlugin.getDefault().getColorProvider();
		setSourceViewerConfiguration(config);
		setPreferenceStore(new ChainedPreferenceStore(
				new IPreferenceStore[]{
						getPreferenceStore(),
						S2JSFPlugin.getDefault().getPreferenceStore()
				}));
		
		setAction(ACTION_ESCAPE_HTML,new EscapeHTMLAction());
		setAction(ACTION_COMMENT,new CommentAction());
		
		IPreferenceStore store = S2JSFPlugin.getDefault().getPreferenceStore();
		useSoftTab = store.getBoolean(S2JSFPlugin.PREF_USE_SOFTTAB);
		int softTabWidth = store.getInt(S2JSFPlugin.PREF_SOFTTAB_WIDTH);
		softTab = "";
		for(int i=0;i<softTabWidth;i++){
			softTab = softTab + " ";
		}
		
		outline = new HTMLOutlinePage(this);
	}
	
	private ProjectionSupport fProjectionSupport; 
	
	protected ISourceViewer createSourceViewer(Composite parent,IVerticalRuler ruler, int styles) {
		ISourceViewer viewer= new ProjectionViewer(parent, ruler, fOverviewRuler, true, styles); 
		getSourceViewerDecorationSupport(viewer);
		viewer.getTextWidget().addVerifyListener(new SoftTabVerifyListener());
		return viewer; 
	}
	
	/** ソフトタブを実現するためのリスナ */
	private class SoftTabVerifyListener implements VerifyListener {
		public void verifyText(VerifyEvent evt) {
			if(useSoftTab){
				if(evt.text.equals("\t")){
					evt.text = softTab;
				}
			}
		}
	}
	
	protected HTMLHyperlinkProvider createHyperlinkProvider(){
		return new HTMLHyperlinkProvider(this);
	}
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		hyperlink = createHyperlinkProvider();
		hyperlink.install();
		
		ProjectionViewer projectionViewer= (ProjectionViewer) getSourceViewer(); 
		fProjectionSupport= new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors()); 
		fProjectionSupport.install();
		projectionViewer.doOperation(ProjectionViewer.TOGGLE);
		projectionViewer.getTextWidget().setTabs(
				getPreferenceStore().getInt(
						AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
		
		ITextViewerExtension2 extension= (ITextViewerExtension2) getSourceViewer();
		pairMatcher = new HTMLCharacterPairMatcher();
		pairMatcher.setEnable(getPreferenceStore().getBoolean(S2JSFPlugin.PREF_PAIR_CHAR));
		MatchingCharacterPainter painter = new MatchingCharacterPainter(getSourceViewer(), pairMatcher);
		painter.setColor(Display.getDefault().getSystemColor(SWT.COLOR_GRAY));
		extension.addPainter(painter);
		
		update();
	}
	
	/** 設定が変更されたときの処理 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		return super.affectsTextPresentation(event) || colorProvider.affectsTextPresentation(event);
	}
	
	/** 設定が変更されたときの処理 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event){
		colorProvider.handlePreferenceStoreChanged(event);
		updateAssistProperties(event);
		
		String key = event.getProperty();
		if(key.equals(S2JSFPlugin.PREF_USE_SOFTTAB)){
			useSoftTab = ((Boolean)event.getNewValue()).booleanValue();
		}
		if(key.equals(S2JSFPlugin.PREF_SOFTTAB_WIDTH)){
			int width = ((Integer)event.getNewValue()).intValue();
			softTab = "";
			for(int i=0;i<width;i++){
				softTab = softTab + " ";
			}
		}
		if(key.equals(S2JSFPlugin.PREF_PAIR_CHAR)){
			boolean enable = ((Boolean)event.getNewValue()).booleanValue();
			pairMatcher.setEnable(enable);
		}
		
		super.handlePreferenceStoreChanged(event);
	}	
	
	private void updateAssistProperties(PropertyChangeEvent event){
		String key = event.getProperty();
		try {
			// 自動活性化遅延
			if(key.equals(S2JSFPlugin.PREF_ASSIST_TIMES)){
				ContentAssistant assistant = (ContentAssistant)getSourceViewerConfiguration().getContentAssistant(null);
				assistant.setAutoActivationDelay(Integer.parseInt((String)event.getNewValue()));
			
			// 自動活性化トリガ
			} else if(key.equals(S2JSFPlugin.PREF_ASSIST_CHARS)){
				ContentAssistant assistant = (ContentAssistant)getSourceViewerConfiguration().getContentAssistant(null);
				HTMLAssistProcessor processor = (HTMLAssistProcessor)assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
				processor.setAutoAssistChars(((String)event.getNewValue()).toCharArray());
				
			// 閉じタグの補完
			} else if(key.equals(S2JSFPlugin.PREF_ASSIST_CLOSE)){
				ContentAssistant assistant = (ContentAssistant)getSourceViewerConfiguration().getContentAssistant(null);
				HTMLAssistProcessor processor = (HTMLAssistProcessor)assistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE);
				processor.setAssistCloseTag(((Boolean)event.getNewValue()).booleanValue());
			
			// 自動活性化を使うかどうか
			} else if(key.equals(S2JSFPlugin.PREF_ASSIST_AUTO)){
				ContentAssistant assistant = (ContentAssistant)getSourceViewerConfiguration().getContentAssistant(null);
				assistant.enableAutoActivation(((Boolean)event.getNewValue()).booleanValue());
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/**
	 * Adds actions to the context menu.
	 * <p>
	 * If you want to customize the context menu,
	 * you can override this method in the sub-class 
	 * instead of editorContextMenuAboutToShow(),
	 * 
	 * @param menu IMenuManager
	 */
	protected void addContextMenuActions(IMenuManager menu){
		menu.add(new Separator(GROUP_HTML));
		addAction(menu,GROUP_HTML,ACTION_ESCAPE_HTML);
		addAction(menu,GROUP_HTML,ACTION_COMMENT);
	}
	
	protected final void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		addContextMenuActions(menu);
	}
	
	protected void updateSelectionDependentActions() {
		super.updateSelectionDependentActions();
		ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
		if(sel.getText().equals("")){
			getAction(ACTION_COMMENT).setEnabled(false);
			getAction(ACTION_ESCAPE_HTML).setEnabled(false);
		} else {
			getAction(ACTION_COMMENT).setEnabled(true);
			getAction(ACTION_ESCAPE_HTML).setEnabled(true);
		}
	}
	
	/**
	 * エディタ関連の補完情報とフォールディング情報を更新します。
	 * 以下のタイミングで呼び出されます。
	 * <ul>
	 *   <li>エディタの初期化時</li>
	 *   <li>エディタの保存時</li>
	 * </ul>
	 */
	protected void update(){
		try {
			outline.update();
			updateFolding();
			
			// IFileEditorInput以外の場合は何もしない
			if(!isFileEditorInput()){
				return;
			}
			
			IFile file = ((IFileEditorInput)getEditorInput()).getFile();
			IJavaProject javaProject = JavaCore.create(file.getProject());
			S2JSFProject project = new S2JSFProject(javaProject);
			
			updateAssist(project,file);
			if(hyperlink!=null){
				hyperlink.setS2JSFProject(project);
			}
			
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void dispose() {
		hyperlink.uninstall();
		fProjectionSupport.dispose();
		pairMatcher.dispose();
		super.dispose();
	}
	
	public File getFile(){
		IFile file = ((FileEditorInput)this.getEditorInput()).getFile();
		return file.getLocation().makeAbsolute().toFile();
	}
	
	public File getTempFile(){
		IFile file = ((FileEditorInput)this.getEditorInput()).getFile();
		return new File(file.getLocation().makeAbsolute().toFile().getParentFile(),"." +  file.getName());
	}
	
	protected void createActions() {
	    super.createActions();
	    // コンテンツアシストアクションを追加
	    IAction action = new ContentAssistAction(
	    		S2JSFPlugin.getDefault().getResourceBundle(),"ContentAssistProposal", this);
	    action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
	    setAction("ContentAssistProposal", action);
	}
	
	public Object getAdapter(Class adapter) {
		if(fProjectionSupport!=null){
			Object obj = fProjectionSupport.getAdapter(getSourceViewer(), adapter); 
			if (obj != null){
				return obj; 
			}
		}
		if(IContentOutlinePage.class.equals(adapter)){
			return outline;
		}
		if(IDocumentProvider.class.equals(adapter)){
			return getDocumentProvider();
		}
		if(ISelectionProvider.class.equals(adapter)){
			return getSelectionProvider();
		}
		return super.getAdapter(adapter);
	}
	
	protected void doSetInput(IEditorInput input) throws CoreException {
		if(input instanceof IFileEditorInput){
			setDocumentProvider(new HTMLTextDocumentProvider());
		} else if(input instanceof IStorageEditorInput){
			setDocumentProvider(new HTMLFileDocumentProvider());
		} else {
			setDocumentProvider(new HTMLTextDocumentProvider());
		}
		super.doSetInput(input);
		
		// Eclipse 3.1だとリネームしたときここで止まってしまう
		//update();
	}
	
	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		update();
	}
	
	public void doSaveAs() {
		super.doSaveAs();
		update();
	}
	
	public ISourceViewer getViewer(){
		return getSourceViewer();
	}
	
	public boolean isFileEditorInput(){
		if(getEditorInput() instanceof IFileEditorInput){
			return true;
		}
		return false;
	}
	
	protected void updateAssist(S2JSFProject project,IFile file){
		// アシストプロセッサを更新
		HTMLAssistProcessor processor = 
			(HTMLAssistProcessor)((HTMLConfiguration)getSourceViewerConfiguration()).getAssistProcessor();
		processor.update(project,file);
	}
	
	/** タグを検索するための正規表現 */
	private Pattern tagPattern = Pattern.compile("<([^<]*?)>");
	
	/**
	 * エディタのフォールディング情報を更新します。
	 */
	private void updateFolding(){
		try {
			ProjectionViewer viewer = (ProjectionViewer)getSourceViewer();
			if(viewer==null){
				return;
			}
			ProjectionAnnotationModel model = viewer.getProjectionAnnotationModel();
			if(model==null){
				return;
			}
			
			ArrayList list  = new ArrayList();
			Stack     stack = new Stack();
			IDocument doc   = getDocumentProvider().getDocument(getEditorInput());
			String    xml   = FuzzyXMLUtil.comment2space(doc.get(),true);
			
			Matcher matcher = tagPattern.matcher(xml);
			while(matcher.find()){
				String text = matcher.group(1).trim();
				// XML宣言とDOCTYPE宣言はとりあえず飛ばす
				if(text.startsWith("?")){
					continue;
				}
				// DOCTYPE宣言の場合
				if(text.startsWith("!DOCTYPE")){
					// 同一行の場合はフォールディングしない
					if(doc.getLineOfOffset(matcher.start())!=doc.getLineOfOffset(matcher.end())){
						FoldingInfo info = new FoldingInfo(matcher.start(),matcher.end(),"!DOCTYPE");
						info.end = info.end + countUpReturn(xml,matcher.end());
						list.add(info);
					}
					continue;
				}
				// JSPのソース（ここで処理するのもなんだかなーという気がしますが…）
				if(text.startsWith("%")){
					// 同一行の場合はフォールディングしない
					if(doc.getLineOfOffset(matcher.start())!=doc.getLineOfOffset(matcher.end())){
						FoldingInfo info = new FoldingInfo(matcher.start(),matcher.end(),"%");
						info.end = info.end + countUpReturn(xml,matcher.end());
						list.add(info);
					}
					continue;
				}
				// コメントの場合
				if(text.startsWith("!--")){
					// 同一行の場合はフォールディングしない
					if(doc.getLineOfOffset(matcher.start())!=doc.getLineOfOffset(matcher.end())){
						FoldingInfo info = new FoldingInfo(matcher.start(),matcher.end(),"!--");
						info.end = info.end + countUpReturn(xml,matcher.end());
						list.add(info);
					}
					continue;
				}
				// 閉じタグの場合
				if(text.startsWith("/")){
					text = text.substring(1,text.length());
					while(stack.size()!=0){
						FoldingInfo info = (FoldingInfo)stack.pop();
						if(info.tagName.toLowerCase().equals(text.toLowerCase())){
							info.end = matcher.end();
							// 同一行の場合はフォールディングしない
							if(doc.getLineOfOffset(info.start)!=doc.getLineOfOffset(info.end)){
								// 次の文字が改行だったらそこまで含めてフォールディング
								info.end = info.end + countUpReturn(xml,matcher.end());
								list.add(info);
							}
							break;
						}
					}
					continue;
				}
				// 空タグの場合
				if(text.endsWith("/")){
					// 同一行の場合はフォールディングしない
					if(doc.getLineOfOffset(matcher.start())!=doc.getLineOfOffset(matcher.end())){
						text.substring(0,text.length()-1);
						if(text.indexOf(" ")!=-1){
							text = text.substring(0,text.indexOf(" "));
						}
						// 次の文字が改行だったらそこまで含めてフォールディング
						FoldingInfo info = new FoldingInfo(matcher.start(),matcher.end(),text);
						info.end = info.end + countUpReturn(xml,matcher.end());
						list.add(info);
					}
					continue;
				}
				// 開始タグの場合はスタックに積んでおく
				if(text.indexOf(" ")!=-1){
					text = text.substring(0,text.indexOf(" "));
				}
				stack.push(new FoldingInfo(matcher.start(),0,text));
			}
			
			// 変更のあったアノーテーションだけを反映する
			Iterator ite = model.getAnnotationIterator();
			while(ite.hasNext()){
				ProjectionAnnotation annotation = (ProjectionAnnotation)ite.next();
				Position pos = model.getPosition(annotation);
				boolean remove = true;
				for(int i=0;i<list.size();i++){
					FoldingInfo info = (FoldingInfo)list.get(i);
					if(info.start == pos.offset && info.end == pos.offset + pos.length){
						remove = false;
						list.remove(info);
						break;
					}
				}
				if(remove){
					model.removeAnnotation(annotation);
				}
			}
			for(int i=0;i<list.size();i++){
				FoldingInfo info = (FoldingInfo)list.get(i);
				Position pos = new Position(info.start,info.end - info.start);
				model.addAnnotation(new ProjectionAnnotation(), pos);
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/** 改行コードが続いていた場合に加算するキャラクタ数を計算 */
	private int countUpReturn(String text,int pos){
		int count = 0;
		if(text.length()-1 > pos){
			char c1 = text.charAt(pos);
			if(c1=='\r' || c1=='\n'){
				count++;
				if(c1=='\r' && text.length()-1 > pos+1){
					if(text.charAt(pos+1)=='\n'){
						count++;
					}
				}
			}
		}
		return count;
	}
	
	/** フォールディング情報を一時的に格納するクラス */
	private class FoldingInfo {
		public FoldingInfo(int start,int end,String tagName){
			this.start   = start;
			this.end     = end;
			this.tagName = tagName;
		}
		public int start;
		public int end;
		public String tagName;
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// エディタに追加するアクション群
	////////////////////////////////////////////////////////////////////////////	
	/** 選択範囲のHTMLをエスケープするアクション */
	private class EscapeHTMLAction extends Action {
		
		public EscapeHTMLAction(){
			super(S2JSFPlugin.getResourceString("HTMLEditor.EscapeAction"));
			setEnabled(false);
			setAccelerator(SWT.CTRL|'\\');
		}
		
		public void run() {
			ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			try {
				doc.replace(sel.getOffset(),sel.getLength(),Util.escapeHTML(sel.getText()));
			} catch (BadLocationException e) {
				Util.logException(e);
			}
		}
	}
	
	/** 選択範囲をコメント化するアクション */
	private class CommentAction extends Action {
		
		public CommentAction(){
			super(S2JSFPlugin.getResourceString("HTMLEditor.CommentAction"));
			setEnabled(false);
			setAccelerator(SWT.CTRL|'/');
		}
		
		public void run() {
			ITextSelection sel = (ITextSelection)getSelectionProvider().getSelection();
			IDocument doc = getDocumentProvider().getDocument(getEditorInput());
			String text = sel.getText().trim();
			try {
				if(text.startsWith("<!--") && text.endsWith("-->")){
					text = sel.getText().replaceAll("<!--|-->", "");
					doc.replace(sel.getOffset(),sel.getLength(),text);
				} else {
					doc.replace(sel.getOffset(),sel.getLength(),"<!--" + sel.getText() + "-->");
				}
			} catch (BadLocationException e) {
				Util.logException(e);
			}
		}
	}
}

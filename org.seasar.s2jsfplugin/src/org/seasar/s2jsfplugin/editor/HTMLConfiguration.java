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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.assist.HTMLAssistProcessor;
import org.seasar.s2jsfplugin.pref.ColorProvider;

/**
 * 
 * @author Naoki Takezoe
 */
public class HTMLConfiguration extends SourceViewerConfiguration {
	
	private IEditorPart editor;
	private HTMLDoubleClickStrategy doubleClickStrategy;
	private HTMLScanner scanner;
	private HTMLTagScanner tagScanner;
	private RuleBasedScanner commentScanner;
	private RuleBasedScanner scriptScanner;
	private RuleBasedScanner doctypeScanner;
	private ColorProvider colorProvider;
	private ContentAssistant assistant;
	private HTMLAssistProcessor processor;

	public HTMLConfiguration(ColorProvider colorProvider) {
		this.colorProvider = colorProvider;
	}
	
	public void setEditorPart(IEditorPart editor){
		this.editor = editor;
	}
	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			HTMLPartitionScanner.HTML_COMMENT,
			HTMLPartitionScanner.HTML_TAG,
			HTMLPartitionScanner.HTML_SCRIPT,
			HTMLPartitionScanner.HTML_DOCTYPE};
	}
	
	public HTMLAssistProcessor getAssistProcessor(){
		if(processor==null){
			processor = createAssistProcessor();
		}
		return processor;
	}
	
	protected HTMLAssistProcessor createAssistProcessor(){
		HTMLAssistProcessor processor = new HTMLAssistProcessor();
		return processor;
	}
	
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new HTMLAnnotationHover(editor);
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if(assistant==null){
			assistant = new ContentAssistant();
			assistant.enableAutoInsert(true);
			assistant.setInformationControlCreator(new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent);
				}});
			HTMLAssistProcessor processor = getAssistProcessor();
			assistant.setContentAssistProcessor(processor,IDocument.DEFAULT_CONTENT_TYPE);
			assistant.setContentAssistProcessor(processor,HTMLPartitionScanner.HTML_TAG);
			assistant.install(sourceViewer);
			
			// ï‚äÆÇÃê›íËÇîΩâf
			IPreferenceStore store = S2JSFPlugin.getDefault().getPreferenceStore();
			assistant.enableAutoActivation(store.getBoolean(S2JSFPlugin.PREF_ASSIST_AUTO));
			assistant.setAutoActivationDelay(store.getInt(S2JSFPlugin.PREF_ASSIST_TIMES));
			processor.setAutoAssistChars(store.getString(S2JSFPlugin.PREF_ASSIST_CHARS).toCharArray());
			processor.setAssistCloseTag(store.getBoolean(S2JSFPlugin.PREF_ASSIST_CLOSE));
		}
		return assistant;
	}
	
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer,String contentType) {
		if (doubleClickStrategy == null){
			doubleClickStrategy = new HTMLDoubleClickStrategy();
		}
		return doubleClickStrategy;
	}

	protected HTMLScanner getHTMLScanner() {
		if (scanner == null) {
			scanner = new HTMLScanner(colorProvider);
			scanner.setDefaultReturnToken(
					colorProvider.getToken(S2JSFPlugin.PREF_COLOR_FG));
		}
		return scanner;
	}
	
	protected HTMLTagScanner getTagScanner() {
		if (tagScanner == null) {
			tagScanner = new HTMLTagScanner(colorProvider);
			tagScanner.setDefaultReturnToken(
					colorProvider.getToken(S2JSFPlugin.PREF_COLOR_TAG));
		}
		return tagScanner;
	}
	
	protected RuleBasedScanner getCommentScanner() {
		if (commentScanner == null) {
			commentScanner = new RuleBasedScanner();
			commentScanner.setDefaultReturnToken(
					colorProvider.getToken(S2JSFPlugin.PREF_COLOR_COMMENT));
		}
		return commentScanner;
	}
	
	protected RuleBasedScanner getScriptScanner() {
		if (scriptScanner == null) {
			scriptScanner = new RuleBasedScanner();
			scriptScanner.setDefaultReturnToken(
					colorProvider.getToken(S2JSFPlugin.PREF_COLOR_SCRIPT));
		}
		return scriptScanner;
	}
	
	protected RuleBasedScanner getDoctypeScanner(){
		if (doctypeScanner == null) {
			doctypeScanner = new RuleBasedScanner();
			doctypeScanner.setDefaultReturnToken(
					colorProvider.getToken(S2JSFPlugin.PREF_COLOR_DOCTYPE));
		}
		return doctypeScanner;
	}
	
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = null;
		
		dr = new HTMLTagDamagerRepairer(getTagScanner());
		reconciler.setDamager(dr, HTMLPartitionScanner.HTML_TAG);
		reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_TAG);
		
		dr = new HTMLTagDamagerRepairer(getHTMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new HTMLTagDamagerRepairer(getCommentScanner());
		reconciler.setDamager(dr, HTMLPartitionScanner.HTML_COMMENT);
		reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_COMMENT);
		
		dr = new DefaultDamagerRepairer(getScriptScanner());
		reconciler.setDamager(dr, HTMLPartitionScanner.HTML_SCRIPT);
		reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_SCRIPT);
		
		dr = new DefaultDamagerRepairer(getDoctypeScanner());
		reconciler.setDamager(dr, HTMLPartitionScanner.HTML_DOCTYPE);
		reconciler.setRepairer(dr, HTMLPartitionScanner.HTML_DOCTYPE);
		
		return reconciler;
	}
	
	private class HTMLTagDamagerRepairer extends DefaultDamagerRepairer {
		
		public HTMLTagDamagerRepairer(ITokenScanner scanner) {
			super(scanner);
		}
		
		// TODO This method works with 3.0 and 3.1.2 but does't work well with Eclipse 3.1.1.
		public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {
			if (!documentPartitioningChanged) {
				String source = fDocument.get();
				int start = source.substring(0, e.getOffset()).lastIndexOf('<');
				if(start == -1){
					start = 0;
				}
				int end = source.indexOf('>', e.getOffset());
				int nextEnd = source.indexOf('>', end + 1);
				if(nextEnd >= 0 && nextEnd > end){
					end = nextEnd;
				}
				int end2 = e.getOffset() + (e.getText() == null ? e.getLength() : e.getText().length());
				if(end == -1){
					end = source.length();
				} else if(end2 > end){
					end = end2;
				} else {
					end++;
				}
				
				return new Region(start, end - start);
			}
			return partition;
		}
		
	}

}
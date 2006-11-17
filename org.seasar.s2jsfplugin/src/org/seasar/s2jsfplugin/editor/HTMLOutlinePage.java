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

import java.util.ArrayList;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLComment;
import jp.aonir.fuzzyxml.FuzzyXMLDocType;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.seasar.s2jsfplugin.S2JSFPlugin;

/**
 * HTMLのアウトラインを表示するContentOutlinePage。
 * 
 * @author Naoki Takezoe
 */
public class HTMLOutlinePage extends ContentOutlinePage {
	
	private RootNode root;
	private HTMLSourceEditor editor;
	private FuzzyXMLDocument doc;
	
	public HTMLOutlinePage(HTMLSourceEditor editor){
		super();
		this.editor = editor;
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		root = new RootNode();
		viewer.setContentProvider(new HTMLContentProvider());
		viewer.setLabelProvider(new HTMLLabelProvider());
		viewer.setInput(root);
		viewer.addSelectionChangedListener(new HTMLSelectionChangedListener());
		try {
			viewer.setExpandedState(root.getChildren()[0],true);
		} catch(Exception ex){
			// ignore
		}
		update();
	}
	
	protected Image getNodeImage(FuzzyXMLNode element){
		if(element instanceof FuzzyXMLElement){
			FuzzyXMLElement e = (FuzzyXMLElement)element;
			if(e.getName().equalsIgnoreCase("html")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_HTML);
			} else if(e.getName().equalsIgnoreCase("title")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TITLE);
			} else if(e.getName().equalsIgnoreCase("body")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_BODY);
			} else if(e.getName().equalsIgnoreCase("form")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_FORM);
			} else if(e.getName().equalsIgnoreCase("img")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_IMAGE);
			} else if(e.getName().equalsIgnoreCase("a")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_LINK);
			} else if(e.getName().equalsIgnoreCase("table")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TABLE);
			} else if(e.getName().equalsIgnoreCase("input")){
				String type = e.getAttributeValue("type");
				if(type!=null){
					if(type.equalsIgnoreCase("button") || type.equalsIgnoreCase("reset") || type.equalsIgnoreCase("submit")){
						return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_BUTTON);
					} else if(type.equalsIgnoreCase("radio")){
						return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_RADIO);
					} else if(type.equalsIgnoreCase("checkbox")){
						return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_CHECK);
					} else if(type.equalsIgnoreCase("text")){
						return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TEXT);
					} else if(type.equalsIgnoreCase("password")){
						return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_PASSWD);
					} else if(type.equalsIgnoreCase("hidden")){
						return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_HIDDEN);
					}
				}
			} else if(e.getName().equalsIgnoreCase("select")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_SELECT);
			} else if(e.getName().equalsIgnoreCase("textarea")){
				return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TEXTAREA);
			}
			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_TAG);
		} else if(element instanceof FuzzyXMLDocType){
			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_DOCTYPE);
		} else if(element instanceof FuzzyXMLComment){
			return S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_COMMENT);
		}
		return null;
	}
	
	protected Object[] getNodeChildren(FuzzyXMLElement element){
		ArrayList children = new ArrayList();
		FuzzyXMLNode[] nodes = element.getChildren();
		for(int i=0;i<nodes.length;i++){
			if(nodes[i] instanceof FuzzyXMLElement){
				children.add(nodes[i]);
			} else if(nodes[i] instanceof FuzzyXMLDocType){
				children.add(nodes[i]);
			} else if(nodes[i] instanceof FuzzyXMLComment){
				children.add(nodes[i]);
			}
		}
		return (FuzzyXMLNode[])children.toArray(new FuzzyXMLNode[children.size()]);
	}
	
	protected String getNodeText(FuzzyXMLNode node){
		if(node instanceof FuzzyXMLElement){
			StringBuffer sb = new StringBuffer();
			FuzzyXMLAttribute[] attrs = ((FuzzyXMLElement)node).getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(sb.length() != 0){
					sb.append(", ");
				}
				sb.append(attrs[i].getName() + "=" + attrs[i].getValue());
			}
			if(sb.length()==0){
				return ((FuzzyXMLElement)node).getName();
			} else {
				return ((FuzzyXMLElement)node).getName() + "(" + sb.toString() + ")";
			}
		}
		if(node instanceof FuzzyXMLDocType){
			return "DOCTYPE";
		}
		if(node instanceof FuzzyXMLComment){
			return "#comment";
		}
		return node.toString();
	}
	
	protected boolean isHTML(){
		return true;
	}
	
	public void update(){
		String source = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
		this.doc = new FuzzyXMLParser(isHTML()).parse(source);
		TreeViewer viewer = getTreeViewer();
		if(viewer!=null){
			viewer.refresh();
		}
	}
	
	/** ルートエレメント */
	private class RootNode {
		
		public RootNode(){
			super();
		}
		
		public Object[] getChildren(){
			FuzzyXMLNode[] nodes = doc.getDocumentElement().getChildren();
			ArrayList children = new ArrayList();
			for(int i=0;i<nodes.length;i++){
				if(nodes[i] instanceof FuzzyXMLElement){
					children.add(nodes[i]);
				}
			}
			return (FuzzyXMLElement[])children.toArray(new FuzzyXMLElement[children.size()]);
		}
		
		public boolean equals(Object obj){
			if(obj instanceof RootNode){
				return true;
			}
			return false;
		}
	}
	
	/** HTMLアウトラインのコンテンツプロバイダ */
	private class HTMLContentProvider implements ITreeContentProvider {
		
		public Object[] getChildren(Object parentElement) {
			if(parentElement instanceof RootNode){
				return ((RootNode)parentElement).getChildren();
			} else if(parentElement instanceof FuzzyXMLElement){
				return getNodeChildren((FuzzyXMLElement)parentElement);
			}
			return new Object[0];
		}
		
		public Object getParent(Object element) {
			if(element instanceof FuzzyXMLNode){
				FuzzyXMLNode parent = ((FuzzyXMLNode)element).getParentNode();
				if(parent==null){
					return root.getChildren()[0];
				}
				return parent;
			}
			return null;
		}
		
		public boolean hasChildren(Object element) {
			if(getChildren(element).length==0){
				return false;
			} else {
				return true;
			}
		}
		
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		
		public void dispose() {
		}
		
		public void inputChanged(Viewer viewer, Object oldInput,Object newInput) {
		}
	}
	
	/** HTMLアウトラインのラベルプロバイダ */
	private class HTMLLabelProvider extends LabelProvider {
		
		public Image getImage(Object element) {
			if(element instanceof FuzzyXMLNode){
				return getNodeImage((FuzzyXMLNode)element);
			}
			return null;
		}
		
		public String getText(Object element) {
			if(element instanceof FuzzyXMLNode){
				return getNodeText((FuzzyXMLNode)element);
			}
			return super.getText(element);
		}
	}
	
	/** ツリービューアで選択が変更された場合のリスナ */
	private class HTMLSelectionChangedListener implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			IStructuredSelection sel = (IStructuredSelection)event.getSelection();
			Object element = sel.getFirstElement();
			if(element instanceof FuzzyXMLNode){
				int offset = ((FuzzyXMLNode)element).getOffset();
			    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			    IEditorPart editorPart = page.getActiveEditor();
			    if(editorPart instanceof HTMLEditor){
			    	((HTMLEditor)editorPart).setOffset(offset);
			    } else if(editorPart instanceof HTMLSourceEditor){
			    	((HTMLSourceEditor)editorPart).selectAndReveal(offset, 0);
			    }
			}
		}
	}
}

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

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.assist.JSFTagDefinition;
import org.seasar.s2jsfplugin.assist.S2JSFSelecterMap;
import org.seasar.s2jsfplugin.model.ManagedBean;
import org.seasar.s2jsfplugin.model.ManagedBeanProperty;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * ハイパーリンク機能を提供するクラス。
 * 
 * @author Naoki Takezoe
 */
public class HTMLHyperlinkProvider implements KeyListener,MouseListener,MouseMoveListener,
	FocusListener, PaintListener, IDocumentListener, ITextInputListener, ITextPresentationListener {
	
	private boolean active;
	private IRegion activeRegion;
	private Cursor cursor;
	private Color color;
	private Object open;
	private Position fRememberedPosition;
	private S2JSFProject project;
	private HTMLSourceEditor editor;
	
	public HTMLHyperlinkProvider(HTMLSourceEditor editor){
		this.editor = editor;
	}
	
	public void install() {
		ISourceViewer sourceViewer= editor.getViewer();
		if (sourceViewer == null)
			return;
			
		StyledText text= sourceViewer.getTextWidget();			
		if (text == null || text.isDisposed())
			return;
			
		color = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
//		updateColor(sourceViewer);

		sourceViewer.addTextInputListener(this);
		
		IDocument document= sourceViewer.getDocument();
		if (document != null)
			document.addDocumentListener(this);			

		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		text.addPaintListener(this);
		
		((ITextViewerExtension4)sourceViewer).addTextPresentationListener(this);
		
//		updateKeyModifierMask();
		
//		IPreferenceStore preferenceStore= getPreferenceStore();
//		preferenceStore.addPropertyChangeListener(this);
	}
	
	public void uninstall() {

//		if (color != null) {
//			color.dispose();
//			color= null;
//		}
		
		if (cursor != null) {
			cursor.dispose();
			cursor= null;
		}
		
		ISourceViewer sourceViewer= editor.getViewer();
		if (sourceViewer != null)
			sourceViewer.removeTextInputListener(this);
		
		IDocumentProvider documentProvider= editor.getDocumentProvider();
		if (documentProvider != null) {
			IDocument document= documentProvider.getDocument(editor.getEditorInput());
			if (document != null)
				document.removeDocumentListener(this);
		}
			
//		IPreferenceStore preferenceStore= getPreferenceStore();
//		if (preferenceStore != null)
//			preferenceStore.removePropertyChangeListener(this);
		
		if (sourceViewer == null)
			return;
		
		StyledText text= sourceViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
			
		text.removeKeyListener(this);
		text.removeMouseListener(this);
		text.removeMouseMoveListener(this);
		text.removeFocusListener(this);
		text.removePaintListener(this);
		
		((ITextViewerExtension4)sourceViewer).removeTextPresentationListener(this);
	}
	
	public void deactivate() {
		deactivate(false);
	}
	
	public void deactivate(boolean redrawAll) {
		if (!active)
			return;
			repairRepresentation(redrawAll);
		active = false;
	}
	
	private void resetCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text != null && !text.isDisposed())
			text.setCursor(null);
					
		if (cursor != null) {
			cursor.dispose();
			cursor= null;
		}
	}
	
	private void repairRepresentation() {
		repairRepresentation(false);
	}
	
	private void repairRepresentation(boolean redrawAll) {
		if (activeRegion == null)
			return;
		
		int offset= activeRegion.getOffset();
		int length= activeRegion.getLength();
		activeRegion= null;
			
		ISourceViewer viewer= editor.getViewer();
		if (viewer != null) {
			
			resetCursor(viewer);
			
			// Invalidate ==> remove applied text presentation
			if (!redrawAll && viewer instanceof ITextViewerExtension2)
				((ITextViewerExtension2) viewer).invalidateTextPresentation(offset, length);
			else
				viewer.invalidateTextPresentation();
			
			// Remove underline
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				offset= extension.modelOffset2WidgetOffset(offset);
			} else {
				offset -= viewer.getVisibleRegion().getOffset();
			}
			try {
				StyledText text= viewer.getTextWidget();

				text.redrawRange(offset, length, false);
			} catch (IllegalArgumentException x) {
//				JavaPlugin.log(x);
			}
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if (active) {
			deactivate();
			return;	
		}
		if (e.keyCode != SWT.CTRL) {
			deactivate();
			return;
		}
		active = true;
	}
	
	public void keyReleased(KeyEvent event) {
		if (!active)
			return;
		deactivate();
	}

	
	public void mouseUp(MouseEvent e) {
		if (!active)
			return;
			
		if (e.button != 1) {
			deactivate();
			return;
		}
		
		boolean wasActive= cursor != null;
		deactivate();
		
		if (wasActive) {
			try {
				if(open instanceof IFile){
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					IDE.openEditor(window.getActivePage(),(IFile)open,true);
				} else if(open instanceof IJavaElement){
					JavaUI.revealInEditor(JavaUI.openInEditor((IJavaElement)open), (IJavaElement)open);
				}
			} catch(Exception ex){
				Util.logException(ex);
			}
		}
	}
	
	public void mouseDoubleClick(MouseEvent e) {}
	
	public void mouseDown(MouseEvent event) {
		if (!active)
			return;
			
		if (event.stateMask != SWT.CTRL) {
			deactivate();
			return;	
		}
		if (event.button != 1) {
			deactivate();
			return;	
		}			
	}
	
	public void mouseMove(MouseEvent event) {
		
		if (event.widget instanceof Control && !((Control) event.widget).isFocusControl()) {
			deactivate();
			return;
		}
		
		if (!active) {
			if (event.stateMask != SWT.CTRL)
				return;
			// modifier was already pressed
			active = true;
		}

		ISourceViewer viewer= editor.getViewer();
		if (viewer == null) {
			deactivate();
			return;
		}
			
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			deactivate();
			return;
		}
			
		if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() != 0) {
			deactivate();
			return;
		}
	
		IRegion region= getCurrentTextRegion(viewer);
		if (region == null || region.getLength() == 0) {
			repairRepresentation();
			return;
		}
		
		highlightRegion(viewer, region);
		activateCursor(viewer);
	}
	
	private void activateCursor(ISourceViewer viewer) {
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		Display display= text.getDisplay();
		if (cursor == null)
			cursor= new Cursor(display, SWT.CURSOR_HAND);
		text.setCursor(cursor);
	}
	
	/**
	 * キャレット位置からハイパーリンクする範囲を取得します。
	 * 
	 * @param doc エディタで編集中のIDocumentオブジェクト
	 * @param offset キャレットのオフセット
	 * @return ハイパーリンクする範囲を示すIRegionオブジェクト
	 */
	protected IRegion selectWord(IDocument doc,int offset){
		FuzzyXMLDocument document = new FuzzyXMLParser().parse(doc.get());
		FuzzyXMLElement element = document.getElementByOffset(offset);
		if(element==null){
			return null;
		}
		FuzzyXMLAttribute[] attrs = element.getAttributes();
		for(int i=0;i<attrs.length;i++){
			if(attrs[i].getOffset() < offset && offset < attrs[i].getOffset()+attrs[i].getLength()){
				int attrOffset = getAttributeValueOffset(doc.get(),attrs[i]);
				int attrLength = attrs[i].getValue().length();
				if(attrOffset >= 0 && attrLength >= 0 && attrOffset <= offset){
					// キャレット位置の単語を切り出す
					String value = attrs[i].getValue();
					boolean isEL = value.startsWith("#{");
					int increaseOffset = 0;
					StringBuffer sb = new StringBuffer();
					for(int j=0;j<value.length();j++){
						char c = value.charAt(j);
						if(!isEL || Character.isJavaIdentifierPart(c) || c=='.'){
							if(sb.length()==0){
								increaseOffset = j;
							}
							sb.append(c);
						} else {
							if(attrOffset+j > offset){
								break;
							}
							sb.setLength(0);
						}
					}
					String lastWord = sb.toString();
					Object open = getOpenFileInfo(document,element,attrs[i].getName(),attrs[i].getValue(),lastWord);
					setOpenFile(open);
					if(open!=null){
						return new Region(attrOffset+increaseOffset,lastWord.length());
					}
				}
				return null;
			}
		}
		return null;
	}
	
	/**
	 * selectWordメソッド内で検出したオープン対象のオブジェクトをセットします。
	 * 
	 * @param open オープン対象のオブジェクト
	 */
	protected void setOpenFile(Object open){
		this.open = open;
	}
	
	/**
	 * S2JSFプロジェクトを取得します。
	 * 
	 * @return S2JSFプロジェクト
	 */
	protected S2JSFProject getProject(){
		return this.project;
	}
	
	/**
	 * ハイパーリンクで開くオブジェクトを取得します。
	 * 
	 * @param doc       FuzzyXMLDocumentオブジェクト
	 * @param element   キャレット位置の要素を示すFuzzyXMLElementオブジェクト
	 * @param attrName  キャレット位置の属性名
	 * @param attrValue キャレット位置の属性値
	 * @param lastValue キャレット位置の単語
	 */
	private Object getOpenFileInfo(FuzzyXMLDocument doc,FuzzyXMLElement element,String attrName,String attrValue,String lastWord){
		try {
			if(project==null){
				return null;
			}
			if(attrValue.startsWith("#{") && attrValue.endsWith("}")){
				
				ManagedBean initBean = Util.getInitBean(project,
						(FuzzyXMLElement)XPath.selectSingleNode(doc.getDocumentElement(),"*"));
				
				// バインディングの場合
				String[] dim = lastWord.split("\\.");
				ManagedBean bean = project.getManagedBean(dim[0]);
				if(bean==null){
					if(initBean!=null){
						ManagedBeanProperty[] props = initBean.getProperties();
						for(int i=0;i<props.length;i++){
							if(props[i].getPropertyName().equalsIgnoreCase(dim[0])){
								bean = props[i].toManagedBean();
								break;
							}
						}
					}
					if(bean==null){
						return null;
					}
				}
				String className = bean.getClassName();
				IType type = project.getJavaProject().findType(className);
				IType diconType = type;
				if(dim.length > 1){
					String jsfTagName[] = S2JSFSelecterMap.getTagName(project,element,Util.getMayaPrefix(element));
					String attrType = JSFTagDefinition.getAttributeInfo(jsfTagName[1],attrName.split(":")[1]);
					
					ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
					IType[] superClass = hierarchy.getAllSuperclasses(type);
					if(attrType==JSFTagDefinition.PROPERTY || attrType==JSFTagDefinition.VALUE){
						// ValueBindingの場合
						String getter = "get" + dim[1].substring(0,1).toUpperCase() + dim[1].substring(1);
						String is     = "is"  + dim[1].substring(0,1).toUpperCase() + dim[1].substring(1);
						for(int j=0;j<superClass.length+1;j++){
							if(j!=0){
								type = superClass[j-1];
							}
							IMethod[] methods = type.getMethods();
							for(int i=0;i<methods.length;i++){
								String methodName = methods[i].getElementName();
								if((methodName.equals(getter) || methodName.equals(is))
										&& methods[i].getParameterTypes().length==0 
										&& !methods[i].getReturnType().equals("V")){
									return methods[i];
								}
							}
						}
					} else {
						for(int j=0;j<superClass.length+1;j++){
							// MethodBindingの場合
							if(j!=0){
								type = superClass[j-1];
							}
							IMethod[] methods = type.getMethods();
							for(int i=0;i<methods.length;i++){
								String methodName = methods[i].getElementName();
								if(methodName.equals(dim[1])){
									return methods[i];
								}
							}
						}
					}
				}
				return diconType;
			}
			
			if(attrName.equalsIgnoreCase("href")){
				// ファイル指定の場合（相対パス？）
				String href = attrValue;
				if(href.indexOf("#") > 0){
					href = href.substring(0,href.indexOf("#"));
				}
				IFile file = ((IFileEditorInput)editor.getEditorInput()).getFile();
				IPath path = file.getParent().getProjectRelativePath();
				IResource resource = project.getJavaProject().getProject().findMember(path.append(href));
				if(resource!=null && resource.exists() && resource instanceof IFile){
					return resource;
				}
			}
			
			// Maya名前空間のプレフィックスを取得
			String mayaPrefix = Util.getMayaPrefix(element);
			
			if(mayaPrefix != null){
				if(element.getName().equalsIgnoreCase("html") && attrName.equals(mayaPrefix + ":extends")){
					// htmlタグのm:extends属性で指定されたテンプレート
					IPath path = project.getJavaProject().getProject().getProjectRelativePath();
					if(attrValue.startsWith("/")){
						S2JSFProjectParams params = new S2JSFProjectParams(project.getJavaProject().getProject());
						path = path.append(params.getRoot());
					}
					IResource resource = project.getJavaProject().getProject().findMember(path.append(attrValue));
					if(resource!=null && resource.exists() && resource instanceof IFile){
						return resource;
					}
				}
			}
			
		} catch(Exception ex){
			Util.logException(ex);
		}
		return null;
	}
	
	/**
	 * 属性値部分の開始オフセットを取得するためのユーティリティメソッド。
	 */
	private int getAttributeValueOffset(String source,FuzzyXMLAttribute attr){
		int offset = source.indexOf('=',attr.getOffset());
		if(offset == -1){
			return -1;
		}
		char c = ' ';
		while(c==' ' || c=='\t' || c=='\r' || c=='\n' || c=='"' || c=='\''){
			offset++;
			if(source.length() == offset+1){
				break;
			}
			c = source.charAt(offset);
		}
		return offset;
	}
	
	private IRegion getCurrentTextRegion(ISourceViewer viewer) {

		int offset= getCurrentTextOffset(viewer);
		if (offset == -1)
			return null;

		return selectWord(viewer.getDocument(), offset);
	}

	private int getCurrentTextOffset(ISourceViewer viewer) {

		try {
			StyledText text= viewer.getTextWidget();
			if (text == null || text.isDisposed())
				return -1;

			Display display= text.getDisplay();
			Point absolutePosition= display.getCursorLocation();
			Point relativePosition= text.toControl(absolutePosition);
			
			int widgetOffset= text.getOffsetAtLocation(relativePosition);
			if (viewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			} else {
				return widgetOffset + viewer.getVisibleRegion().getOffset();
			}

		} catch (IllegalArgumentException e) {
			return -1;
		}
	}
	
	private void highlightRegion(ISourceViewer viewer, IRegion region) {

		if (region.equals(activeRegion))
			return;

		repairRepresentation();
		
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
		
		// Underline
		int offset= 0;
		int length= 0;
		if (viewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(region);
			if (widgetRange == null)
				return;
				
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			offset= region.getOffset() - viewer.getVisibleRegion().getOffset();
			length= region.getLength();
		}
		text.redrawRange(offset, length, false);
		
		// Invalidate region ==> apply text presentation
		activeRegion = region;
		if (viewer instanceof ITextViewerExtension2)
			((ITextViewerExtension2) viewer).invalidateTextPresentation(region.getOffset(), region.getLength());
		else
			viewer.invalidateTextPresentation();
	}
	
	public void focusGained(FocusEvent e) {}

	public void focusLost(FocusEvent event) {
		deactivate();
	}
	
	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentAboutToBeChanged(DocumentEvent event) {
		if (active && activeRegion != null) {
			fRememberedPosition= new Position(activeRegion.getOffset(), activeRegion.getLength());
			try {
				event.getDocument().addPosition(fRememberedPosition);
			} catch (BadLocationException x) {
				fRememberedPosition= null;
			}
		}
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public void documentChanged(DocumentEvent event) {
		if (fRememberedPosition != null) {
			if (!fRememberedPosition.isDeleted()) {
				
				event.getDocument().removePosition(fRememberedPosition);
				activeRegion= new Region(fRememberedPosition.getOffset(), fRememberedPosition.getLength());
				fRememberedPosition= null;
				
				ISourceViewer viewer= editor.getViewer();
				if (viewer != null) {
					StyledText widget= viewer.getTextWidget();
					if (widget != null && !widget.isDisposed()) {
						widget.getDisplay().asyncExec(new Runnable() {
							public void run() {
								deactivate();
							}
						});
					}
				}
				
			} else {
				activeRegion= null;
				fRememberedPosition= null;
				deactivate();
			}
		}
	}

	public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		if (oldInput == null)
			return;
		deactivate();
		oldInput.removeDocumentListener(this);
	}

	public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
		if (newInput == null)
			return;
		newInput.addDocumentListener(this);
	}
	
	public void applyTextPresentation(TextPresentation textPresentation) {
		if (activeRegion == null)
			return;
		IRegion region= textPresentation.getExtent();
		if (activeRegion.getOffset() + activeRegion.getLength() >= region.getOffset() && region.getOffset() + region.getLength() > activeRegion.getOffset())
			textPresentation.mergeStyleRange(new StyleRange(activeRegion.getOffset(), activeRegion.getLength(), color, null));
	}

	public void paintControl(PaintEvent event) {	
		if (activeRegion == null)
			return;

		ISourceViewer viewer= editor.getViewer();
		if (viewer == null)
			return;
			
		StyledText text= viewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;
			
			
		int offset= 0;
		int length= 0;

		if (viewer instanceof ITextViewerExtension5) {
			
			ITextViewerExtension5 extension= (ITextViewerExtension5) viewer;
			IRegion widgetRange= extension.modelRange2WidgetRange(activeRegion);
			if (widgetRange == null)
				return;
				
			offset= widgetRange.getOffset();
			length= widgetRange.getLength();
			
		} else {
			
			IRegion region= viewer.getVisibleRegion();			
			if (!includes(region, activeRegion))
				return;		    
			
			offset= activeRegion.getOffset() - region.getOffset();
			length= activeRegion.getLength();
		}
		
		// support for bidi
		Point minLocation= getMinimumLocation(text, offset, length);
		Point maxLocation= getMaximumLocation(text, offset, length);

		int x1= minLocation.x;
		int x2= minLocation.x + maxLocation.x - minLocation.x - 1;
		int y= minLocation.y + text.getLineHeight() - 1;
		
		GC gc= event.gc;
		if (color != null && !color.isDisposed())
		gc.setForeground(color);
		gc.drawLine(x1, y, x2, y);
	}
	
	private boolean includes(IRegion region, IRegion position) {
		return
			position.getOffset() >= region.getOffset() &&
			position.getOffset() + position.getLength() <= region.getOffset() + region.getLength();
	}
	
	private Point getMinimumLocation(StyledText text, int offset, int length) {
		Point minLocation= new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x < minLocation.x)
				minLocation.x= location.x;			
			if (location.y < minLocation.y)
				minLocation.y= location.y;			
		}	
		
		return minLocation;
	}

	private Point getMaximumLocation(StyledText text, int offset, int length) {
		Point maxLocation= new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
		for (int i= 0; i <= length; i++) {
			Point location= text.getLocationAtOffset(offset + i);
			
			if (location.x > maxLocation.x)
				maxLocation.x= location.x;			
			if (location.y > maxLocation.y)
				maxLocation.y= location.y;			
		}	
		
		return maxLocation;
	}
	
	public void setS2JSFProject(S2JSFProject project){
		this.project = project;
	}
}

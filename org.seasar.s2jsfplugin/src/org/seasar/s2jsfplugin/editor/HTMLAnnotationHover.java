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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

/**
 * エディタで編集中のリソースの付与されているマーカのメッセージを
 * ルーラにホバー表示します。
 * 
 * @author Naoki Takezoe
 */
public class HTMLAnnotationHover implements IAnnotationHover {

	private IEditorPart editor;
	
	/**
	 * コンストラクタ。
	 * 
	 * @param editor 対象のエディタ
	 */
	public HTMLAnnotationHover(IEditorPart editor) {
		this.editor = editor;
	}
	
	/**
	 * エディタで編集中のリソースから全てのマーカを取得します。
	 * 
	 * @return マーカの配列
	 */
	private IMarker[] getMarker() {
		try {
			IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
			IFile file = input.getFile();
			return file.findMarkers(IMarker.MARKER, true, IFile.DEPTH_ZERO);
		} catch (CoreException e) {
			return new IMarker[0];
		}
	}
	
	/**
	 * ホバー表示するテキストを返却します。
	 * 指定された行に複数のマーカが存在する場合は複数行のテキストを返却します。
	 * 
	 * @param sourceViewer ソースビューア
	 * @param lineNumber 行番号
	 * @return ホバー表示するテキスト
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		IMarker[] marker = getMarker();
		if(marker != null) {
		    StringBuffer buffer = new StringBuffer();
			for(int i = 0; i < marker.length; i++) {
				try {
					Integer integer = (Integer)marker[i].getAttribute(IMarker.LINE_NUMBER);
					if((integer != null) && (integer.intValue() == lineNumber + 1)) {
					    String message = (String)marker[i].getAttribute(IMarker.MESSAGE);
					    if(message!=null && message.length()!=0) {
						    if(buffer.length() > 0) {
						        buffer.append("\r\n");
						    }
							buffer.append(message);
					    }
					}
				} catch (CoreException e) {
				}
			}
			return buffer.toString();
		}
		return null;
	}

}

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

import org.eclipse.swt.browser.Browser;

/**
 * HTMLエディタのEditorPartが実装するインターフェース。
 * 
 * @author Naoki Takezoe
 */
public interface HTMLEditorPart {
    
	/**
	 * プレビュー用のブラウザウィジェットを返却します。
	 * 
	 * @return Browser
	 */
    public Browser getBrowser();
    
    /**
     * ソース編集用のエディタを返却します。
     * 
     * @return HTMLSourceEditor
     */
    public HTMLSourceEditor getSourceEditor();
    
    /**
     * EditorInputがIFileEditorInputかどうかを調べます。
     * 
     * @return IFileEditorInputの場合true、そうでない場合false
     */
    public boolean isFileEditorInput();
}

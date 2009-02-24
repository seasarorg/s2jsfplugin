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
 * HTML�G�f�B�^��EditorPart����������C���^�[�t�F�[�X�B
 * 
 * @author Naoki Takezoe
 */
public interface HTMLEditorPart {
    
	/**
	 * �v���r���[�p�̃u���E�U�E�B�W�F�b�g��ԋp���܂��B
	 * 
	 * @return Browser
	 */
    public Browser getBrowser();
    
    /**
     * �\�[�X�ҏW�p�̃G�f�B�^��ԋp���܂��B
     * 
     * @return HTMLSourceEditor
     */
    public HTMLSourceEditor getSourceEditor();
    
    /**
     * EditorInput��IFileEditorInput���ǂ����𒲂ׂ܂��B
     * 
     * @return IFileEditorInput�̏ꍇtrue�A�����łȂ��ꍇfalse
     */
    public boolean isFileEditorInput();
}

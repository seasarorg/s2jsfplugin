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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.seasar.s2jsfplugin.Util;

/**
 * HTML�t�@�C���Ɏ����ҏW�@�\��񋟂��܂��B
 * 
 * @author Naoki Takezoe
 * @since 1.1.1
 */
public class HTMLAutoEditStrategy extends DefaultIndentLineAutoEditStrategy {
	
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		try {
			if("-".equals(c.text) && c.offset >= 3 && d.get(c.offset - 3, 3).equals("<!-")){
				c.text = "-  -->";
				c.shiftsCaret = false;
				c.caretOffset = c.offset + 2;
				c.doit = false;	
				return;
			}
			if("[".equals(c.text) && c.offset >= 2 && d.get(c.offset - 2, 2).equals("<!")){
				c.text = "[CDATA[]]>";
				c.shiftsCaret = false;
				c.caretOffset = c.offset + 7;
				c.doit = false;	
				return;
			}
		} catch (BadLocationException e) {
			Util.logException(e);
		}
		super.customizeDocumentCommand(d, c);
	}
	
}

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

import org.eclipse.jface.text.rules.*;

/**
 * HTMLタグ（&lt;と&gt;で囲まれた範囲）にマッチするルール。
 * 
 * @author Naoki Takezoe
 */
public class HTMLTagRule extends MultiLineRule {

	public HTMLTagRule(IToken token) {
		super("<", ">", token);
	}
	
	protected boolean sequenceDetected(ICharacterScanner scanner,char[] sequence,boolean eofAllowed) {
		if (sequence[0] == '<') {
			int c = scanner.read();
			if (c=='?' || c=='!' || c=='%') {
				scanner.unread();
				return false;
			}
		} else if (sequence[0] == '>') {
			// 前の１文字を読む
			scanner.unread();
			scanner.unread();
			int c = scanner.read();
			// 元に戻しておく
			scanner.read();
			
			if(c=='%') {
				return false;
			}
		}
		return super.sequenceDetected(scanner, sequence, eofAllowed);
	}
	
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		
		int c;
		boolean doubleQuoted = false;
		boolean singleQuoted = false;
		
		char[][] delimiters= scanner.getLegalLineDelimiters();
		boolean previousWasEscapeCharacter = false;	
		while ((c= scanner.read()) != ICharacterScanner.EOF) {
			if (c == fEscapeCharacter) {
				// Skip the escaped character.
				scanner.read();
			} else if(c=='"'){
				if(singleQuoted==false){
					doubleQuoted = !doubleQuoted;
				}
			} else if(c=='\''){
				if(doubleQuoted==false){
					singleQuoted = !singleQuoted;
				}
			} else if (fEndSequence.length > 0 && c == fEndSequence[0]) {
				// Check if the specified end sequence has been found.
				if (doubleQuoted==false && singleQuoted==false && sequenceDetected(scanner, fEndSequence, true))
					return true;
			} else if (fBreaksOnEOL) {
				// Check for end of line since it can be used to terminate the pattern.
				for (int i= 0; i < delimiters.length; i++) {
					if (c == delimiters[i][0] && sequenceDetected(scanner, delimiters[i], true)) {
						if (!fEscapeContinuesLine || !previousWasEscapeCharacter)
							return true;
					}
				}
			}
			previousWasEscapeCharacter = (c == fEscapeCharacter);
		}
		if (fBreaksOnEOF) return true;
		scanner.unread();
		return false;
	}
}

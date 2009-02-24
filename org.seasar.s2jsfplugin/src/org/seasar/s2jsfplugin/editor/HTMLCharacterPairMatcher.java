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

import jp.aonir.fuzzyxml.internal.FuzzyXMLUtil;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

/**
 * @author Naoki Takezoe
 */
public class HTMLCharacterPairMatcher implements ICharacterPairMatcher {

	private int fAnchor;
	private boolean enable;
	
	public HTMLCharacterPairMatcher() {
	}
	
	public void setEnable(boolean enable){
		this.enable = enable;
	}
	
	public void dispose() {
	}

	public void clear() {
	}

	public IRegion match(IDocument document, int offset) {
		if(offset < 0 || offset > document.getLength() || !enable){
			return null;
		}
		
		String text = document.get();
		text = FuzzyXMLUtil.escapeString(text);
		
		if(offset < document.getLength()){
			char c = text.charAt(offset);
			if(c=='>' || c=='}' || c==')'){
				int place = text.lastIndexOf(getPairCharacter(c), offset - 1);
				if(place >= 0){
					fAnchor = ICharacterPairMatcher.LEFT;
					return new Region(place, 1);
				}
			}
			if(c=='"' || c=='\''){
				String substr = text.substring(0, offset - 1);
				int stoffset = substr.lastIndexOf(c);
				int eqoffset = substr.lastIndexOf('=');
				if(stoffset > eqoffset){
					int place = substr.lastIndexOf(c, offset - 1);
					if(place >= 0){
						fAnchor = ICharacterPairMatcher.LEFT;
						return new Region(place, 1);
					}
				}
			}
		}
		if(offset > 0){
			char c = text.charAt(offset - 1);
			if(c=='<' || c=='{' || c=='('){
				int place = text.indexOf(getPairCharacter(c), offset + 1);
				if(place >= 0){
					fAnchor = ICharacterPairMatcher.RIGHT;
					return new Region(place, 1);
				}
			}
			if(c=='"' || c=='\''){
				String substr = text.substring(0, offset - 1);
				int stoffset = substr.lastIndexOf(c);
				int eqoffset = substr.lastIndexOf('=');
				if(stoffset < eqoffset){
					int place = text.indexOf(c, offset);
					if(place >= 0){
						fAnchor = ICharacterPairMatcher.RIGHT;
						return new Region(place, 1);
					}
				}
			}
		}
		
		fAnchor = -1;
		return null;
	}
	
	private char getPairCharacter(char c){
		if(c=='<') return '>';
		if(c=='>') return '<';
		if(c=='(') return ')';
		if(c==')') return '(';
		if(c=='{') return '}';
		if(c=='}') return '{';
		return 0;
	}
	
	public int getAnchor() {
		return fAnchor;
	}

}

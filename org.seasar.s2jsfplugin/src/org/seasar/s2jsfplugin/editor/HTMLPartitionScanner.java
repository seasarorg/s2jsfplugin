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

import org.eclipse.jface.text.rules.*;

/**
 * HTML/XMLファイル用のパーテーションスキャナ。
 * 
 * @author Naoki Takezoe
 */
public class HTMLPartitionScanner extends RuleBasedPartitionScanner {
	
	public final static String HTML_DEFAULT = "__html_default";
	public final static String HTML_COMMENT = "__html_comment";
	public final static String HTML_TAG     = "__html_tag";
	public final static String HTML_SCRIPT  = "__html_script";
	public final static String HTML_DOCTYPE = "__html_doctype";
	
	public HTMLPartitionScanner() {

		IToken htmlComment = new Token(HTML_COMMENT);
		IToken htmlTag     = new Token(HTML_TAG);
		IToken htmlScript  = new Token(HTML_SCRIPT);
		IToken htmlDoctype = new Token(HTML_DOCTYPE);

		IPredicateRule[] rules = new IPredicateRule[6];

		rules[0] = new MultiLineRule("<!--"      , "-->" , htmlComment);
		rules[1] = new MultiLineRule("<!DOCTYPE" , ">"   , htmlDoctype);
		rules[2] = new MultiLineRule("<%"        , "%>"  , htmlScript);
		rules[3] = new MultiLineRule("<?xml"     , "?>"  , htmlDoctype);
		rules[4] = new MultiLineRule("<![CDATA[" , "]]>" , htmlDoctype);
		rules[5] = new HTMLTagRule(htmlTag);
		
		setPredicateRules(rules);
	}
}

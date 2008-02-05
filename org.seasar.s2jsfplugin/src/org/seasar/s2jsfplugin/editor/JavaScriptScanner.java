/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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
import java.util.List;

import org.eclipse.jdt.internal.ui.text.JavaWordDetector;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WordRule;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.pref.ColorProvider;

/**
 * 
 * @author Naoki Takezoe
 */
public class JavaScriptScanner extends RuleBasedScanner {
	
	public static final String KEYWORDS[] = {
			"abstract",
			"boolean", "break", "byte",
			"case", "catch", "char", "class", "const", "continue",
			"default", "do", "double",
			"else", "extends",
			"false", "final", "finally", "float", "for", "function",
			"goto", "if", "implements", "import", "in", "instanceof", "int", "interface",
			"long",
			"native", "new", "null",
			"package", "private", "protected", "prototype", "public",
			"return", "short", "static", "super", "switch", "synchronized",
			"this", "throw", "throws", "transient", "true", "try",
			"var", "void", "while", "with"
	};
	
	public JavaScriptScanner(ColorProvider colorProvider){
		List rules = createRules(colorProvider);
		setRules((IRule[])rules.toArray(new IRule[rules.size()]));
	}
	
	/**
	 * Creates the list of <code>IRule</code>.
	 * If you have to customize rules, override this method.
	 * 
	 * @param colorProvider ColorProvider
	 * @return the list of <code>IRule</code>
	 */
	protected List createRules(ColorProvider colorProvider){
		IToken normal  = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_FG);
		IToken string  = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_JS_STRING);
		IToken comment = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_JS_COMMENT);
		IToken keyword = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_JS_KEYWORD);
		
		List rules = new ArrayList();
		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		rules.add(new SingleLineRule("'", "'", string, '\\'));
		rules.add(new EndOfLineRule("//", comment));
		
		WordRule wordRule = new WordRule(new JavaWordDetector(), normal);
		for(int i=0;i<KEYWORDS.length;i++){
			wordRule.addWord(KEYWORDS[i], keyword);
		}
		rules.add(wordRule);
		return rules;
	}
	
}

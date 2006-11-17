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

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.seasar.s2jsfplugin.pref.ColorProvider;

/**
 * 
 * @author Naoki Takezoe
 */
public class HTMLScanner extends RuleBasedScanner {

	public HTMLScanner(ColorProvider colorProvider) {
//		IToken procInstr = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_DOCTYPE);
		
		IRule[] rules = new IRule[1];
		rules[0] = new WhitespaceRule(new HTMLWhitespaceDetector());
		
		setRules(rules);
	}
}

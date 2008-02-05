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

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.pref.ColorProvider;

/**
 * <code>RuleBasedScanner</code> for the inner CSS in the HTML.
 * 
 * @author Naoki Takezoe
 * @see 2.0.3
 */
public class InnerCSSScanner extends CSSBlockScanner {

	public InnerCSSScanner(ColorProvider colorProvider) {
		super(colorProvider);
	}
	
	protected List createRules(ColorProvider colorProvider) {
		IToken tag = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_TAG);
		IToken comment = colorProvider.getToken(S2JSFPlugin.PREF_COLOR_CSS_COMMENT);
		
		List rules = new ArrayList();
		rules.add(new SingleLineRule("<style", ">", tag));
		rules.add(new SingleLineRule("</style", ">", tag));
		rules.add(new MultiLineRule("/*", "*/", comment));
		rules.addAll(super.createRules(colorProvider));
		
		return rules;
	}
}

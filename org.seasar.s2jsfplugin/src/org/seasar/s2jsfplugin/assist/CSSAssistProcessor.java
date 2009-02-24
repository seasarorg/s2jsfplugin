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
package org.seasar.s2jsfplugin.assist;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.resources.IFile;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.w3c.css.sac.AttributeCondition;
import org.w3c.css.sac.Condition;
import org.w3c.css.sac.ConditionalSelector;
import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SelectorList;
import org.w3c.css.sac.SimpleSelector;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import com.steadystate.css.parser.CSSOMParser;

/**
 * HTMLタグのclass属性を補完するための情報を管理するクラスです。
 * <pre>
 * &lt;style type=&quot;text/css&quot;&gt;
 * ...
 * &lt;/style&gt;
 * </pre>
 * でそのHTMLファイル内記述されたCSSと、
 * <pre>
 * &lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot; href=&quot;...&quot; /&gt;
 * </pre>
 * で指定された外部CSSで定義されているクラスを補完対象とします。
 * 
 * @author Naoki Takezoe
 */
public class CSSAssistProcessor {
	
	private HashMap rules = new HashMap();
	private IFile file;
	
	/**
	 * CSSからの補完情報を再読み込みします。
	 * 
	 * @param file ファイル
	 */
	public void reload(IFile file){
		this.file = file;
		rules.clear();
		try {
			FuzzyXMLDocument doc = new FuzzyXMLParser().parse(Util.readFile(file));
			if(doc!=null){
				processElement(doc.getDocumentElement());
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	private void processElement(FuzzyXMLElement element){
		if(element.getName().equalsIgnoreCase("link")){
			// linkタグで指定された外部CSS
			String rel  = "";
			String type = "";
			String href = "";
			FuzzyXMLAttribute[] attrs = element.getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(attrs[i].getName().equalsIgnoreCase("rel")){
					rel = attrs[i].getValue();
				} else if(attrs[i].getName().equalsIgnoreCase("type")){
					type = attrs[i].getValue();
				} else if(attrs[i].getName().equalsIgnoreCase("href")){
					href = attrs[i].getValue();
				}
			}
			if(rel.equalsIgnoreCase("stylesheet") && type.equalsIgnoreCase("text/css")){
				try {
					IFile css = getFile(href);
					if(css!=null && css.exists()){
						processStylesheet(Util.readFile(css));
					}
				} catch(Exception ex){
				}
			}
		} else if(element.getName().equalsIgnoreCase("style")){
			// styleタグで記述されたインラインCSS
			String type =  "";
			FuzzyXMLAttribute[] attrs = element.getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(attrs[i].getName().equalsIgnoreCase("type")){
					type = attrs[i].getValue();
				}
			}
			if(type.equalsIgnoreCase("text/css")){
				String text = Util.getXPathValue(element,"/");
				processStylesheet(text);
			}
		}
		FuzzyXMLNode[] children = element.getChildren();
		for(int i=0;i<children.length;i++){
			if(children[i] instanceof FuzzyXMLElement){
				processElement((FuzzyXMLElement)children[i]);
			}
		}
	}
	
	private IFile getFile(String path){
		// Webアプリケーションルートからのパス？
		if(path.startsWith("/")){
			return null;
		}
		// 相対パス
		return file.getProject().getFile(file.getParent().getProjectRelativePath().append(path));
	}
	
	/**
	 * 引数で渡されたCSSのソースをパースして補完情報を作成します。
	 * 
	 * @param css
	 */
	private void processStylesheet(String css){
		try {
			CSSOMParser parser = new CSSOMParser();
			InputSource is = new InputSource(new StringReader(css));
			CSSStyleSheet stylesheet = parser.parseStyleSheet(is);
			CSSRuleList list = stylesheet.getCssRules();
//			ArrayList assists = new ArrayList();
			for(int i=0;i<list.getLength();i++){
				CSSRule rule = list.item(i);
				if(rule instanceof CSSStyleRule){
					CSSStyleRule styleRule = (CSSStyleRule)rule;
					String selector = styleRule.getSelectorText();
					SelectorList selectors = parser.parseSelectors(new InputSource(new StringReader(selector)));
					for(int j=0;j<selectors.getLength();j++){
						Selector sel = selectors.item(j);
						if(sel instanceof ConditionalSelector){
							Condition cond = ((ConditionalSelector)sel).getCondition();
							SimpleSelector simple = ((ConditionalSelector)sel).getSimpleSelector();
							
							if(simple instanceof ElementSelector){
								String tagName = ((ElementSelector)simple).getLocalName();
								if(tagName==null){
									tagName = "*";
								} else {
									tagName = tagName.toLowerCase();
								}
								if(cond instanceof AttributeCondition){
									AttributeCondition attrCond = (AttributeCondition)cond;
									if(rules.get(tagName)==null){
										ArrayList classes = new ArrayList();
//										classes.add(new AssistInfo(attrCond.getValue()));
										classes.add(attrCond.getValue());
										rules.put(tagName,classes);
									} else {
										ArrayList classes = (ArrayList)rules.get(tagName);
//										classes.add(new AssistInfo(attrCond.getValue()));
										classes.add(attrCond.getValue());
									}
								}
							}
						}
					}
				}
			}
		} catch(Throwable ex){
			// java.lang.Error: Missing return statement in function が出るんですよねぇ…。
		}
	}
	
	/**
	 * タグのclass属性の補完候補を取得します。
	 * 
	 * @param tagName
	 * @return
	 */
	public AssistInfo[] getAssistInfo(String tagName,String value){
		try {
			if(value.indexOf(' ')!=-1){
				value = value.substring(0,value.lastIndexOf(' ')+1);
			} else {
				value = "";
			}
			
			ArrayList assists = new ArrayList();
			ArrayList all = (ArrayList)rules.get("*");
			if(all!=null){
				assists.addAll(all);
			}
			if(rules.get(tagName.toLowerCase())!=null){
				ArrayList list = (ArrayList)rules.get(tagName.toLowerCase());
				assists.addAll(list);
			}
			AssistInfo[] info = new AssistInfo[assists.size()];
			for(int i=0;i<assists.size();i++){
				String keyword = (String)assists.get(i);
				info[i] = new AssistInfo(value + keyword,keyword,
						S2JSFPlugin.getDefault().getImageRegistry().get(S2JSFPlugin.ICON_VALUE));
			}
			return info;
		} catch(Exception ex){
		}
		return new AssistInfo[0];
	}
}

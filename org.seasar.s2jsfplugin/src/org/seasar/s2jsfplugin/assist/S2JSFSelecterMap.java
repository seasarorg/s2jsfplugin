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

import java.util.ArrayList;
import java.util.HashMap;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.seasar.jsf.TagSelector;
import org.seasar.jsf.selector.AbstractTagSelector;
import org.seasar.jsf.selector.BaseSelector;
import org.seasar.jsf.selector.CommandButtonSelector;
import org.seasar.jsf.selector.CommandLinkSelector;
import org.seasar.jsf.selector.FormSelector;
import org.seasar.jsf.selector.InputHiddenSelector;
import org.seasar.jsf.selector.InputSecretSelector;
import org.seasar.jsf.selector.InputTextSelector;
import org.seasar.jsf.selector.InputTextareaSelector;
import org.seasar.jsf.selector.OutputLinkSelector;
import org.seasar.jsf.selector.OutputTextSelector;
import org.seasar.jsf.selector.SelectBooleanCheckboxSelector;
import org.seasar.jsf.selector.SelectItemSelector;
import org.seasar.jsf.selector.SelectManyCheckboxSelector;
import org.seasar.jsf.selector.SelectOneMenuSelector;
import org.seasar.jsf.selector.SelectOneRadioSelector;
import org.seasar.jsf.selector.TitleSelector;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.xml.sax.helpers.AttributesImpl;

/**
 * HTMLタグとJSFタグのマッピングを管理するクラス。
 * 
 * @author Naoki Takezoe
 */
public class S2JSFSelecterMap {

	private static HashMap selecterMap = new HashMap();
	private static ArrayList selecter = new ArrayList();
	
	static {
		selecter.add(new SelectManyCheckboxSelector());
		selecter.add(new SelectOneRadioSelector());
		selecter.add(new SelectItemSelector());
//		selecter.add(new InjectSelector());
//		selecter.add(new HtmlSelector());
//		selecter.add(new MetaContentTypeSelector());
		selecter.add(new CommandButtonSelector());
		selecter.add(new CommandLinkSelector());
		selecter.add(new OutputTextSelector());
		selecter.add(new OutputLinkSelector());
		selecter.add(new FormSelector());
		selecter.add(new BaseSelector());
		selecter.add(new InputTextSelector());
		selecter.add(new InputHiddenSelector());
		selecter.add(new InputSecretSelector());
		selecter.add(new InputTextareaSelector());
		selecter.add(new SelectBooleanCheckboxSelector());
		selecter.add(new SelectOneMenuSelector());
		selecter.add(new SelectItemSelector());
//		selecter.add(new ElementSelector());
		selecter.add(new TitleSelector());
		
		selecterMap.put(SelectManyCheckboxSelector.class,new String[]{S2JSFPlugin.HTML_URI,"selectManyCheckbox"});
		selecterMap.put(SelectOneRadioSelector.class,new String[]{S2JSFPlugin.HTML_URI,"selectOneRadio"});
		selecterMap.put(SelectItemSelector.class,new String[]{S2JSFPlugin.HTML_URI,"selectItem"});
//		selecterMap.put(InjectSelector.class,"");
//		selecterMap.put(HtmlSelector.class,"");
//		selecterMap.put(MetaContentTypeSelector.class,"");
		selecterMap.put(CommandButtonSelector.class,new String[]{S2JSFPlugin.HTML_URI,"commandButton"});
		selecterMap.put(CommandLinkSelector.class,new String[]{S2JSFPlugin.HTML_URI,"commandLink"});
		selecterMap.put(OutputTextSelector.class,new String[]{S2JSFPlugin.HTML_URI,"outputText"});
		selecterMap.put(OutputLinkSelector.class,new String[]{S2JSFPlugin.HTML_URI,"outputLink"});
		selecterMap.put(FormSelector.class,new String[]{S2JSFPlugin.HTML_URI,"form"});
		selecterMap.put(BaseSelector.class,new String[]{S2JSFPlugin.S2JSF_URI,"base"});
		selecterMap.put(InputTextSelector.class,new String[]{S2JSFPlugin.HTML_URI,"inputText"});
		selecterMap.put(InputHiddenSelector.class,new String[]{S2JSFPlugin.HTML_URI,"inputHidden"});
		selecterMap.put(InputSecretSelector.class,new String[]{S2JSFPlugin.HTML_URI,"inputSecret"});
		selecterMap.put(InputTextareaSelector.class,new String[]{S2JSFPlugin.HTML_URI,"inputTextarea"});
		selecterMap.put(SelectBooleanCheckboxSelector.class,new String[]{S2JSFPlugin.HTML_URI,"booleanCheckbox"});
		selecterMap.put(SelectOneMenuSelector.class,new String[]{S2JSFPlugin.HTML_URI,"selectOneMenu"});
		selecterMap.put(SelectItemSelector.class,new String[]{S2JSFPlugin.HTML_URI,"selectItem"});
//		selecter.add(new ElementSelector());
		selecterMap.put(TitleSelector.class,new String[]{S2JSFPlugin.S2JSF_URI,"title"});
	}
	
	public static String[] getTagName(S2JSFProject project,FuzzyXMLElement element, String mayaPrefix){
		if(mayaPrefix!=null && element.hasAttribute(mayaPrefix + ":inject")){
			String value = element.getAttributeValue(mayaPrefix + ":inject");
			int index = value.indexOf(":");
			if(index >= 0){
				String[] dim = value.split(":");
				return new String[]{Util.getPrefixURI(element, dim[0]), dim[1]};
			}
		}
		TagSelector selector = getSelector(project,element,mayaPrefix);
		if(selector==null){
			return null;
		}
		return (String[])selecterMap.get(selector.getClass());
	}
	
	private static TagSelector getSelector(S2JSFProject project,FuzzyXMLElement element, String mayaPrefix){
		// タグセレクタに渡すためのAttributeオブジェクトを準備
		AttributesImpl saxAttrs = new AttributesImpl();
		FuzzyXMLAttribute[] attrs = element.getAttributes();
		for(int i=0;i<attrs.length;i++){
			String attrName = attrs[i].getName();
			if(mayaPrefix!=null && attrName.startsWith(mayaPrefix + ":")){
				String[] dim = attrName.split(":");
				if(dim.length > 1){
					saxAttrs.addAttribute(S2JSFPlugin.MAYA_URI,dim[1],attrName,"",attrs[i].getValue());
					continue;
				}
			}
			saxAttrs.addAttribute("",attrName,attrName,"",attrs[i].getValue());
		}
		
		// セレクタを引き当てる
		for(int i=0;i<selecter.size();i++){
			TagSelector sel = (TagSelector)selecter.get(i);
			if(sel instanceof AbstractTagSelector){
				((AbstractTagSelector)sel).setJsfConfig(project.getJsfConfig());
			}
			if(sel.isSelectable("",element.getName(),element.getName(),saxAttrs)){
				return sel;
			}
		}
		return null;
	}
}

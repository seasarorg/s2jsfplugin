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
package org.seasar.s2jsfplugin.assist;

import java.util.HashMap;

/**
 * JSFのカスタムタグの情報を定義します。
 * 
 * @author Naoki Takezoe
 */
public class JSFTagDefinition {
	
	public static final String PROPERTY = "prop";
	public static final String ACTION = "action";
	public static final String ACTION_LISTENER = "actionListener";
	public static final String CHANGE_LISTENER = "valudaChangeListener";
	public static final String VALIDATER = "validater";
	public static final String VALUE = "value";
	
	private static HashMap map = new HashMap();
	
	static {
		HashMap commandButton = new HashMap();
		commandButton.put("action",ACTION);
		commandButton.put("binding",PROPERTY);
		commandButton.put("actionListener",ACTION_LISTENER);
		map.put("commandButton",commandButton);
		
		HashMap commandLink = new HashMap();
		commandLink.put("action",ACTION);
		commandLink.put("binding",PROPERTY);
		commandLink.put("actionListener",ACTION_LISTENER);
		map.put("commandLink",commandLink);
		
		HashMap inputText = new HashMap();
		inputText.put("value",PROPERTY);
		inputText.put("binding",PROPERTY);
		inputText.put("converter",PROPERTY);
		inputText.put("validator",VALIDATER);
		inputText.put("valueChangeListener",CHANGE_LISTENER);
		map.put("inputText",inputText);
		
		HashMap outputText = new HashMap();
		outputText.put("value",PROPERTY);
		outputText.put("binding",PROPERTY);
		outputText.put("converter",PROPERTY);
		map.put("outputText",outputText);
		
		HashMap inputTextarea = new HashMap();
		inputTextarea.put("value",PROPERTY);
		inputTextarea.put("binding",PROPERTY);
		inputTextarea.put("converter",PROPERTY);
		inputTextarea.put("validator",VALIDATER);
		inputTextarea.put("valueChangeListener",CHANGE_LISTENER);
		map.put("inputTextarea",inputTextarea);
		
		HashMap inputSecret = new HashMap();
		inputSecret.put("value",PROPERTY);
		inputSecret.put("binding",PROPERTY);
		inputSecret.put("converter",PROPERTY);
		inputSecret.put("validator",VALIDATER);
		inputSecret.put("valueChangeListener",CHANGE_LISTENER);
		map.put("inputSecret",inputSecret);
		
		HashMap inputHidden = new HashMap();
		inputHidden.put("value",PROPERTY);
		inputHidden.put("binding",PROPERTY);
		inputHidden.put("converter",PROPERTY);
		inputHidden.put("validator",VALIDATER);
		inputHidden.put("valueChangeListener",CHANGE_LISTENER);
		map.put("inputHidden",inputHidden);
		
		HashMap outputLabel = new HashMap();
		outputLabel.put("value",PROPERTY);
		outputLabel.put("binding",PROPERTY);
		outputLabel.put("converter",PROPERTY);
		map.put("outputLabel",outputLabel);
		
		HashMap outputFormat = new HashMap();
		outputFormat.put("value",PROPERTY);
		outputFormat.put("binding",PROPERTY);
		outputFormat.put("converter",PROPERTY);
		map.put("outputFormat",outputFormat);
		
		HashMap graphicImage = new HashMap();
		graphicImage.put("value",PROPERTY);
		graphicImage.put("binding",PROPERTY);
		map.put("graphicImage",graphicImage);
		
		HashMap selectItem = new HashMap();
		selectItem.put("value",PROPERTY);
		selectItem.put("binding",PROPERTY);
		map.put("selectItem",selectItem);
		
		HashMap selectItems = new HashMap();
		selectItems.put("value",PROPERTY);
		selectItems.put("binding",PROPERTY);
		map.put("selectItems",selectItems);
		
		HashMap selectOneRadio = new HashMap();
		selectOneRadio.put("value",PROPERTY);
		selectOneRadio.put("binding",PROPERTY);
		selectOneRadio.put("converter",PROPERTY);
		selectOneRadio.put("validator",VALIDATER);
		selectOneRadio.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectOneRadio",selectOneRadio);
		
		HashMap selectOneRadio2 = new HashMap();
		selectOneRadio2.put("value",PROPERTY);
		selectOneRadio2.put("binding",PROPERTY);
		selectOneRadio2.put("converter",PROPERTY);
		selectOneRadio2.put("validator",VALIDATER);
		selectOneRadio2.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectOneRadio2",selectOneRadio2);
		
		HashMap selectOneMenu = new HashMap();
		selectOneMenu.put("value",PROPERTY);
		selectOneMenu.put("binding",PROPERTY);
		selectOneMenu.put("converter",PROPERTY);
		selectOneMenu.put("validator",VALIDATER);
		selectOneMenu.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectOneMenu",selectOneMenu);
		
		HashMap selectOneListbox = new HashMap();
		selectOneListbox.put("value",PROPERTY);
		selectOneListbox.put("binding",PROPERTY);
		selectOneListbox.put("converter",PROPERTY);
		selectOneListbox.put("validator",VALIDATER);
		selectOneListbox.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectOneListbox",selectOneListbox);
		
		HashMap selectBooleanCheckbox = new HashMap();
		selectBooleanCheckbox.put("value",PROPERTY);
		selectBooleanCheckbox.put("binding",PROPERTY);
		selectBooleanCheckbox.put("converter",PROPERTY);
		selectBooleanCheckbox.put("validator",VALIDATER);
		selectBooleanCheckbox.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectBooleanCheckbox",selectBooleanCheckbox);
		
		HashMap selectManyCheckbox = new HashMap();
		selectManyCheckbox.put("value",PROPERTY);
		selectManyCheckbox.put("binding",PROPERTY);
		selectManyCheckbox.put("converter",PROPERTY);
		selectManyCheckbox.put("validator",VALIDATER);
		selectManyCheckbox.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectManyCheckbox",selectManyCheckbox);
		
		HashMap selectManyMenu = new HashMap();
		selectManyMenu.put("value",PROPERTY);
		selectManyMenu.put("binding",PROPERTY);
		selectManyMenu.put("converter",PROPERTY);
		selectManyMenu.put("validator",VALIDATER);
		selectManyMenu.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectManyMenu",selectManyMenu);
		
		HashMap selectManyListbox = new HashMap();
		selectManyListbox.put("value",PROPERTY);
		selectManyListbox.put("binding",PROPERTY);
		selectManyListbox.put("converter",PROPERTY);
		selectManyListbox.put("validator",VALIDATER);
		selectManyListbox.put("valueChangeListener",CHANGE_LISTENER);
		map.put("selectManyListbox",selectManyListbox);
		
	}
	
	/**
	 * 引数で渡されたタグがJSFのタグかどうかを判定します。
	 * 
	 * @param jsfTagName JSFのカスタムタグのタグ名
	 * @return 引数で渡されたタグがJSFタグの場合true、そうでない場合false
	 */
	public static boolean hasTagInfo(String jsfTagName){
		if(map.get(jsfTagName)==null){
			return false;
		}
		return true;
	}
	
	/**
	 * JSFタグの属性情報を取得します。
	 * 
	 * @param jsfTagName JSFカスタムタグのタグ名
	 * @param attrName 属性名
	 * @return 
	 *   <ul>
	 *     <li>PROPERTY</li>
	 *     <li>ACTION</li>
	 *     <li>ACTION_LISTENER</li>
	 *     <li>CHANGE_LISTENER</li>
	 *     <li>VALIDATER</li>
	 *     <li>VALUE</li>
	 *   </ul>
	 */
	public static String getAttributeInfo(String jsfTagName,String attrName){
		if(map.get(jsfTagName)==null){
			return VALUE;
		}
		HashMap tagInfo = (HashMap)map.get(jsfTagName);
		if(tagInfo.get(attrName)==null){
			return VALUE;
		}
		return (String)tagInfo.get(attrName);
	}
	
}

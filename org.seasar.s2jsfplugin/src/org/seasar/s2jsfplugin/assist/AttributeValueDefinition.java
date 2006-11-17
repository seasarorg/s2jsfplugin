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

import java.util.Arrays;
import java.util.HashMap;

import org.seasar.s2jsfplugin.model.AttributeInfo;

/**
 * HTMLタグの属性値情報を定義します。
 * 
 * @author Naoki Takezoe
 */
public class AttributeValueDefinition {
	
	/** align属性の属性値候補 */
	private static final String[] align = {
		"left","center","right"
	};
	
	/** valign属性の属性値候補 */
	private static final String[] valign = {
		"top","middle","bottom"
	};
	
	/** input type属性の属性値候補 */
	private static final String[] inputType = {
		"text","password","hidden","checkbox",
		"radio","button","reset","submit","file"
	};
	
	private static HashMap map = new HashMap();
	
	static {
		addAttributeValues(AttributeInfo.ALIGN,align);
		addAttributeValues(AttributeInfo.VALIGN,valign);
		addAttributeValues(AttributeInfo.INPUT_TYPE,inputType);
	}
	
	private static void addAttributeValues(int type,String[] values){
		Arrays.sort(values);
		map.put(new Integer(type),values);
	}
	
	public static String[] getAttributeValues(int type){
		Integer key = new Integer(type);
		if(map.get(key)==null){
			return new String[0];
		}
		return (String[])map.get(key);
	}
	
}

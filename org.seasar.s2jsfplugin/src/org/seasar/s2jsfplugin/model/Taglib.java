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
package org.seasar.s2jsfplugin.model;

/**
 * カスタムタグライブラリの情報（プレフィックスとURI）を格納するクラス。
 * 
 * @author Naoki Takezoe
 */
public class Taglib {
	
	private String prefix;
	private String uri;
	
	public Taglib(String prefix,String uri){
		this.prefix = prefix;
		this.uri    = uri;
	}
	
	/**
	 * @return prefix を戻します。
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * @return uri を戻します。
	 */
	public String getUri() {
		return uri;
	}
}

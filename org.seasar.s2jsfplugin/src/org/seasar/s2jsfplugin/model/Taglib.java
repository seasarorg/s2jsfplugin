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
package org.seasar.s2jsfplugin.model;

/**
 * �J�X�^���^�O���C�u�����̏��i�v���t�B�b�N�X��URI�j���i�[����N���X�B
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
	 * @return prefix ��߂��܂��B
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * @return uri ��߂��܂��B
	 */
	public String getUri() {
		return uri;
	}
}

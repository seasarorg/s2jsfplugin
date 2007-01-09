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
package org.seasar.s2jsfplugin.model.autoregister;

import java.util.HashMap;

import jp.aonir.fuzzyxml.FuzzyXMLElement;

/**
 * IAutoRegister�̃t�@�N�g���B
 * 
 * @author Naoki Takezoe
 */
public class AutoRegisterFactory {
	
	private static HashMap autoRegisters = new HashMap();
	static {
		autoRegisters.put(
				"org.seasar.framework.container.autoregister.FileSystemComponentAutoRegister",
				new FileSystemAutoRegister());
		autoRegisters.put(
				"org.seasar.framework.container.autoregister.JarComponentAutoRegister",
				new JarAutoRegister());
		autoRegisters.put(
				"org.seasar.framework.container.autoregister.ComponentAutoRegister",
				new ComponentAutoRegister());
	}
	
	/**
	 * �����œn�����N���X���ɑΉ�����IAutoRegister�����݂��邩�ǂ����𔻒肵�܂��B
	 * 
	 * @param className dicon�t�@�C���ɓo�^����Ă���N���X��
	 * @return IAutoRegister�����݂���ꍇtrue�A���݂��Ȃ��ꍇfalse
	 */
	public static boolean isAutoRegister(String className){
		return autoRegisters.containsKey(className);
	}
	
	/**
	 * IAutoRegister�̃C���X�^���X���擾���܂��B
	 * 
	 * @param className dicon�t�@�C���ɓo�^����Ă���N���X��
	 * @param element component�v�f��FuzzyXMLElement�I�u�W�F�N�g
	 * @return IAutoRegister�̃C���X�^���X
	 */
	public static IAutoRegister getAutoRegister(String className, FuzzyXMLElement element){
		IAutoRegister register = (IAutoRegister)autoRegisters.get(className);
		register.init(element);
		return register;
	}
	
}

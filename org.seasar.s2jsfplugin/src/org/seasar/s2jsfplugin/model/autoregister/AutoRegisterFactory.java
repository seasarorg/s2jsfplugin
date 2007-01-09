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
 * IAutoRegisterのファクトリ。
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
	 * 引数で渡したクラス名に対応するIAutoRegisterが存在するかどうかを判定します。
	 * 
	 * @param className diconファイルに登録されているクラス名
	 * @return IAutoRegisterが存在する場合true、存在しない場合false
	 */
	public static boolean isAutoRegister(String className){
		return autoRegisters.containsKey(className);
	}
	
	/**
	 * IAutoRegisterのインスタンスを取得します。
	 * 
	 * @param className diconファイルに登録されているクラス名
	 * @param element component要素のFuzzyXMLElementオブジェクト
	 * @return IAutoRegisterのインスタンス
	 */
	public static IAutoRegister getAutoRegister(String className, FuzzyXMLElement element){
		IAutoRegister register = (IAutoRegister)autoRegisters.get(className);
		register.init(element);
		return register;
	}
	
}

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
package org.seasar.s2jsfplugin.model.autoregister;

import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.jdt.core.IJavaProject;
import org.seasar.framework.container.autoregister.AutoNaming;
import org.seasar.s2jsfplugin.model.ManagedBean;


/**
 * S2のAutoRegisterを再現するためのインターフェース。
 * 
 * @author Naoki Takezoe
 */
public interface IAutoRegister {
	
	/**
	 * IAutoRegisterを初期化します。
	 * @param e AutoRegisterのcomponent要素に該当するFuzzyXMLElementオブジェクト
	 */
	public void init(FuzzyXMLElement e);
	
	/**
	 * このIAutoRegisterによって登録されたマネージド・ビーンを取得します。
	 * @return マネージド・ビーン
	 */
	public ManagedBean[] getRegisteredBeans();
	
	/**
	 * Javaプロジェクトを設定します。
	 * @param project Javaプロジェクト
	 */
	public void setProject(IJavaProject project);
	
	/**
	 * 自動登録を実行します。
	 * <p>
	 * このメソッドを呼び出したあとに<code>getRegisteredBeans()</code>
	 * で自動登録されたマネージド・ビーンを取得することができます。
	 */
	public void registerAll();
	
	/**
	 * 登録するクラスパターンを追加します。
	 * 
	 * @param packageName パッケージ名
	 * @param shortClassNames クラス名のパターン
	 */
	public void addClassPattern(String packageName, String shortClassNames);
	
	/**
	 * 無視するクラスパターンを追加します。
	 * 
	 * @param packageName パッケージ名
	 * @param shortClassNames クラス名のパターン
	 */
	public void addIgnoreClassPattern(String packageName, String shortClassNames);
	
	/**
	 * 自動登録時のコンポーネント名の命名規則を設定します。
	 * 
	 * @param naming マネージド・ビーンの自動命名を行うオブジェクト
	 */
	public void setAutoNaming(AutoNaming naming);
	
	/**
	 * 自動登録されるマネージド・ビーンのスコープを設定します。
	 * 
	 * @param scope requestまたはsession
	 */
	public void setScope(String scope);
}

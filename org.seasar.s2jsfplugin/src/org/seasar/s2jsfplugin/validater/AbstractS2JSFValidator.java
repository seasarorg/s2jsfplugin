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
package org.seasar.s2jsfplugin.validater;

import org.eclipse.core.resources.IFile;
import org.seasar.s2jsfplugin.S2JSFPlugin;
import org.seasar.s2jsfplugin.Util;
import org.seasar.s2jsfplugin.model.S2JSFProject;
import org.seasar.s2jsfplugin.pref.S2JSFProjectParams;

/**
 * S2JSFプラグインで使用するバリデータの抽象基底クラス。
 * 
 * @author Naoki Takezoe
 */
public abstract class AbstractS2JSFValidator {
	
	private S2JSFProject project;
	private IFile file;
	private String contents;
	private S2JSFProjectParams params;
	
	public AbstractS2JSFValidator(S2JSFProject project,IFile file){
		this.project = project;
		this.file    = file;
		
		try {
			this.contents = Util.readFile(file);
			this.contents = contents.replaceAll("\r\n"," \n");
			this.contents = contents.replaceAll("\r"  ,"\n");
			
			this.params = new S2JSFProjectParams(file.getProject());
			
		} catch(Exception ex){
			Util.logException(ex);
		}
	}
	
	/** 検証処理を実装します。 */
	public abstract void doValidate();
	
	/**
	 * メッセージを作成します。
	 * 
	 * @param key メッセージのキー
	 * @param param メッセージに埋め込むパラメータ
	 * @return 作成されたメッセージ
	 */
	protected String createMessage(String key,String param){
		String message = S2JSFPlugin.getResourceString(key);
		return Util.createMessage(message,new String[]{param});
	}
	
	/** オフセットから行番号を取得します。 */
	protected int getLineAtOffset(int offset){
		String text = contents.substring(0,offset);
		return text.split("\n").length;
	}
	
	/** 属性のオフセットから属性値部分のオフセットを取得します。 */
	protected int getAttrValueOffset(int offset){
		int valueStart = 0;
		
		valueStart = contents.indexOf("\"",offset);
		if(valueStart==-1){
			valueStart = contents.indexOf("=")+1;
		}
		
		return valueStart;
	}
	
	/** S2JSFProjectを取得します。 */
	protected S2JSFProject getS2JSFProject(){
		return this.project;
	}
	
	/** 検証対象のファイルオブジェクトを取得します。 */
	protected IFile getFile(){
		return this.file;
	}
	
	/** S2JSFProjectParamsを取得します。 */
	protected S2JSFProjectParams getS2JSFProjectParams(){
		return this.params;
	}
}

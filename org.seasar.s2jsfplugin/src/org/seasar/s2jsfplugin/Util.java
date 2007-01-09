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
package org.seasar.s2jsfplugin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.XPath;
import jp.aonir.fuzzyxml.internal.FuzzyXMLUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.seasar.s2jsfplugin.model.ManagedBean;
import org.seasar.s2jsfplugin.model.ManagedBeanProperty;
import org.seasar.s2jsfplugin.model.S2JSFProject;


/**
 * ユーティリティメソッドを提供します。
 * 
 * @author Naoki Takezoe
 */
public class Util {
	
	/**
	 * HTMLタグのエスケープを行います。以下の変換を行います。
	 * <p>
	 * また、引数としてnullが渡された場合は空文字列に変換して返却します。
	 * </p>
	 * @param str 文字列
	 * @return 変換後の文字列
	 */
	public static String escapeHTML(String str){
		if(str==null){
			return "";
		}
		return FuzzyXMLUtil.escape(str, true);
	}
	
	/**
	 * IFileからテキストファイルの内容を読み込み、文字列として返却します。
	 * 
	 * @param file 読み込むIFileオブジェクト
	 * @return 引数で指定したIFileオブジェクトの内容
	 * @throws IOException ファイル入出力例外
	 * @throws CoreException IFileから入力ストリームや文字コードの取得に失敗した場合
	 */
	public static String readFile(IFile file) throws IOException, CoreException {
		InputStream in = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			in = file.getContents();
			int len = 0;
			byte[] buf = new byte[1024 * 8];
			while((len = in.read(buf))!=-1){
				out.write(buf,0,len);
			}
			byte[] result = out.toByteArray();
			return new String(result, file.getCharset());
			
		} finally {
			if(in!=null){
				in.close();
			}
		}
	}
	
	/**
	 * FuzzyXMLのXPath#getValueをラップします。FuzzyXMLのXPath#getValueメソッドとは以下の点が異なります。
	 * 
	 * <ul>
	 *   <li>例外発生時はnullを返します。</li>
	 *   <li>文字列をトリムして返します。</li>
	 * </ul>
	 * 
	 * @param element ルートとするFuzzyXMLのエレメントオブジェクト
	 * @param xpath XPath
	 * @return XPathの結果値
	 */
	public static String getXPathValue(FuzzyXMLElement element,String xpath){
		try {
			String value = (String)XPath.getValue(element,xpath);
			return value.trim();
		} catch(Exception ex){
			return null;
		}
	}
	
	/**
	 * FuzzyXMLのXPath#selectSingleNodeをラップします。例外発生時にはthrowせず、nullを返却します。
	 * 
	 * @param element ルートとするFuzzyXMLのエレメントオブジェクト
	 * @param xpath XPath
	 * @return XPathで選択されたノード
	 */
	public static FuzzyXMLNode selectXPathNode(FuzzyXMLElement element,String xpath){
		try {
			return XPath.selectSingleNode(element,xpath);
		} catch(Exception ex){
			return null;
		}
	}
	
	/**
	 * 文字列リテラルをデコードします。
	 * 
	 * @param value 文字列リテラル
	 * @return デコード後の文字列
	 */
	public static String decodeString(String value){
		value = value.replaceAll("(^\"|\"$)","");
		value = value.replaceAll("\\\"","\"");
		return value;
	}
	
	/**
	 * 引数で渡した文字列が文字列リテラルかどうかをチェックします。
	 * 文字列がダブルクォートで開始し、ダブルクォートで終了している場合にtrueを返却します。
	 * 
	 * @param value チェック対象の文字列
	 * @return 文字列リテラルの場合true、そうでない場合false
	 */
	public static boolean isString(String value){
		if(value==null){
			return false;
		}
		if(value.startsWith("\"") && value.endsWith("\"")){
			return true;
		}
		return false;
	}
	
	/**
	 * EclipseのIFileオブジェクトからjava.io.Fileオブジェクトを取得します。
	 * 
	 * @param file IFileオブジェクト
	 * @return java.io.Fileオブジェクト
	 */
	public static File getFile(IFile file){
		return file.getLocation().makeAbsolute().toFile();
	}
	
	/**
	 * テンプレートとパラメータからメッセージを作成します。
	 * メッセージに含まれる{0}{1}…をパラメータで置換した文字列を返却します。
	 * 
	 * @param message メッセージ
	 * @param params  パラメータ
	 * @return 作成されたメッセージ
	 */
	public static String createMessage(String message,String[] params){
		for(int i=0;i<params.length;i++){
			message = message.replaceAll("\\{"+i+"\\}",params[i]);
		}
		return message;
	}
	
	/**
	 * FuzzyXMLElementオブジェクトからプレフィックスのURIを取得します。
	 * 
	 * @param element FuzzyXMLElementオブジェクト
	 * @param prefix プレフィックス
	 * @return プレフィックスに対応するURI。見つからない場合はnullを返します。
	 */
	public static String getPrefixURI(FuzzyXMLElement element,String prefix){
		try {
			FuzzyXMLAttribute[] attrs = element.getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(attrs[i].getName().startsWith("xmlns:" + prefix)){
					return attrs[i].getValue();
				}
			}
			FuzzyXMLElement parent = (FuzzyXMLElement)element.getParentNode();
			if(parent!=null){
				return getPrefixURI(parent,prefix);
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
		return null;
	}
	
	/**
	 * HTMLからMayaのプレフィックスを取得します。
	 * 
	 * @param element FuzzyXMLElementオブジェクト
	 * @return Mayaのプレフィックス。見つからなかった場合はnullを返します。
	 */
	public static String getMayaPrefix(FuzzyXMLElement element){
		try {
			FuzzyXMLAttribute[] attrs = element.getAttributes();
			for(int i=0;i<attrs.length;i++){
				if(attrs[i].getName().startsWith("xmlns:")){
					if(attrs[i].getValue().equals(S2JSFPlugin.MAYA_URI)){
						String[] dim = attrs[i].getName().split(":");
						if(dim.length > 1){
							return dim[1];
						}
					}
				}
			}
			FuzzyXMLElement parent = (FuzzyXMLElement)element.getParentNode();
			if(parent!=null){
				return getMayaPrefix(parent);
			}
		} catch(Exception ex){
			Util.logException(ex);
		}
		return null;
	}
	
	/**
	 * CoreExceptionを投げるためのユーティリティメソッド
	 * 
	 * @param message メッセージ
	 * @throws CoreException 引数のメッセージを使用して生成されたCoreException。
	 */
	public static void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,
				S2JSFPlugin.S2JSF_PLUGIN_ID,
				IStatus.OK,
				message, null);
		throw new CoreException(status);
	}
	
	/**
	 * 例外ログを出力します。
	 * 
	 * @param ex ログに出力する例外
	 */
	public static void logException(Exception ex){
		IStatus status = null;
		if(ex instanceof CoreException){
			status = ((CoreException)ex).getStatus();
		} else {
			status = new Status(IStatus.ERROR,S2JSFPlugin.S2JSF_PLUGIN_ID,0,ex.toString(),ex);
		}
		S2JSFPlugin.getDefault().getLog().log(status);
		// TODO デバッグ出力
		ex.printStackTrace();
	}
	
	////////////////////////////////////////////////////////////////////////
	// マーカー関係のユーティリティメソッド
	////////////////////////////////////////////////////////////////////////
	
	/**
	 * リソースの全てのマーカを削除します。
	 * 
	 * @param resouce リソース
	 */
	public static void removeMakers(IResource resouce) throws CoreException {
		resouce.deleteMarkers(IMarker.PROBLEM,false,0);
	}
	
	/**
	 * リソースにエラーマーカを作成します。
	 * 
	 * @param resource リソースe
	 * @param start    開始オフセット
	 * @param end      終了オフセット
	 * @param line     行番号
	 * @param message  メッセージ
	 */
	public static void createErrorMarker(IResource resource,int start,int end,int line,String message) throws CoreException {
		IMarker marker = resource.createMarker(IMarker.PROBLEM);
		Map map = new HashMap();
		map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
		map.put(IMarker.MESSAGE, message);
		map.put(IMarker.LINE_NUMBER,new Integer(line));
		map.put(IMarker.CHAR_START,new Integer(start));
		map.put(IMarker.CHAR_END,new Integer(end));
		marker.setAttributes(map);
	}
	
	/**
	 * リソースに警告マーカを作成します。
	 * 
	 * @param resource リソースe
	 * @param start    開始オフセット
	 * @param end      終了オフセット
	 * @param line     行番号
	 * @param message  メッセージ
	 */
	public static void createWarnMarker(IResource resource,int start,int end,int line,String message) throws CoreException {
		IMarker marker = resource.createMarker(IMarker.PROBLEM);
		Map map = new HashMap();
		map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_WARNING));
		map.put(IMarker.MESSAGE, message);
		map.put(IMarker.LINE_NUMBER,new Integer(line));
		map.put(IMarker.CHAR_START,new Integer(start));
		map.put(IMarker.CHAR_END,new Integer(end));
		marker.setAttributes(map);
	}
	
	////////////////////////////////////////////////////////////////////////
	// 補完関係のユーティリティメソッド
	////////////////////////////////////////////////////////////////////////
	
	/**
	 * キャレット位置の単語など、補完に必要な情報を取得します。
	 * 
	 * @param text テキスト
	 * @param offset キャレットのオフセット
	 * @return
	 * <ul>
	 *   <li>0 - カーソル位置の直近の単語（タグの場合は&lt;を含む）</li>
	 *   <li>1 - 属性補完のターゲット（&lt;を含まないタグ名のみ）</li>
	 *   <li>2 - 閉じタグ補完のターゲット（&lt;を含まないタグ名のみ）</li>
	 *   <li>3 - 直前の属性名</li>
	 * </ul>
	 */
	public static String[] getWordsForCompletion(String text,int offset) {
		text = text.substring(0,offset);
		
		StringBuffer sb = new StringBuffer();
		Stack  stack   = new Stack();
		String word    = "";
		String prevTag = "";
		String lastTag = "";
		String attr    = "";
		String temp1   = ""; // テンポラリ
		String temp2   = ""; // テンポラリ
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			// XML宣言は飛ばす
			if(c=='<' && text.length()>i+1 && text.charAt(i+1)=='?'){
				i = text.indexOf("?>",i+2);
				if(i==-1){
					i = text.length();
				}
				continue;
			}
			if (isDelimiter(c)) {
				temp1 = sb.toString();
				// 属性値のパース中の空白文字列は区切りとみなさない
				if(temp1.length()>1 && 
						((temp1.startsWith("\"") && !temp1.endsWith("\"") && c!='"') || 
								(temp1.startsWith("'") && !temp1.endsWith("'") && c!='\''))){
					sb.append(c);
					continue;
				}
				
				if(!temp1.equals("")){
					temp2 = temp1;
					if(temp2.endsWith("=") && !prevTag.equals("") && !temp2.equals("=")){
						attr = temp2.substring(0,temp2.length()-1);
					}
				}
				if(temp1.startsWith("<") && !temp1.startsWith("</") && !temp1.startsWith("<!")){
					prevTag = temp1.substring(1);
					if(!temp1.endsWith("/")){
						stack.push(prevTag);
					}
				} else if(temp1.startsWith("</") && stack.size()!=0){
					stack.pop();
				} else if(temp1.endsWith("/") && stack.size()!=0){
					stack.pop();
				}
				sb.setLength(0);
				
				if(c=='<'){
					sb.append(c);
				} else if(c=='"' || c=='\''){
					if(temp1.startsWith("\"") || temp1.startsWith("'")){
						sb.append(temp1);
					}
					sb.append(c);
				} else if(c=='>'){
					prevTag = "";
					attr    = "";
				}
			} else {
				if(c=='=' && !prevTag.equals("")){
					attr = temp2;
				}
				temp1 = sb.toString();
				if(temp1.length()>1 &&
						(temp1.startsWith("\"") && temp1.endsWith("\"")) || 
						(temp1.startsWith("'") && temp1.endsWith("'"))){
					sb.setLength(0);
				}
				sb.append(c);
			}
		}
		
		if(stack.size()!=0){
			lastTag = (String)stack.pop();
		}
		
		word = sb.toString();
		
		return new String[]{word,prevTag,lastTag,attr};
	}
	
	/**
	 * 文字が単語の区切り文字であるかどうかを判定します。
	 * <ul>
	 *   <li>半角スペース, タブ, 改行(\rもしくは\n)</li>
	 *   <li>ダブルクオート,  シングルクォート, カンマ, ドット, セミコロン</li>
	 *   <li>(, ), [, ], &lt;, &gt;, +, *</li>
	 * </ul>
	 * 
	 * @param c 文字
	 * @return 区切り文字の場合true、区切り文字ではない場合false
	 */
	private static boolean isDelimiter(char c) {
		if (c == ' ' || c == '(' || c == ')' || c == ',' //|| c == '.' 
		 || c == ';' || c == '\n' || c == '\r' || c == '\t' || c == '+'
		 || c == '>' || c == '<' || c == '*' || c == '^' //|| c == '{'
			//|| c == '}' 
		 || c == '[' || c == ']' || c == '"' || c == '\'') {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * プロパティバインディング時のELを評価して補完対象のManagedBeanを取得します。
	 * 
	 * @param dim ELをデリミタで分割したもの。
	 * @param project S2JSFProjectオブジェクト。
	 * @param initBean
	 * @return ELを評価した結果として得られたマネージド・ビーン
	 */
	public static ManagedBean evalPropertyBinding(String[] dim,S2JSFProject project,ManagedBean initBean){
		ManagedBean result = getFirstBean(dim[0],project,initBean);
		if(result==null){
			return null;
		}
		try {
			for(int i=1;i<dim.length-1;i++){
				ManagedBeanProperty[] props = result.getProperties();
				boolean flag = false;
				for(int j=0;j<props.length;j++){
					if(props[j].getPropertyName().equals(dim[i])){
						result = props[j].toManagedBean();
						flag = true;
						break;
					}
				}
				if(flag==false){
					return null;
				}
			}
		} catch(Exception ex){
		}
		return result;
	}
	
	/**
	 * マネージド・ビーンの名前から実際のマネージド・ビーンを解決します。
	 * <p>
	 * 以下の順で解決が行われます。
	 * </p>
	 * <ol>
	 *   <li>beanNameで指定したマネージド・ビーンがコンテナに登録されている場合、そのビーンを返却。</li>
	 *   <li>initBeanで指定したビーンがbeanNameで指定した名称のプロパティを持つ場合、そのプロパティの戻り値を返却。</li>
	 *   <li>上記のいずれにも該当しない場合、nullを返却</li>
	 * </ol>
	 * 
	 * @param beanName マネージド・ビーンの名前
	 * @param project S2JSFProjectオブジェクト
	 * @param initBean 初期化アクションで呼び出されるビーン（存在しない場合はnullを指定）
	 * @return 解決後のマネージド・ビーン
	 */
	private static ManagedBean getFirstBean(String beanName,S2JSFProject project,ManagedBean initBean){
		ManagedBean result = null;
		ManagedBean[] beans = project.getManagedBeans();
		for(int i=0;i<beans.length;i++){
			if(beans[i].getBeanName().equalsIgnoreCase(beanName)){
				result = beans[i];
				break;
			}
		}
		if(result==null && initBean!=null){
			ManagedBeanProperty[] initProps = initBean.getProperties();
			for(int i=0;i<initProps.length;i++){
				if(initProps[i].getPropertyName().equalsIgnoreCase(beanName)){
					result = initProps[i].toManagedBean();
				}
			}
		}
		return result;
	}
	
	/**
	 * マネージド・ビーンの記述を配列に分割します。
	 * 
	 * @param el マネージド・ビーンの記述を含むEL
	 * @return ELをデリミタで分割した配列
	 */
	public static String[] splitManagedBean(String el){
		String[] dim = el.trim().split("\\.");
		if(!el.endsWith(".")){
			return dim;
		}
		ArrayList list = new ArrayList();
		for(int i=0;i<dim.length;i++){
			list.add(dim[i]);
		}
		list.add("");
		return (String[])list.toArray(new String[list.size()]);
	}
	
	/**
	 * マネージド・ビーンを分割した配列を結合します。
	 * 
	 * @param dim ELをデリミタで分割した配列
	 * @return 引数の配列をデリミタで結合した文字列
	 */
	public static String joinManagedBean(String[] dim){
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<dim.length-1;i++){
			if(i!=0){
				sb.append('.');
			}
			sb.append(dim[i]);
		}
		return sb.toString();
	}
	
	/**
	 * 型がプリミティブ型かどうかを判定します。
	 * 
	 * @param type 型
	 * @return プリミティブ型の場合true、そうでない場合false
	 */
	public static boolean isPrimitive(String type){
		// この部分はユーティリティとして切り出したほうがいいかも
		if(type.equals("int") || type.equals("long") || type.equals("double") || type.equals("float") || 
				type.equals("char") || type.equals("boolean") || type.equals("byte")){
			return true;
		}
		return false;
	}
	
	/**
	 * パッケージ名なしのクラス名からパッケージ付きのフルクラス名を作成します。
	 * 
	 * @param parent この変数が使われているクラスの型
	 * @param type   パッケージなしのクラス名
	 * @return パッケージ付きのクラス名
	 */
	public static String getFullQName(IType parent,String type){
		if(type.indexOf('.') >= 0){
			return type;
		}
		if(isPrimitive(type)){
			return type;
		}
		IJavaProject project = parent.getJavaProject();
		try {
			IType javaType = project.findType("java.lang." + type);
			if(javaType!=null && javaType.exists()){
				return javaType.getFullyQualifiedName();
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		try {
			IType javaType = project.findType(parent.getPackageFragment().getElementName() + "." + type);
			if(javaType!=null && javaType.exists()){
				return javaType.getFullyQualifiedName();
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		try {
			IImportDeclaration[] imports = parent.getCompilationUnit().getImports();
			for(int i=0;i<imports.length;i++){
				String importName = imports[i].getElementName();
				if(importName.endsWith("." + type)){
					return importName;
				}
				if(importName.endsWith(".*")){
					try {
						IType javaType = project.findType(importName.replaceFirst("\\*$",type));
						if(javaType!=null && javaType.exists()){
							return javaType.getFullyQualifiedName();
						}
					} catch(Exception ex){
					}
				}
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return type;
	}
	
	/**
	 * HTMLタグのm:actionで呼び出される初期化ビーンを取得します。
	 * 
	 * @param project S2JSFProjectオブジェクト。
	 * @param root HTMLファイルのルート要素。
	 * @return HTMLタグのm:actionで呼び出される初期化ビーン。存在しない場合はnullを返します。
	 */
	public static ManagedBean getInitBean(S2JSFProject project,FuzzyXMLElement root){
		if(root==null){
			return null;
		}
		String mayaPrefix = Util.getMayaPrefix(root);
		if(mayaPrefix!=null){
			String invoke = Util.getXPathValue(root, "@" + mayaPrefix + ":action");
			if(invoke!=null){
				if(invoke.startsWith("#{") && invoke.endsWith("}")){
					invoke = invoke.replaceFirst("^#\\{","");
					invoke = invoke.replaceFirst("\\}$" ,"");
					String[] dim = invoke.split("\\.");
					if(dim.length < 2){
						// TODO エラー
					}
					return project.getManagedBean(dim[0]);
				} else {
					// TODO エラー
				}
			}
		}
		return null;
	}
}

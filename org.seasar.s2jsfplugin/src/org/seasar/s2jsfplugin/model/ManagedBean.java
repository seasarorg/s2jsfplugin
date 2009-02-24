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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.seasar.s2jsfplugin.Util;

/**
 * マネージド・ビーンの情報を格納するクラス。
 * 
 * @author Naoki Takezoe
 */
public class ManagedBean {
	
	private IJavaProject project;
	private String className;
	private String beanName;
	private String beanScope;
	
	public static final int TYPE_UNDEF     = -1;
	public static final int TYPE_CLASS     = 0;
	public static final int TYPE_INTERFACE = 1;
	
	public ManagedBean(IJavaProject project,String className,String beanName,String beanScope){
		this.project   = project;
		this.className = className;
		this.beanName  = beanName;
		this.beanScope = beanScope;
	}
	
	public String getJavadoc(){
		try {
			return Util.extractJavadoc(project.findType(className), null);
		} catch(Exception ex){
			return null;
		}
	}
	
	public int getType(){
		try {
			IType type = project.findType(className);
			if(type.isInterface()){
				return TYPE_INTERFACE;
			} else if(type.isClass()){
				return TYPE_CLASS;
			}
		} catch(Exception ex){
			ex.printStackTrace();
		}
		return TYPE_UNDEF;
	}
	
	/**
	 * @return beanName を戻します。
	 */
	public String getBeanName() {
		return beanName;
	}
	
	/**
	 * @return className を戻します。
	 */
	public String getClassName() {
		return className;
	}
	
	/**
	 * @return beanScope を戻します。
	 */
	public String getBeanScope() {
		return beanScope;
	}
	
	/**
	 * プロパティ名の一覧を返します(IFieldで返したほうがいいかも？) 
	 * 
	 * @return
	 */
	public ManagedBeanProperty[] getProperties(){
		try {
			IType type = project.findType(className);
			IMethod[] methods = getMethods();
			ArrayList list = new ArrayList();
			HashSet set = new HashSet();
			for(int i=0;i<methods.length;i++){
				String name = methods[i].getElementName();
				String propType = null;
				
				// getClassは除外する
				if(name.equals("getClass")){
					continue;
				}
				// プロパティ名と型を取得する
				if(name.startsWith("get")){
					String returnType = methods[i].getReturnType();
					// voidは除く
					if(returnType.equals("V") || methods[i].getParameterTypes().length!=0){
						continue;
					}
					propType = Signature.toString(returnType);
					
				} else if(name.startsWith("set")){
					String[] argTypes = methods[i].getParameterTypes();
					if(argTypes.length==1){
						propType = Signature.toString(argTypes[0]);
					}
					
				} else if(name.startsWith("is")){
					String returnType = methods[i].getReturnType();
					propType = Signature.toString(returnType);
					
				}
				
				if((((name.startsWith("get") || name.startsWith("set")) && name.length() > 3) ||
					(name.startsWith("is") && name.length() > 2)) && propType!=null ){
					
					String propName = getPropertyName(name);
					
					if(name.startsWith("get") || name.startsWith("is") || set.contains(propName + "(" + propType + ")")){
						list.add(new ManagedBeanProperty(type,propName,propType,methods[i]));
					} else {
						set.add(propName + "(" + propType + ")");
					}
				}
			}
			return (ManagedBeanProperty[])list.toArray(new ManagedBeanProperty[list.size()]);
		} catch(Exception ex){
			return new ManagedBeanProperty[0];
		}
	}
	
	/**
	 * バリデータとして使用可能なメソッドの一覧を取得します。
	 */
	public IMethod[] getValidaterMethods(){
		try {
			IMethod[] methods = getMethods();
			ArrayList list = new ArrayList();
			for(int i=0;i<methods.length;i++){
				String[] args = methods[i].getParameterTypes();
				if(methods[i].getReturnType().equals("V") && args.length==3 && 
				   (args[0].equals("QFacesContext;") || args[0].equals("Ljavax.faces.context.FacesContext;")) && 
				   (args[1].equals("QUIComponent;")  || args[1].equals("Ljavax.faces.component.UIComponent;")) && 
				   (args[2].equals("QObject;")       || args[2].equals("Ljava.lang.Object;"))){
					list.add(methods[i]);
				}
			}
			return (IMethod[])list.toArray(new IMethod[list.size()]);
		} catch(Exception ex){
			return new IMethod[0];
		}
	}
	
	public boolean hasValidaterMethod(String methodName){
		IMethod[] methods = getValidaterMethods();
		for(int i=0;i<methods.length;i++){
			if(methods[i].getElementName().equals(methodName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 値変更リスナとして使用可能なメソッドの一覧を取得します。
	 */
	public IMethod[] getValueChangeListenerMethods(){
		try {
			IMethod[] methods = getMethods();
			ArrayList list = new ArrayList();
			for(int i=0;i<methods.length;i++){
				String[] args = methods[i].getParameterTypes();
				if(methods[i].getReturnType().equals("V") && args.length==1 && 
						(args[0].equals("QValueChangeEvent;") || args[0].equals("Qjavax.faces.event.ValueChangeEvent;"))){
					list.add(methods[i]);
				}
			}
			return (IMethod[])list.toArray(new IMethod[list.size()]);
		} catch(Exception ex){
			return new IMethod[0];
		}
	}

	public boolean hasValueChangeListenerMethod(String methodName){
		IMethod[] methods = getValueChangeListenerMethods();
		for(int i=0;i<methods.length;i++){
			if(methods[i].getElementName().equals(methodName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * アクションリスナとして使用可能なメソッドの一覧を取得します。
	 */
	public IMethod[] getActionListenerMethods(){
		try {
			IMethod[] methods = getMethods();
			ArrayList list = new ArrayList();
			for(int i=0;i<methods.length;i++){
				String[] args = methods[i].getParameterTypes();
				if(methods[i].getReturnType().equals("V") && args.length==1 && 
						(args[0].equals("QActionEvent;") || args[0].equals("Qjavax.faces.event.ActionEvent;"))){
					list.add(methods[i]);
				}
			}
			return (IMethod[])list.toArray(new IMethod[list.size()]);
		} catch(Exception ex){
			return new IMethod[0];
		}
	}

	public boolean hasActionListenerMethod(String methodName){
		IMethod[] methods = getActionListenerMethods();
		for(int i=0;i<methods.length;i++){
			if(methods[i].getElementName().equals(methodName)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * アクションメソッドとして使用可能なメソッドの一覧を取得します。
	 */
	public IMethod[] getActionMethods(){
		try {
			IMethod[] methods = getMethods();
			ArrayList list = new ArrayList();
			for(int i=0;i<methods.length;i++){
				if((methods[i].getReturnType().equals("QString;") || methods[i].getReturnType().equals("V")) && 
				    methods[i].getParameterTypes().length==0){
					list.add(methods[i]);
				}
			}
			return (IMethod[])list.toArray(new IMethod[list.size()]);
		} catch(Exception ex){
			return new IMethod[0];
		}
	}
	
	public boolean hasActionMethod(String methodName){
		IMethod[] methods = getActionMethods();
		for(int i=0;i<methods.length;i++){
			if(methods[i].getElementName().equals(methodName)){
				return true;
			}
		}
		return false;
	}
	
	/** アクセサメソッド名からプロパティ名を取得します */
	private String getPropertyName(String methodName){
		int point = 3;
		if(methodName.startsWith("is")){
			point = 2;
		}
		String name = methodName.substring(point);
		name = name.substring(0,1).toLowerCase() + name.substring(1);
		return name;
	}
	
	/** このマネージド・ビーンが引数で指定したプロパティを持っているかどうかを返します */
	public boolean hasProperty(String propertyName){
		ManagedBeanProperty[] props = getProperties();
		for(int i=0;i<props.length;i++){
			if(props[i].getPropertyName().equals(propertyName)){
				return true;
			}
		}
		return false;
	}
	
	public IMethod[] getMethods() throws JavaModelException {
		IType type = project.findType(className);
		
		ArrayList list = new ArrayList();
		IMethod[] methods = type.getMethods();
		for(int i=0;i<methods.length;i++){
			if(checkMethod(methods[i])){
				list.add(methods[i]);
			}
		}
		// スーパークラスを調べる
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		IType[] superClass = hierarchy.getAllSuperclasses(type);
		for(int i=0;i<superClass.length;i++){
			IMethod[] superMethods = superClass[i].getMethods();
			for(int j=0;j<superMethods.length;j++){
				if(checkMethod(superMethods[j]) && !containsMethod(list,superMethods[j])){
					list.add(superMethods[j]);
				}
			}
		}
		return (IMethod[])list.toArray(new IMethod[list.size()]);
	}
	
	private boolean containsMethod(List list,IMethod method){
		for(int i=0;i<list.size();i++){
			IMethod checkMethod = (IMethod)list.get(i);
			if(checkMethod.getElementName().equals(method.getElementName())){
				String[] types1 = checkMethod.getParameterTypes();
				String[] types2 = method.getParameterTypes();
				return Arrays.equals(types1,types2);
			}
		}
		return false;
	}
	
	private boolean checkMethod(IMethod method){
		try {
			if(!method.isConstructor() && !method.isMainMethod() && Flags.isPublic(method.getFlags())){
				return true;
			}
		} catch(Exception ex){
			
		}
		return false;
	}

}

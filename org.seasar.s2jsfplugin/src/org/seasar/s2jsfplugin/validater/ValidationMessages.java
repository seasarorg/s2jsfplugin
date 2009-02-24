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

/**
 * バリデーションのエラーメッセージのキーを定義します。
 * 
 * @author Naoki Takezoe
 */
public class ValidationMessages {
	public static final String NOT_DEFINED      = "Validation.Message.NotDefined";
	public static final String REQUIRE_CLOSE    = "Validation.Message.RequireClose";
	public static final String ATTR_REQUIRED    = "Validation.Message.AttributeRequired";
	public static final String INJECT_PREFIX    = "Validation.Message.InjectPrefix";
	public static final String INJECT_EMPTY     = "Validation.Message.InjectEmpty";
	public static final String PREFIX_NOT_EXIST = "Validation.Message.PrefixNotExist";
	public static final String TAGLIB_NOT_EXIST = "Validation.Message.TaglibNotExist";
	public static final String NS_NOT_DEFINED   = "Validation.Message.NamespaceNotDefined";
	public static final String NOT_SPECIFIED    = "Validation.Message.NotSpecified";
	public static final String NOT_EXISTS       = "Validation.Message.NotExists";
	public static final String INVALID_EL       = "Validation.Message.InvalidEL";
	public static final String TLD_NOT_FOUND    = "Validation.Message.TLDNotFound";
}

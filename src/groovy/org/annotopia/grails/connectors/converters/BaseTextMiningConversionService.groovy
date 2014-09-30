/*
 * Copyright 2014 Massachusetts General Hospital
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.annotopia.grails.connectors.converters

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.codehaus.groovy.grails.web.json.JSONObject;

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
class BaseTextMiningConversionService {

	/** Constants for the URNs. */
	protected static final String URN_SNIPPET_PREFIX = "urn:domeo:contentsnippet:uuid:";
	protected static final String URN_ANNOTATION_SET_PREFIX = "urn:domeo:annotationset:uuid:";
	protected static final String URN_ANNOTATION_PREFIX = "urn:domeo:annotation:uuid:";
	protected static final String URN_SPECIFIC_RESOURCE_PREFIX = "urn:domeo:specificresource:uuid:";
	protected static final String URN_SELECTOR_PREFIX = "urn:domeo:selector:uuid:";
	
	/** The date format for the creation date stamps. */
	protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	
	/* Maximum lengths for prefixes and suffixes. */
	private static final int MAX_LENGTH_PREFIX_AND_SUFFIX = 50;
	
	/** Search for a match in the selected text.
	 * @param textToAnnotate The text to search within.
	 * @param putativeExactMatch The exact text to match.
	 * @param start The start index for the search.
	 * @return The matches, or null if it cannot be found. */
	protected def searchForMatch(final String textToAnnotate, final String putativeExactMatch,
			final int start)
	{
		String matchRegex = putativeExactMatch.replaceAll(/\s+/,"\\\\s+")
		matchRegex = matchRegex.replaceAll("[)]", "\\\\)")
		matchRegex = matchRegex.replaceAll("[(]", "\\\\(")
		Pattern pattern = Pattern.compile("\\b${matchRegex}\\b", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE)
		Matcher matcher = pattern.matcher(textToAnnotate)
		int startPos = -1
		int endPos = -1
		if (matcher.find(start)) {
			startPos = matcher.start()
			endPos = matcher.end()
			String exactMatch = textToAnnotate[startPos..endPos - 1]
			
			String prefix = null;
			if(startPos == 0) {
				prefix = '';
			} else {
				 prefix = textToAnnotate.getAt([
					 Math.max(startPos - (MAX_LENGTH_PREFIX_AND_SUFFIX + 1), 0)..Math.max(0, startPos - 1)
				])
			}
			
			String suffix = null;
			if(Math.min(endPos, textToAnnotate.length() - 1)==Math.min(startPos + MAX_LENGTH_PREFIX_AND_SUFFIX, textToAnnotate.length()-1)) {
				suffix = "";
			} else {
				suffix = textToAnnotate.getAt([
					Math.min(endPos, textToAnnotate.length() - 1)..Math.min(startPos + MAX_LENGTH_PREFIX_AND_SUFFIX, textToAnnotate.length()-1)
				])
			}
			
			return ['offset':startPos,'prefix': prefix, 'exact': exactMatch, 'suffix': suffix]
		}else{
			return null
		}
	}
	
	/** @return Create the public permissions content. */
	protected JSONObject getPublicPermissions( ) {
		JSONObject permissions = new JSONObject( );
		permissions.put("permissions:isLocked", "false");
		permissions.put("permissions:accessType", "urn:domeo:access:public");
		return permissions;
	}
}

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
package org.annotopia.grails.connectors.vocabularies;

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
public interface IOOpenAnnotation {

	static final String Annotation = "oa:Annotation";
	static final String hasTarget = "oa:hasTarget";
	static final String hasBody = "oa:hasBody";
	
	static final String ContentAsText = "cnt:ContentAsText";
	static final String chars = "cnt:chars";
	
	static final String SemanticTag = "oa:SemanticTag";
	
	static final String SpecificResource = "oa:SpecificResource";
	static final String hasSelector = "oa:hasSelector";
	static final String hasSource = "oa:hasSource";
	
	static final String TextQuoteSelector = "oa:TextQuoteSelector";
	static final String prefix = "oa:prefix";
	static final String suffix = "oa:suffix";
	static final String exact = "oa:exact";
}

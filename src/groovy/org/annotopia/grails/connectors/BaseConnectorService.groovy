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
package org.annotopia.grails.connectors;

import groovyx.net.http.HTTPBuilder

import org.apache.http.conn.params.ConnRoutePNames

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
public class BaseConnectorService {

	protected void evaluateProxy(HTTPBuilder http, String uri) {
		if(connectorsConfigAccessService.isProxyDefined()) {
			log.info("proxy: " + connectorsConfigAccessService.getProxyIp() + "-" + connectorsConfigAccessService.getProxyPort());
			http.client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, connectorsConfigAccessService.getProxyHttpHost());
		} else {
			log.info("NO PROXY selected while accessing " + uri);
		}
	}
}

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
package org.annotopia.grails.connectors.services

import grails.util.Holders

import org.annotopia.grails.connectors.model.Connector
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.springframework.context.ApplicationContext

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
class ConnectorsManagerService {

	public void registerConnectors() {
		def pluginManager = PluginManagerHolder.getPluginManager();
		pluginManager.getAllPlugins().each {
			if(it.name.startsWith("cn") && it.name.endsWith("Connector")) {
				log.info("** Detected: " + it.name);
				def serviceName = it.name.substring(2).replaceAll("Connector", "") + "Service";
				char[] c = serviceName.toCharArray();
				c[0] = Character.toLowerCase(c[0]);
				serviceName = new String( c );
			
				ApplicationContext ctx = Holders.grailsApplication.mainContext
				Object service = ctx.getBean(serviceName);
				
				def conectorName = ((String)it.getProperties( ).get('title'))
				def conectorVersion = ((String)it.getProperties( ).get('version'))
				def conectorDescription = ((String)it.getProperties( ).get('description'))
				
				def connector = Connector.findByName(conectorName);
				if(connector==null) {
					connector = new Connector(
						ver: conectorVersion,
						name: conectorName,
						description: conectorDescription
					).save(failOnError: true);
				} else {
					log.info "Found: " + conectorName;
				}

				// retrieve interfaces for the service class
				Class<?> clazz = service.getClass( ).getSuperclass( );
				Class<?>[ ] interfaces = clazz.getInterfaces( );
				for(Class<?> i : interfaces) {
					switch(i.getName( )) {
						case "org.annotopia.grails.connectors.ITermSearchService":
							log.info("Found Term Search Connector");
							break;
							
						case "org.annotopia.grails.connectors.ITextMiningService":
							log.info("Found Text Mining Connector");
							break;
							
						case "org.annotopia.grails.connectors.IVocabulariesListService":
							log.info("Found Vocabularies List Connector");
							break;
					}
				}
			}
		}
	}
}

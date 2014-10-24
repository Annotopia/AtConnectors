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

import java.util.HashMap;

import grails.util.Holders

import org.annotopia.grails.connectors.model.Connector
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.web.json.JSONObject;
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
				//def serviceName = it.name.substring(2).replaceAll("Connector", "") + "Service";
				//char[] c = serviceName.toCharArray();
				//c[0] = Character.toLowerCase(c[0]);
				//serviceName = new String( c );
				def serviceName = ((String)it.getProperties( ).get('service'));
				if(serviceName.trim().length()<3 && !serviceName.endsWith("Service")) {
					log.error("No service detected: " + serviceName);
				} else {				
					char[] c = serviceName.toCharArray();
					c[0] = Character.toLowerCase(c[0]);
					serviceName = new String( c );
				
					ApplicationContext ctx = Holders.grailsApplication.mainContext
					Object service = ctx.getBean(serviceName);
					
					def connectorName = it.name
					
					def connector = Connector.findByName(connectorName);
					if(connector==null) {
						def connectorTitle = ((String)it.getProperties( ).get('title'))
						def connectorVersion = ((String)it.getProperties( ).get('version'))
						def connectorDescription = ((String)it.getProperties( ).get('description'))
						
						connector = new Connector(
							ver: connectorVersion,
							name: connectorName,
							title: connectorTitle,
							description: connectorDescription
						).save(failOnError: true);
					} else {
						log.warn "Connector already registered";
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
	
	private Object retrieveService(String identifier) {
		
	}
	
	/**
	 * Method that must be implemented by all term search services
	 * @param service		The service to call
	 * @param content		The search query
	 * @param parameters	The service parametrization
	 * @return The results in JSON format
	 */
	JSONObject search(Object service, String content, HashMap parameters) {
		
	}
	
	/**
	 * Method that returns all available vocabularies.
	 * @param service		The service to call
	 * @param parameters	The service parametrization
	 * @return List of vocabularies
	 */
	JSONObject listVocabularies(Object service, HashMap parameters) {
		
	}
	
	/**
	 * Method that must be implemented by all text mining and entity recognition services
	 * @param service		The service to call
	 * @param resourceUri			The URI of the analyzed resource
	 * @param content				The content to analyze
	 * @param parameters			The service parametrization
	 * @return The results in JSON format
	 */
	JSONObject textmine(Object service, String resourceUri, String content, HashMap parameters) {
		
	}
}

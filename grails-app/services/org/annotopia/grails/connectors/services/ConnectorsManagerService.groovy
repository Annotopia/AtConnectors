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

import org.annotopia.grails.connectors.ITermSearchService
import org.annotopia.grails.connectors.ITextMiningService
import org.annotopia.grails.connectors.IVocabulariesListService
import org.annotopia.grails.connectors.model.Connector;
import org.annotopia.grails.connectors.model.ConnectorInterface;
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.ApplicationContext

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
class ConnectorsManagerService {
	
	/** Index for the term search interface. */
	private static final int TERM_SEARCH = 0;
	
	/** Index for the text mining interface. */
	private static final int TEXT_MINING = 1;
	
	/** Index for the vocabulary search interface. */
	private static final int VOCABULARY_SEARCH = 2;
	
	/** The list of connector interfaces. */
	private ConnectorInterface[ ] connectorInterfaces;
	
	/** The map of registered connectors. */
	private Map<String, Connector> connectors;
	
	public ConnectorsManagerService( ) {
		// initialise
		connectorInterfaces = new ConnectorInterface[3];
		connectors = new HashMap<String, Connector>(27);
	}

	/**
	 * This method registers all the allowed/recognized connectors interfaces.
	 */
	public void registerInterfaces() {		
		connectorInterfaces[TERM_SEARCH] = new ConnectorInterface(
			"title": ITermSearchService.class.getSimpleName( ),
			"fullname": ITermSearchService.class.getName( ),
			"name": "Terms Search",
			"description":"Search vocabularies and ontologies."
		);
		connectorInterfaces[TEXT_MINING] = new ConnectorInterface(
			"title": ITextMiningService.class.getSimpleName( ),
			"fullname": ITextMiningService.class.getName( ),
			"name": "Textmining",
			"description": "Text mining of textual content"
		);
		connectorInterfaces[VOCABULARY_SEARCH] = new ConnectorInterface(
			"title": IVocabulariesListService.class.getSimpleName( ),
			"fullname": IVocabulariesListService.class.getName( ),
			"name": "Vocabularies Listing",
			"description":"Listing of available vocabularies"
		);
	}
	
	/**
	 * This method dynamically registers all the available connectors.
	 */
	public void registerConnectors() {
		def pluginManager = PluginManagerHolder.getPluginManager();
		pluginManager.getAllPlugins().each {
			if(it.name.startsWith("cn") && it.name.endsWith("Connector")) {
				log.info("** Detected: " + it.name);
				def serviceName = ((String)it.getProperties( ).get('service'));
				if(serviceName!=null) {
					if(serviceName.trim().length()<3 && !serviceName.endsWith("Service")) {
						log.error("No service detected: " + serviceName);
					} else {				
						char[] c = serviceName.toCharArray();
						c[0] = Character.toLowerCase(c[0]);
						serviceName = new String( c );
					
						ApplicationContext ctx = Holders.grailsApplication.mainContext
						Object service = ctx.getBean(serviceName);
						
						def connectorName = it.name					
						def connectorTitle = ((String)it.getProperties( ).get('title'))
						def connectorVersion = ((String)it.getProperties( ).get('version'))
						def connectorDescription = ((String)it.getProperties( ).get('description'))
						
						def connector = new Connector(
							ver: connectorVersion,
							name: connectorName,
							title: connectorTitle,
							serviceName: serviceName,
							description: connectorDescription
						);
						connectors.put(connectorName, connector);
		
						// retrieve interfaces for the service class
						Class<?> clazz = service.getClass().getSuperclass( );
						Class<?>[ ] interfaces = clazz.getInterfaces( );
						for(Class<?> i : interfaces) {
							switch(i.getName()) {
								case connectorInterfaces[TERM_SEARCH].getFullname( ):
									log.info("Found Term Search Connector");
									connector.interfaces.add(connectorInterfaces[TERM_SEARCH])
									break;
									
								case connectorInterfaces[TEXT_MINING].getFullname( ):
									log.info("Found Text Mining Connector");
									connector.interfaces.add(connectorInterfaces[TEXT_MINING])
									break;
									
								case connectorInterfaces[VOCABULARY_SEARCH].getFullname( ):
									log.info("Found Vocabularies List Connector");
									connector.interfaces.add(connectorInterfaces[VOCABULARY_SEARCH])
									break;
							}
						}
					}
				} else {
					log.warn("Skipped dynamic connector detection");
					log.warn("-> serviceName not defined in *Plugin.groovy");
					log.warn("Plugin will be stil available through custom means");
				}
			}
		}
	}
	
	/**
	 * Returns the list of all available connectors.
	 * @return The list of all available connectors.
	 */
	public List<Connector> listConnectors() {
		return connectors.values( );
	}
	
	/**
	 * Returns the list of all available connector interfaces.
	 * @return The list of all available connector interfaces.
	 */
	public ConnectorInterface[ ] listConnectorsInterfaces() {
		return connectorInterfaces;
	}
	
	/**
	 * Returns the connector service that matches the requested connector
	 * name. Returns an exception if a service with that name does not exist
	 * or if the service is null.
	 * @param name	The name of the requested connector
	 * @return The instance of the service of the connector.
	 */
	private Object retrieveService(String serviceName) {
		log.warn serviceName
		log.warn connectors.get(serviceName)
		def connector = connectors.get(serviceName);
		if(connector!=null) {
			ApplicationContext ctx = Holders.grailsApplication.mainContext
			Object service = ctx.getBean(connector.serviceName);
			if(service!=null) {
				return service;
			}
			log.warn("Service null by name: " + serviceName);
			throw new RuntimeException("Service null by name: " + serviceName);
		} else {
			log.warn("Service not found by name: " + serviceName);
			throw new RuntimeException("Service not found by name: " + serviceName);
		}
	}
	
	/**
	 * Retrieves the service if it offers the requested features.
	 * Returns an exception if the services does not offer the requested feature.
	 * @param serviceName	The name of the requested connector
	 * @param feature		The requested feature.
	 * @return The instance of the service of the connector.
	 */
	private Object retrieveServiceFeature(String serviceName, String feature) {
		def service = retrieveService(serviceName);
		Class<?> clazz = service.getClass().getSuperclass( );
		Class<?>[ ] interfaces = clazz.getInterfaces( );
		
		boolean compatible = false;
		for(Class<?> i : interfaces) {
			if(i.getName().equals(feature))
				compatible = true;
		}
		if(compatible) return service;
		else throw new RuntimeException("Incompatible service selected.");
	}
}

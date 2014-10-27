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
import org.annotopia.grails.connectors.model.Connector
import org.annotopia.grails.connectors.model.ConnectorInterface
import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.ApplicationContext

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 */
class ConnectorsManagerService {

	/**
	 * This method registers all the allowed/recognized connectors interfaces.
	 */
	public void registerInterfaces() {
		
		def interfaces = [
			["title":"ITermSearchService","name":"org.annotopia.grails.connectors.ITermSearchService"],
			["title":"ITextMiningService","name":"org.annotopia.grails.connectors.ITextMiningService"],
			["title":"IVocabulariesListService","name":"org.annotopia.grails.connectors.IVocabulariesListService"]
		];
		
		interfaces.each { 
			def connectorInterface = ConnectorInterface.findByTitle(it.title);
			if(connectorInterface==null) {
				log.info("** Registering: " + it.name);
				new ConnectorInterface(
					name: it.name,
					title: it.title
				).save(failOnError: true);
			}
		}
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
							serviceName: serviceName,
							description: connectorDescription
						).save(failOnError: true);
					} else {
						log.warn "Connector already registered";
					}
	
					// retrieve interfaces for the service class
					Class<?> clazz = service.getClass().getSuperclass( );
					Class<?>[ ] interfaces = clazz.getInterfaces( );
					for(Class<?> i : interfaces) {
						switch(i.getName()) {
							case "org.annotopia.grails.connectors.ITermSearchService":
								log.info("Found Term Search Connector");
								ConnectorInterface ci = ConnectorInterface.findByName(i.getName());
								if(ci!=null) connector.interfaces.add(ci)
								break;
								
							case "org.annotopia.grails.connectors.ITextMiningService":
								log.info("Found Text Mining Connector");
								ConnectorInterface ci = ConnectorInterface.findByName(i.getName());
								if(ci!=null) connector.interfaces.add(ci)
								break;
								
							case "org.annotopia.grails.connectors.IVocabulariesListService":
								log.info("Found Vocabularies List Connector");
								ConnectorInterface ci = ConnectorInterface.findByName(i.getName());
								if(ci!=null)  connector.interfaces.add(ci)
								break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns the connector service that matches the requested connector
	 * name. Returns an exception if a service with that name does not exist
	 * or if the service is null.
	 * @param name	The name of the requested connector
	 * @return The instance of the service of the connector.
	 */
	private Object retrieveService(String serviceName) {
		def connector = Connector.findByName(serviceName);
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
	
	/**
	 * Method that must be implemented by all term search services
	 * @param serviceName	The service to call
	 * @param content		The search query
	 * @param parameters	The service parametrization
	 * @return The results in JSON format
	 */
	JSONObject search(String serviceName, String content, HashMap parameters) {		
		def service = retrieveServiceFeature(serviceName, ITermSearchService.class.getName());	
		service.search(content, parameters);
	}
	
	/**
	 * Method that returns all available vocabularies.
	 * @param serviceName	The service to call
	 * @param parameters	The service parametrization
	 * @return List of vocabularies
	 */
	JSONObject listVocabularies(Object serviceName, HashMap parameters) {
		def service = retrieveServiceFeature(serviceName, IVocabulariesListService.class.getName());
		service.listVocabularies(parameters);
	}
	
	/**
	 * Method that must be implemented by all text mining and entity recognition services
	 * @param serviceName		The service to call
	 * @param resourceUri		The URI of the analyzed resource
	 * @param content			The content to analyze
	 * @param parameters		The service parametrization
	 * @return The results in JSON format
	 */
	JSONObject textmine(Object serviceName, String resourceUri, String content, HashMap parameters) {
		def service = retrieveServiceFeature(serviceName, ITextMiningService.class.getName());
		service.textmine(resourceUri, content, parameters);
	}
}

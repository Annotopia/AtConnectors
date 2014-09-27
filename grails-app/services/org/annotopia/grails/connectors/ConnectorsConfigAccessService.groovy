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
package org.annotopia.grails.connectors

import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.http.HttpHost

/**
 * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
 * 
 * This class centralizes the access to the Domeo configuration properties.
 */
class ConnectorsConfigAccessService {


    def grailsApplication;
    
    public boolean isProxyDefined() {
        //grailsApplication.config.annotopia.server.proxy.host.isEmpty() ?????
        return (grailsApplication.config.annotopia.server.proxy.host!=null && grailsApplication.config.annotopia.server.proxy.host.size()>0 
            && grailsApplication.config.annotopia.server.proxy.port!=null && grailsApplication.config.annotopia.server.proxy.port.size()>0);
    }
    
    public HttpHost getProxyHttpHost() {
        if(isProxyDefined()) {
            log.info("proxy: " + getProxyIp() + "-" + getProxyPort()) ;
            return new HttpHost(getProxyIp(), getProxyPort(), getProxyProtocol());
        } else throw new RuntimeException("No proxy defined, check with isProxyDefined() first.");
    }
    
    public Proxy getProxy() {
        if(isProxyDefined()) {
            log.info("proxy: " + getProxyIp() + "-" + getProxyPort()) ;
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyIp(), getProxyPort()));
        } else throw new RuntimeException("No proxy defined, check with isProxyDefined() first.");
    }
    
    public String getProxyIp() {
        return grailsApplication.config.annotopia.server.proxy.host;
    }
    
    public Integer getProxyPort() {
        return new Integer(grailsApplication.config.annotopia.server.proxy.port);
    }
	
	public Integer getProxyProtocol() {
		return new Integer(grailsApplication.config.annotopia.server.proxy.protocol);
	}
}

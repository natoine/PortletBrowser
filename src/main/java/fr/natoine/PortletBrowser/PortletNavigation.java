/*
 * Copyright 2010 Antoine Seilles (Natoine)
 *   This file is part of PortletBrowser.

    model-htmlDocs is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    model-htmlDocs is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with PortletBrowser.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.natoine.PortletBrowser;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventPortlet;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import fr.natoine.properties.PropertiesUtils;
import fr.natoine.stringOp.StringOp;

public class PortletNavigation extends GenericPortlet implements EventPortlet
{
	private Properties applicationProps ;
	private final String saved_properties = "/properties/PortletNavigation/appProperties";
	private final String default_properties = "/properties/PortletNavigation/defaultProperties";
	
	private static final String EDIT_VIEW = "/WEB-INF/jsp/Navigation/add_url.jsp";
	private static final String NORMAL_VIEW = "/WEB-INF/jsp/Navigation/navigate.jsp";
	
	private List<String> urls;
	private List<String> names;
	
	private PortletRequestDispatcher normalView;
	private PortletRequestDispatcher editView;
	
	public void doView( RenderRequest request, RenderResponse response )
	throws PortletException, IOException 
	{
		setRenderAttributes(request);
		normalView.include( request, response );
	}

	public void doEdit( RenderRequest request, RenderResponse response )
	throws PortletException, IOException 
	{
		setRenderAttributes(request);
		editView.include( request, response );
	}
	
	public void init( PortletConfig config ) throws PortletException {
		super.init( config );
		normalView = config.getPortletContext().getRequestDispatcher( NORMAL_VIEW );
		editView = config.getPortletContext().getRequestDispatcher( EDIT_VIEW );
		urls = new ArrayList<String>();
		names = new ArrayList<String>();
		
		// create application properties with default
		Properties defaultProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(default_properties));
		applicationProps = new Properties(defaultProps);
		// now load properties from last invocation
		applicationProps = PropertiesUtils.loadLastState(applicationProps, getPortletContext().getRealPath(saved_properties));
		
		//sets values 
		Enumeration<String> _properties = (Enumeration<String>) applicationProps.propertyNames();
		while(_properties.hasMoreElements())
		{
			String _property_name = _properties.nextElement();
			if(_property_name.contains("url")) 
				urls.add(applicationProps.getProperty(_property_name));
			if(_property_name.contains("name"))
				names.add(applicationProps.getProperty(_property_name));
		}
		
	}
	
	public void destroy() {
		normalView = null;
		super.destroy();
	}

	private void sendEvent(String _event_type , Serializable _event_object , ActionResponse response)
	{
		System.out.println("[PortletNavigation.sendEvent] type : " + _event_type + " value : " + _event_object);
		response.setEvent(_event_type, _event_object);
	}

	private void setRenderAttributes(RenderRequest request) 
	{
		//List<String> _urls = (List<String>) request.getPortletSession().getAttribute("urls");
		//if(_urls == null) _urls = new ArrayList<String>();
		request.setAttribute("urls", urls);
		//List<String> _names = (List<String>) request.getPortletSession().getAttribute("names");
		//if(_names == null) _names = new ArrayList<String>();
		request.setAttribute("names", names);
	}
	
	public void processAction(ActionRequest request, ActionResponse response)
	throws PortletException, PortletSecurityException, IOException {
		String op = request.getParameter("op");
		StringBuffer message = new StringBuffer(1024);
		if ((op != null) && (op.trim().length() > 0)) 
		{
			//Ajouter une url
			if (op.equalsIgnoreCase("add_url")) 
			{
				//vider la liste de sélections
				doAddUrl(request, response);
				return;
			} 
			//Supprimer une url
			if (op.equalsIgnoreCase("delete_url")) 
			{
				//vider la liste de sélections
				doDeleteUrl(request, response);
				return;
			}
			//envoyer l'url au navigateur web
			if(op.equalsIgnoreCase("go_url"))
			{
				doGoURL(request , response);
				return ;
			}
			else
			{
				System.out.println("[PortletNavigation.processAction 2]" + op);
				message.append("Operation not found");
			}
		} 
		else 
		{
			System.out.println("[PortletNavigation.processAction 3]" + op);
			message.append("Operation is null");
		}
		System.out.println("[PortletNavigation.processAction 4]" + op);
		response.setRenderParameter("message", message.toString());
		response.setPortletMode(PortletMode.VIEW);
	}
	
	private void doAddUrl(ActionRequest request, ActionResponse response)
	{
		if(request.getParameter("new_url")!=null && request.getParameter("new_name")!=null)
		{
			String _url = StringOp.deleteBlanks(request.getParameter("new_url")) ;
			String _name = StringOp.deleteBlanks(request.getParameter("new_name")) ;
			if(!StringOp.isNull(_url) && !StringOp.isNull(_name))
			{
				//List<String> _urls = (List<String>) request.getPortletSession().getAttribute("urls");
				//if(_urls == null) _urls = new ArrayList<String>();
				//List<String> _names = (List<String>) request.getPortletSession().getAttribute("names");
				//if(_names == null) _names = new ArrayList<String>();
				urls.add(_url);
				names.add(_name);
				int nb_urls = urls.size();
				applicationProps.setProperty("url" + nb_urls, _url);
				applicationProps.setProperty("name" + nb_urls, _name);
				PropertiesUtils.store(applicationProps, getPortletContext().getRealPath(saved_properties), "[PortletNavigation.doAddUrl]");
				//request.getPortletSession().setAttribute("urls", _urls);
				//request.getPortletSession().setAttribute("names", _names);
			}
		}
	}
	
	private void doDeleteUrl(ActionRequest request, ActionResponse response)
	{
		if(request.getParameter("indice")!=null)
		{
			int _indice = Integer.parseInt(request.getParameter("indice"));
			//List<String> _urls = (List<String>) request.getPortletSession().getAttribute("urls");
			//if(_urls == null) _urls = new ArrayList<String>();
			//List<String> _names = (List<String>) request.getPortletSession().getAttribute("names");
			//if(_names == null) _names = new ArrayList<String>();
			urls.remove(_indice);
			names.remove(_indice);
			//request.getPortletSession().setAttribute("urls", _urls);
			//request.getPortletSession().setAttribute("names", _names);
		}
	}
	private void doGoURL(ActionRequest request, ActionResponse response)
	{
		if(request.getParameter("url")!=null)
		{
			String url = request.getParameter("url");
			this.sendEvent("toLoadUrl", url , response);
			this.sendEvent("loadedurl", url, response);
		}
	}	
}
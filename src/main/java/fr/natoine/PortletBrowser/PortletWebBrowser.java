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
import java.util.Date;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.portlet.Event;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletSecurityException;
import javax.portlet.ValidatorException;

import org.htmlparser.util.ParserException;

import fr.natoine.dao.consultation.DAOConsultation;
import fr.natoine.dao.resource.DAOResource;
import fr.natoine.html.HTMLPage;
import fr.natoine.model_htmlDocs.HighlightSelectionHTML;
import fr.natoine.model_htmlDocs.SelectionHTML;
import fr.natoine.model_htmlDocs.WebPage;
import fr.natoine.model_resource.URI;
import fr.natoine.model_user.UserAccount;
import fr.natoine.properties.PropertiesUtils;

public class PortletWebBrowser extends GenericPortlet implements EventPortlet
{
	private Properties applicationProps ;
	private final String saved_properties = "/properties/PortletWebBrowser/appProperties";
	private final String default_properties = "/properties/PortletWebBrowser/defaultProperties";
	
	//private static CreateConsultation CREATOR_CONSULTATION = null ;
	//private static CreateUri CREATOR_URI = null ;
	private static DAOConsultation daoConsultation = null ;
	private static DAOResource daoResource = null ;
	
	private static EntityManagerFactory emf_consultation = null ; // Persistence.createEntityManagerFactory("consultation");
	private static EntityManagerFactory emf_resource = null ; // Persistence.createEntityManagerFactory("resource");
	
	private static boolean hide_url = false ;
	private static boolean hide_new_windows = false ;
	private static String defaultURL = "http://www.google.com";
	private static String defaultHeight = "200px";
	private static String defaultWidth = "100%";
	private static String defaultMessage = "There is a problem with your URL";
	
	public void init( PortletConfig config ) throws PortletException 
	{
		super.init( config );
		// create application properties with default
		Properties defaultProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(default_properties));
		applicationProps = new Properties(defaultProps);
		// now load properties from last invocation
		applicationProps = PropertiesUtils.loadLastState(applicationProps, getPortletContext().getRealPath(saved_properties));
		
		//sets values 
		if(applicationProps.getProperty("defaultURL")!=null) defaultURL = applicationProps.getProperty("defaultURL");
		if(applicationProps.getProperty("defaultHeight")!=null) defaultHeight = applicationProps.getProperty("defaultHeight");
		if(applicationProps.getProperty("defaultWidth")!=null) defaultWidth = applicationProps.getProperty("defaultWidth");
		if(applicationProps.getProperty("defaultMessage")!=null) defaultMessage = applicationProps.getProperty("defaultMessage");
		if(applicationProps.getProperty("hide_url")!=null)
			if(applicationProps.getProperty("hide_url").equalsIgnoreCase("true")) hide_url = true;
		if(applicationProps.getProperty("hide_new_windows")!=null)
			if(applicationProps.getProperty("hide_new_windows").equalsIgnoreCase("true")) hide_new_windows = true;
		
		//CREATOR_CONSULTATION = new CreateConsultation();
		//CREATOR_URI = new CreateUri();
		emf_consultation = Persistence.createEntityManagerFactory("consultation");
		emf_resource = Persistence.createEntityManagerFactory("resource");
		daoConsultation = new DAOConsultation(emf_consultation);
		daoResource = new DAOResource(emf_resource);
	}
	
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {
		String op = request.getParameter("op");
		StringBuffer message = new StringBuffer(1024);
		if ((op != null) && (op.trim().length() > 0)) 
		{
			//Mise à jour de l'url dans la barre de navigation de la portlet en View
			if (op.equalsIgnoreCase("go")) 
			{
				doGo(request , response , message);
				return;
			} 
			//il y a eu une action de sélection pour annotation
			else if(op.equalsIgnoreCase("selection"))
			{
				doSelection(request , response);
				return;
			}
			//mise à jour des données par interface Edit
			else if (op.equalsIgnoreCase("update")) 
			{
				doUpdate(request , response , message);
				return;
			}
			else if (op.equalsIgnoreCase("cancel")) 
			{
				doCancel(response);
				return;
			} 
			else if(op.equalsIgnoreCase("Page"))
			{
				doPage(request, response);
				return ;
			}
			else
			{
				System.out.println("[BrowserPortlet.processAction 2]" + op);
				message.append("Operation not found");
			}
		} 
		else 
		{
			System.out.println("[BrowserPortlet.processAction 3]" + op);
			message.append("Operation is null");
		}
		System.out.println("[BrowserPortlet.processAction 4]" + op);
		response.setRenderParameter("message", message.toString());
		response.setPortletMode(PortletMode.VIEW);
	}

	public void doView(RenderRequest request, RenderResponse response) {
		try {
		setRenderAttributes(request);
			response.setContentType("text/html");
			PortletRequestDispatcher prd = getPortletContext()
					.getRequestDispatcher("/WEB-INF/jsp/Browser/browser.jsp");
			prd.include(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doEdit(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		setRenderAttributes(request);
		response.setContentType("text/html");
		response.setTitle("Edit");
		PortletRequestDispatcher prd = getPortletContext()
				.getRequestDispatcher("/WEB-INF/jsp/Browser/edit.jsp");
		prd.include(request, response);
	}

	private void setRenderAttributes(RenderRequest request) 
	{
		String currentURL = defaultURL;
		request.getPortletSession().setAttribute("default_url", defaultURL, request.getPortletSession().APPLICATION_SCOPE);
		request.setAttribute("default_url", defaultURL);
		if(hide_url) request.setAttribute("hide_url", "");
		else request.removeAttribute("hide_url");
		if(hide_new_windows) request.setAttribute("hide_new_windows", "");
		else request.removeAttribute("hide_new_windows");
		if(request.getParameter("height")!=null) request.setAttribute("height" , request.getParameter("height"));
		else request.setAttribute("height", PortletWebBrowser.defaultHeight);
		if(request.getParameter("width")!=null) request.setAttribute("width" , request.getParameter("width"));
		else request.setAttribute("width", PortletWebBrowser.defaultWidth);
		if(request.getParameter("message")!=null) request.setAttribute("message" , request.getParameter("message"));
		else request.setAttribute("message", PortletWebBrowser.defaultMessage);
		if(request.getPortletSession().getAttribute("current_url")!=null) currentURL = (String)request.getPortletSession().getAttribute("current_url");
		request.setAttribute("url", currentURL);
		//String _clean_html = HTMLmanager.getCleanHTML(currentURL); //HTMLParser.toStringFromHTML(currentURL) ;
		HTMLPage toBrowse = new HTMLPage(currentURL);
		String html = toBrowse.getBody();
		//on set la liste de highlights
		ArrayList<HighlightSelectionHTML> _highlights ;
		if(request.getPortletSession().getAttribute("highlights")!=null)
			_highlights = ((ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("highlights"));
		else _highlights = new ArrayList<HighlightSelectionHTML>();
		//String _html_to_load = HTMLmanager.colorHighlights(_clean_html, _highlights);
		for(HighlightSelectionHTML highlight : _highlights)
		{
			try 
			{
				toBrowse.addAnnotationSpan(highlight.getSelection().getXpointerBegin(), highlight.getSelection().getXpointerEnd(), highlight.getStyle(), highlight.getInfo(), highlight.getId());
				if(toBrowse.getBody() != null) html = toBrowse.getBody() ;
				//System.out.println("[PortletBrowser.setRenderAttributes] html : " + html);
			}
			catch (ParserException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("[PortletWebBrowser.setRenderAttributes] unable to parse HTML and add annotations for URL : " + currentURL);
			}
		}
		//String _html_to_load = toBrowse.getBody();
		String _html_to_load = html;
		if(_html_to_load != null && _html_to_load.length() > 0) request.setAttribute("html" , _html_to_load);
		//else request.setAttribute("html" , _clean_html);
		else request.setAttribute("html" , "Problem Parsing HTML, try to reload page without annotations or selections ... or just reload ^^");
		//Add Css
		request.setAttribute("css" , toBrowse.getCss());
	}
	
	private void doGo(ActionRequest request, ActionResponse response , StringBuffer message) throws ReadOnlyException, ValidatorException, IOException, PortletModeException
	{
		boolean save = true;
		//PortletPreferences prefs = request.getPreferences();
		String url = request.getParameter("url");
		if (!url.startsWith("http://")) 
		{
			save = false;
			message.append("URLs must start with 'http://'<br/>");
			response.setRenderParameter("message", message.toString());
			response.setPortletMode(PortletMode.VIEW);
		} 
		if (save) 
		{
			request.getPortletSession().removeAttribute("highlights");
			//response.setRenderParameter("url", url.toLowerCase());
			//request.getPortletSession().setAttribute("current_url", url.toLowerCase());
			request.getPortletSession().setAttribute("current_url", url);
			this.sendEvent("loadedurl", url, response);
			response.setPortletMode(PortletMode.VIEW);
			request.getPortletSession().removeAttribute("highlights");
			//gestion de la consultation
			if(request.getPortletSession().getAttribute("consult_url") != null) // si une consultation a déjà commencé
			{
				if(! url.equalsIgnoreCase((String)request.getPortletSession().getAttribute("consult_url"))) // si on change de page à consulter
				{
					if(request.getPortletSession().getAttribute("user") != null)
					{
						//creates consultation
						//URI uri = CREATOR_URI.createAndGetURI((String)request.getPortletSession().getAttribute("consult_url"));
						//CREATOR_CONSULTATION.createsConsultation((UserAccount)request.getPortletSession().getAttribute("user"), (Date)request.getPortletSession().getAttribute("start_consult") , new Date(), uri, "[PortletWebBrowse]");
						URI uri = daoResource.createAndGetURI((String)request.getPortletSession().getAttribute("consult_url"));
						daoConsultation.createsConsultation((UserAccount)request.getPortletSession().getAttribute("user"), (Date)request.getPortletSession().getAttribute("start_consult") , new Date(), uri, "[PortletWebBrowse]");
					}
					request.getPortletSession().setAttribute("consult_url", url);
					request.getPortletSession().setAttribute("start_consult", new Date());
				}
				
			}
			else // si c'est la première consultation
			{
				request.getPortletSession().setAttribute("consult_url", url);
				request.getPortletSession().setAttribute("start_consult", new Date());
			}
		}
	}

	private void sendEvent(String _event_type , Serializable _event_object , ActionResponse response)
	{
		System.out.println("[BrowserPortlet.sendEvent] type : " + _event_type + " value : " + _event_object);
		response.setEvent(_event_type, _event_object);
	}
	
	private void doSelection(ActionRequest request , ActionResponse response)
	{
		//System.out.println("[BrowserPortlet.doSelection]");
		String url = request.getParameter("url") ;
		HTMLPage page = new HTMLPage();
		page.setURL("url");
		try {
			page.extractTitle();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String title = page.getTitle();
		//String title = HTMLmanager.getTitlePage(url) ;
		SelectionHTML _selection_html = new SelectionHTML();
		_selection_html.setHTMLContent(request.getParameter("text_selection"));
		_selection_html.setXpointerBegin(request.getParameter("xpointer_start"));
		_selection_html.setXpointerEnd(request.getParameter("xpointer_end"));
		_selection_html.setContextCreation("PortletWebBrowser");
		_selection_html.setCreation(new Date());
		_selection_html.setLabel("sélection de : " + title);
		//_selection_html.setRepresentsResource(representsResource);
		WebPage selectionOrigin = new WebPage();
		URI _access = new URI();
		_access.setEffectiveURI(url);
		selectionOrigin.setAccess(_access);
		selectionOrigin.setContextCreation("PortletWebBrowser");
		selectionOrigin.setCreation(new Date());
		//TODO
		//Pas la peine de sauver tout le html pour l'instant
		//selectionOrigin.setHTMLContent(HTMLmanager.getCleanHTML(url));
		selectionOrigin.setLabel(title);
		selectionOrigin.setPrincipalURL(_access);
		selectionOrigin.setRepresentsResource(_access);
		_selection_html.setSelectionOrigin(selectionOrigin);
		HighlightSelectionHTML _highLight = new HighlightSelectionHTML();
		_highLight.setSelection(_selection_html);
		_highLight.setStyle("background-color:yellow;");
		_highLight.setInfo("sélection en attente d'annotation");
		_highLight.setId(generateHighlightId(request));
		//_selection_html.setClassname("new_selection");
		this.sendEvent("selection", _highLight, response);
		//ajouter la nouvelle sélection à la liste courante
		ArrayList<HighlightSelectionHTML> _highlights ;
		if(request.getPortletSession().getAttribute("highlights")!=null) 
		{
			_highlights = (ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("highlights") ;
		}
		else _highlights = new ArrayList<HighlightSelectionHTML>();
		_highlights.add(_highLight);
		request.getPortletSession().setAttribute("highlights" , _highlights);
		//response.setRenderParameter("url", url);
		request.getPortletSession().setAttribute("current_url", url);
	}
	
	private void doPage(ActionRequest request , ActionResponse response)
	{
		//System.out.println("[BrowserPortlet.doPage]");
		String url = request.getParameter("url") ;
		WebPage _page = new WebPage();
		URI _access = new URI();
		_access.setEffectiveURI(url);
		_page.setAccess(_access);
		_page.setContextCreation("PortletWebBrowser");
		_page.setCreation(new Date());
		//TODO pour l'instant on se fout du contenu de la page
		//_page.setHTMLContent(HTMLmanager.getCleanHTML(url));
		HTMLPage page = new HTMLPage();
		page.setURL(url);
		try {
			page.extractTitle();
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String title = page.getTitle();
		//String title = HTMLmanager.getTitlePage(url) ;
		_page.setLabel(title);
		_page.setPrincipalURL(_access);
		_page.setRepresentsResource(_access);
		this.sendEvent("page", _page, response);
		//response.setRenderParameter("url", url);
		request.getPortletSession().setAttribute("current_url", url);
	}
	
	private void doUpdate(ActionRequest request, ActionResponse response , StringBuffer message) throws ReadOnlyException, ValidatorException, IOException, PortletModeException
	{
		String url = request.getParameter("default-url");
		//System.out.println("[BrowserPortlet.doUpdate] url : " + url);
		String height = request.getParameter("height");
		String width = request.getParameter("width");
		String noMessage = request.getParameter("nomessage");
		boolean px = false;
		boolean save = true ;
		// boolean save = true;
		if ((url != null) && (height != null) && (width != null) && (noMessage != null)) 
		{
			if (!url.startsWith("http://")) 
			{
				save = false;
				message.append("URLs must start with 'http://'<br/>");
			} 
			try 
			{
				if (height.endsWith("px")) 
				{
					height = height.substring(0, height.length() - 2);
				}
				Integer.parseInt(height);
			} 
			catch (NumberFormatException nfe) 
			{
				// Bad height value
				save = false;
				message.append("Height must be an integer<br/>");
			}
			try 
			{
				if (width.endsWith("px")) 
				{
					px = true;
					width = width.substring(0, width.length() - 2);
				} 
				else if (width.endsWith("%")) 
				{
					width = width.substring(0, width.length() - 1);
				}
				Integer.parseInt(width);
			}
			catch (NumberFormatException nfe) 
			{
				// Bad height value
				save = false;
				message.append("Width must be an integer<br/>");
			}
			if (save) 
			{
				if(request.getParameter("hide") != null && request.getParameter("hide").equalsIgnoreCase("hide"))
				{
					hide_url = true ;
					request.removeAttribute("hide");
				}
				else hide_url = false ;
				if(request.getParameter("hide_open") != null && request.getParameter("hide_open").equalsIgnoreCase("hide"))
				{
					hide_new_windows = true ;
					request.removeAttribute("hide");
				}
				else hide_new_windows = false ;
				//System.out.println("[BrowserPortlet.doUpdate] save");
				response.setRenderParameter("height", height + "px");
				response.setRenderParameter("width", px ? width + "px" : width + "%");
				//response.setRenderParameter("url", url);
				request.getPortletSession().setAttribute("current_url", url);
				//this.sendEvent("url", url, response);
				this.sendEvent("loadedurl", url, response);
				response.setRenderParameter("message", noMessage);
				defaultURL = url ;
				sendEvent("default_url", url, response);
				request.getPortletSession().setAttribute("default_url", url, request.getPortletSession().APPLICATION_SCOPE);
				defaultHeight = height + "px" ;
				defaultWidth = px ? width + "px" : width + "%" ;
				defaultMessage = noMessage ;
				applicationProps.setProperty("defaultURL", defaultURL);
				applicationProps.setProperty("defaultHeight", defaultHeight);
				applicationProps.setProperty("defaultWidth", defaultWidth);
				applicationProps.setProperty("defaultMessage", defaultMessage);
				if(hide_url) applicationProps.setProperty("hide_url", "true");
				else applicationProps.setProperty("hide_url", "false");
				if(hide_new_windows) applicationProps.setProperty("hide_new_windows", "true");
				else applicationProps.setProperty("hide_new_windows", "false");
				PropertiesUtils.store(applicationProps, getPortletContext().getRealPath(saved_properties), "[PortletWebBrowser.doUpdate]");
				response.setPortletMode(PortletMode.VIEW);
				return;
			}
			response.setRenderParameter("message", message.toString());
			response.setPortletMode(PortletMode.VIEW);
		}
	}
	
	private void doCancel(ActionResponse response) throws PortletModeException
	{
		response.setPortletMode(PortletMode.VIEW);
	}
	
	public void processEvent(EventRequest request, EventResponse response)
	{
		Event event = request.getEvent();
		String event_name = event.getName() ;
		//System.out.println("[PortletWebBrowser.processEvent] event : " + event.getName());
		if(event_name.equals("tohighlight"))
		{
			Object _highlight_event = event.getValue();
			//Récupérer la collection de highlights
			if(_highlight_event instanceof HighlightSelectionHTML)
			{
				ArrayList<HighlightSelectionHTML> _hightlights = (ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("highlights");
				if(_hightlights == null) _hightlights = new ArrayList<HighlightSelectionHTML>();
				_hightlights.add((HighlightSelectionHTML)_highlight_event);
				request.getPortletSession().setAttribute("highlights" , _hightlights);
			}
		}
		if(event_name.equals("todelete"))
		{
			//System.out.println("[PortletWebBrowser.processEvent] event to delete");
			if(event.getValue() instanceof HighlightSelectionHTML) doDelete(request , (HighlightSelectionHTML)event.getValue());
		}
		if(event_name.equals("toLoadUrl"))
		{
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://"))
				{
					request.getPortletSession().setAttribute("current_url", url);
					//response.setRenderParameter("url", url.toLowerCase());
					request.getPortletSession().removeAttribute("highlights");
					request.removeAttribute("highlights");
					//gestion de la consultation
					if(request.getPortletSession().getAttribute("consult_url") != null) // si une consultation a déjà commencé
					{
						if(! url.equalsIgnoreCase((String)request.getPortletSession().getAttribute("consult_url"))) // si on change de page à consulter
						{
							if(request.getPortletSession().getAttribute("user") != null)
							{
								//creates consultation
								//URI uri = CREATOR_URI.createAndGetURI((String)request.getPortletSession().getAttribute("consult_url"));
								//CREATOR_CONSULTATION.createsConsultation((UserAccount)request.getPortletSession().getAttribute("user"), (Date)request.getPortletSession().getAttribute("start_consult") , new Date(), uri, "[PortletWebBrowse]");
								URI uri = daoResource.createAndGetURI((String)request.getPortletSession().getAttribute("consult_url"));
								daoConsultation.createsConsultation((UserAccount)request.getPortletSession().getAttribute("user"), (Date)request.getPortletSession().getAttribute("start_consult") , new Date(), uri, "[PortletWebBrowse]");
							}
							request.getPortletSession().setAttribute("consult_url", url);
							request.getPortletSession().setAttribute("start_consult", new Date());
						}
						
					}
					else // si c'est la première consultation
					{
						request.getPortletSession().setAttribute("consult_url", url);
						request.getPortletSession().setAttribute("start_consult", new Date());
					}
				}
			}
		}
		if(event_name.equalsIgnoreCase("UserLog"))
		{
			if(event.getValue() instanceof UserAccount)
			{
				UserAccount _current_user = (UserAccount)event.getValue() ;
				if(_current_user.getId() != null) request.getPortletSession().setAttribute("user", _current_user);
			}
		}
		if(event_name.equalsIgnoreCase("UserUnLog"))
		{
			request.getPortletSession().removeAttribute("user");
		}
	}
	
	private void doDelete(EventRequest request , HighlightSelectionHTML _to_delete)
	{
		ArrayList<HighlightSelectionHTML> _hightlights = (ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("highlights");
		if(_hightlights != null && _hightlights.size()>0)
		{
			//System.out.println("[PortletWebBrowser.doDelete] _to_delete : " + _to_delete + " selection : " + _to_delete.getSelection() + " id : " + _to_delete.getId());
			for(HighlightSelectionHTML highlight : _hightlights)
			{
				//System.out.println("[PortletWebBrowser.doDelete] highlight : " + highlight + " selection : " + highlight.getSelection()+ " id : " + highlight.getId());
				if(highlight.getId().compareTo(_to_delete.getId()) == 0) 
				{
					_hightlights.remove(highlight);
					request.getPortletSession().setAttribute("highlights" , _hightlights);
					return ;
				}
			}
		}
		System.out.println("[PortletWebBrowser.doDelete] don't find _to_delete : " + _to_delete);
	}
	
	private String generateHighlightId(ActionRequest request)
	{
		int nb_highlight = 0 ;
		if(request.getPortletSession().getAttribute("nb_highlight")!=null)
		{
			nb_highlight = (Integer)request.getPortletSession().getAttribute("nb_highlight");
			int _new_value = nb_highlight + 1;
			request.getPortletSession().setAttribute("nb_highlight", _new_value);
		}
		else request.getPortletSession().setAttribute("nb_highlight", 1);
		return request.getPortletSession().getId() + this.getPortletName() + nb_highlight;
	}
}
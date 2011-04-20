<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>
<%
	String _url = (String)request.getAttribute("url") ;
	String _html = (String)request.getAttribute("html");
	String _css = (String)request.getAttribute("css");
%>
<table width="<%= request.getAttribute("width") %>">
    <tr class="portlet-msg-alert">
            <td colspan="2"><%= request.getParameter("message") != null ?  request.getParameter("message") : ""%></td>
    </tr>
    <tr>
    	<td>
	    	<form id="setUrl" method="post" action="<portlet:actionURL/>">
	    	<%
	    	if(request.getAttribute("hide_url") == null)
	    	{
	    	%>
	    	URL : <input id="url_setUrl" type="text" name="url" value="<%= _url %>" size="50"/> 
	    	<input name="op" type="hidden" value="Go"/>
	    	<input type="submit" value="Go"/>
	    	 <%
    		}
    		else
    		{
    		%>
    		<input id="url_setUrl" type="hidden" name="url" value="<%= _url %>" size="50"/> 
	    	<input name="op" type="hidden" value="Go"/>
	    	<%
    		}
    		%>
	    	</form>
    	</td>
    </tr>
    <%
    if(request.getAttribute("hide_new_windows") == null)
    {
    %>
    <tr>
    	<td>
            <a target="_new" href="<%= _url %>">Open in new window</a>
        </td>
    </tr>
    <%
    }
    %>
    <tr>
        <td>
        	<style><%=_css %></style>
            <div id="browserPortletDiv" src="<%= _url %>" style="overflow:auto;height:<%= request.getAttribute("height") %>;width:<%= request.getAttribute("width") %>;">
	            <%= _html %>
            </div>
        </td>
    </tr>
    <tr>
    	<td>
    		<form id="calldoSelection" action="javascript:getDatas4Annotation();">
    		<input type="submit" value="select" /> Enregistrer la sélection pour Annotation
    		</form>
    	</td>
    </tr>
    <tr>
    	<td>
    		<form id="Page" method="post" action="<portlet:actionURL/>">
    		<input id="url_page" type="hidden" name="url" value="<%= _url %>" />
    		<input name="op" type="hidden" value="Page"/>
	    	<input type="submit" value="page entière"/> Enregistrer la page entière pour Annotation
    		</form>
    	</td>
    </tr>
</table>

			<form id="doSelection" method="post" action="<portlet:actionURL/>">
    		<input name="op" type="hidden" value="Selection"/>
    		<input id="url_doSelection" type="hidden" name="url" value="<%= _url %>" />
    		<input id="text_selection" type="hidden" name="text_selection" value="rien de sélectionné" />
    		<input id="xpointer_start" type="hidden" name="xpointer_start" value="-2" />
    		<input id="xpointer_end" type="hidden" name="xpointer_end" value="-2" />
    		</form>
    	
<script src="<%=request.getContextPath()%>/javascript/jquery.js"></script>    		
<script  type="text/javascript" src="<%=request.getContextPath()%>/javascript/selection.js">
</script>
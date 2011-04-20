<%@ page language="java" extends="org.jboss.portal.core.servlet.jsp.PortalJsp" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<!--
/**
 * User: Chris Mills (millsy@jboss.com)
 * Date: 27-Feb-2006
 * Time: 22:02:11
 */
-->
<portlet:defineObjects/>

<table width="<%= request.getAttribute("iframewidth") %>">
    <tr>
        <td>
            <a target="_new" href="<%= request.getAttribute("iframeurl") %>"><center>Open in new window</center></a>
        </td>
    </tr>
    <tr>
        <td>
            <iframe src="<%= request.getAttribute("iframeurl") %>" width="<%= request.getAttribute("iframewidth") %>" height="<%= request.getAttribute("iframeheight") %>" border="0">
	            Your browser does not support iframes
            </iframe>
        </td>
    </tr>
</table>
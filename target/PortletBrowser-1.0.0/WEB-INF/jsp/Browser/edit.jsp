<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>

<form method="post" action="<portlet:actionURL/>">
	<table>
        <tr class="portlet-msg-alert">
            <td colspan="2"><%= request.getParameter("message") != null ?  request.getParameter("message") : ""%></td>
        </tr>
        <tr class="portlet-section-body">
			<td>Browser Message</td>
			<td><input type="text" name="nomessage" value="<%= request.getAttribute("message") %>" size="50"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td>Source URL</td>
			<td><input type="text" name="default-url" value="<%= request.getAttribute("default_url") %>" size="50"/></td>
		</tr>
		<tr class="portlet-section-body">
			<td>Hide URL</td>
			<td><input type="radio" name="hide" value="hide">Hide</td>
		</tr>
		<tr class="portlet-section-body">
			<td>Hide Link new windows</td>
			<td><input type="radio" name="hide_open" value="hide">Hide</td>
		</tr>
        <tr class="portlet-section-body">
			<td>Height (px)</td>
			<td><input type="text" name="height" value="<%= request.getAttribute("height") %>"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td>Width (px or %)</td>
			<td><input type="text" name="width" value="<%= request.getAttribute("width") %>"/></td>
		</tr>
        <tr class="portlet-section-body">
			<td align="right"><input type="submit" name="op" value="Update"/></td>
            <td align="left"><input type="submit" name="op" value="Cancel"/></td>
        </tr>
    </table>
</form>
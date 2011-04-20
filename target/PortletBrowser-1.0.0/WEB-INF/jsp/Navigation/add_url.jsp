<%@ page language="java"
import ="java.util.List"
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>

	<table>
	<form method="post" action="<portlet:actionURL/>">
	<input name="op" type="hidden" value="add_url"/>
	<tr class="portlet-section-body">
			<td>Add URL : </td>
			<td><input type="text" name="new_url" value="" size="50"/></td>
			<td>Add name : </td>
			<td><input typt="text" name="new_name" value="" size="50"/></td>
			<td>
				<input type="submit" value="add" />Ajouter cette url
			</td>
	</tr>
	</form>
	<%
	List<String> urls = (List<String>)request.getAttribute("urls");
	List<String> names = (List<String>)request.getAttribute("names");
	if(urls!= null && urls.size() > 0)
	{
		int nb_urls = urls.size() ;
		for(int i = 0 ; i < nb_urls ; i ++)
		{
			%>
			<tr class="portlet-section-body">
				<td>URL : </td>
				<td><%=urls.get(i)%></td>
				<td>Name : </td>
				<td><%=names.get(i)%></td>
				<td>
					<form id="doDeleteURL" method="post" action="<portlet:actionURL/>">
					<input name="op" type="hidden" value="delete_url" />
					<input name="indice" type="hidden" value="<%="" + i%>" />
					<input type="submit" value="Supprimer" />Enlever cette URL
					</form> 
				</td>
			</tr>
			<%
		}
	}
	 %>
	</table>
</form>
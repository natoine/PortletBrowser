<%@ page language="java"
import ="java.util.List"
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>
	<%
	List<String> urls = (List<String>)request.getAttribute("urls");
	List<String> names = (List<String>)request.getAttribute("names");
	if(urls!= null && urls.size() > 0)
	{
		int nb_urls = urls.size() ;
		for(int i = 0 ; i < nb_urls ; i ++)
		{
			%>
			<div class=navigation-item>
			<a href="<portlet:actionURL><portlet:param name='op' value='go_url'/><portlet:param name='url' value='<%=urls.get(i)%>'/></portlet:actionURL>"><%=names.get(i)%></a>
			</div>
			<%
		}
	}
	%>
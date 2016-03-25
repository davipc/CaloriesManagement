<%@ page session="true"%>

<%@ page import="com.toptal.calories.web.constants.SessionContextParameters" %>
<%@ page import="com.toptal.calories.web.constants.ApplicationPaths" %>

User '<%=request.getSession().getAttribute(SessionContextParameters.USER_ATTRIBUTE)%>' has been logged out.

<% session.invalidate();

String contextPath = request.getContextPath();
response.sendRedirect(contextPath + ApplicationPaths.LOGIN_PATH);
%>
<br/><br/>

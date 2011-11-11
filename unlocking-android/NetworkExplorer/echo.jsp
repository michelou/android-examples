<%@page import="java.util.*"%>
<%@page import="java.io.*"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <title>Echo</title>
</head>
<body>
<%
out.println("   <br />&nbsp;Request Explorer<br /><hr />");

String method = request.getMethod();
out.println("   <br />&nbsp;Request method: " + method + "<br />");

if (method.equalsIgnoreCase("GET")) {
   out.println("   <br />&nbsp;Request parameters");

   Enumeration paramNames = request.getParameterNames();
   if (paramNames == null || !paramNames.hasMoreElements()) {
       out.println("   <br />&nbsp;no parameters");
   }

   while(paramNames.hasMoreElements()) {
      String paramName = (String) paramNames.nextElement();
      String paramValue = (String) request.getParameter(paramName);
      out.println("   <br />&nbsp;&nbsp;&nbsp;paramName:" + paramName + "  |   paramValue:" + paramValue);
   }
} else if (method.equalsIgnoreCase("POST")) {
   out.println("   <br />&nbsp;Request body");

   BufferedReader reader = request.getReader();
   String line = "";
   while ((line = reader.readLine()) != null) {
      out.println("   <br />&nbsp;&nbsp;&nbsp;line = " + line);
   }
}

%>
</body>
</html>
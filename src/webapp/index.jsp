<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2019-04-27
  Time: 17:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>index</title>
</head>
<body>
<%
    response.sendRedirect(request.getContextPath() + "/sys/index");
%>
</body>
</html>

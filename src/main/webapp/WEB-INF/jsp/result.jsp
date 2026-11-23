<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="ru.qwuadrixx.webprog.dto.PointResult" %>
<%
    String error = (String) request.getAttribute("error");
    PointResult result = (PointResult) request.getAttribute("result");
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>лаб 3 — результат</title>
    <link rel="stylesheet" href="<%= contextPath %>/static/style.css">
</head>
<body>
<div class="header-cell">
    <span class="header-item">Поветин Захарий</span>
    <span class="header-item">Группа P3216</span>
    <span class="header-item">Вариант 343</span>
</div>

<% if (error != null) { %>
<div id="check-result" data-status="error" style="display:none"></div>
<p class="error"><%= error %></p>
<% } else { %>
<div id="check-result"
     data-status="ok"
     data-x="<%= result.getX() %>"
     data-y="<%= result.getY() %>"
     data-r="<%= result.getR() %>"
     data-hit="<%= result.getHit() %>"
     data-request-time="<%= result.getRequestTime() %>"
     style="display:none"></div>
<table class="result-table">
    <tr><th>X</th><th>Y</th><th>R</th></tr>
    <tr><td><%= result.getX() %></td><td><%= result.getY() %></td><td><%= result.getR() %></td></tr>
</table>
<p class="verdict <%= result.getHit() ? "hit" : "miss" %>">
    <%= result.getHit() ? "Точка попадает в область" : "Точка не попадает в область" %>
</p>
<% } %>
<p><a href="<%= contextPath %>/points">Новая проверка</a></p>
</body>
</html>

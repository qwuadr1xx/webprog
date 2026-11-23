<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="ru.qwuadrixx.webprog.dto.PointResult" %>
<%@ page import="ru.qwuadrixx.webprog.storage.PointsStorage" %>
<%
    PointsStorage pointsStorage = (PointsStorage) application.getAttribute("pointsStorage");
    List<PointResult> history = pointsStorage.getAll();
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>лаб 3</title>
    <link rel="stylesheet" href="<%= contextPath %>/static/style.css">
</head>
<body>
<table id="page">
    <tr>
        <td colspan="2" class="header-cell">
            <span class="header-item">Поветин Захарий</span>
            <span class="header-item">Группа P3216</span>
            <span class="header-item">Вариант 343</span>
        </td>
    </tr>
    <tr>
        <td><canvas id="area" width="300" height="300"></canvas></td>
        <td>
            <form id="point-form" method="get" action="<%= contextPath %>/points" novalidate>
                <table>
                    <tr>
                        <td>X:</td>
                        <td class="x-group">
                            <label><input type="radio" name="x" value="-2"> -2</label>
                            <label><input type="radio" name="x" value="-1.5"> -1.5</label>
                            <label><input type="radio" name="x" value="-1"> -1</label>
                            <label><input type="radio" name="x" value="-0.5"> -0.5</label>
                            <label><input type="radio" name="x" value="0"> 0</label>
                            <label><input type="radio" name="x" value="0.5"> 0.5</label>
                            <label><input type="radio" name="x" value="1"> 1</label>
                            <label><input type="radio" name="x" value="1.5"> 1.5</label>
                            <label><input type="radio" name="x" value="2"> 2</label>
                        </td>
                    </tr>
                    <tr>
                        <td>Y:</td>
                        <td><input type="text" id="y-input" name="y" placeholder="-3..5" maxlength="6"></td>
                    </tr>
                    <tr>
                        <td>R:</td>
                        <td class="r-group" id="r-checkboxes">
                            <label><input type="checkbox" name="r" value="1" onchange="onRChange(this)"> 1</label>
                            <label><input type="checkbox" name="r" value="2" onchange="onRChange(this)"> 2</label>
                            <label><input type="checkbox" name="r" value="3" onchange="onRChange(this)"> 3</label>
                            <label><input type="checkbox" name="r" value="4" onchange="onRChange(this)"> 4</label>
                            <label><input type="checkbox" name="r" value="5" onchange="onRChange(this)"> 5</label>
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>
                            <button type="submit">Проверить</button>
                        </td>
                    </tr>
                </table>
                <div id="error"></div>
            </form>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <table id="results">
                <thead>
                <tr><th>X</th><th>Y</th><th>R</th><th>Результат</th><th>Время</th></tr>
                </thead>
                <tbody id="results-body"></tbody>
            </table>
        </td>
    </tr>
</table>
<script>
    var POINTS = [
<%
    for (int i = 0; i < history.size(); i++) {
        PointResult p = history.get(i);
%>
        { x: <%= p.getX() %>, y: <%= p.getY() %>, r: <%= p.getR() %>, hit: <%= p.getHit() %>, requestTime: <%= p.getRequestTime() %> }<%= i < history.size() - 1 ? "," : "" %>
<%
    }
%>
    ];
</script>
<script src="<%= contextPath %>/static/script.js"></script>
</body>
</html>

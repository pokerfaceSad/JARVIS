<%@page contentType="text/html; charset=UTF-8"%>

<html>
	<body>
		<form action="<%=request.getContextPath()%>/JARVIS" method="post">
			<input type="radio" name="order" value="shutdown">关机
			<input type="radio" name="order" value="screenshot">截图
			<tr><button id="submit"  >执行
		</form>
	</body>
</html>
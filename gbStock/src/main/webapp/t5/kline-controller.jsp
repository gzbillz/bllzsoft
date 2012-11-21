<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	String basePath = request.getContextPath();
%>
<!doctype html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>K线控制器</title>
		<script src="<%=basePath%>/libs/util.js"></script>
	    <script src="<%=basePath%>/libs/controller.js" type="text/javascript" ></script>
	    <script type="text/javascript">
	    	function canvasDraw(){
	    		var c = new controller('canvas', {
	  	          region: { x: 0.5, height: 60, y: 400.5, width: 599, borderColor: 'black' },
	  	          bar: { width: 20, height: 35, borderColor: 'black', fillColor: 'lightgray' },
	  	          value: { left: 90, right: 100 },
	  	          minBarDistance: 20,
	  	          onPositionChanged: function (changeToValue) {
	  	              setDebugMsg('left = ' + changeToValue.left + ',right = ' + changeToValue.right);
	  	          },
	  	          touchFaultTolerance:20
	  	      });
	  	
	  	      c.drawControllerPart();
	  	      c.addControllerEvents();
	    	}
	  </script>
	</head>
<body onload="canvasDraw()">
	<canvas id="canvas" width="600" height="500" ></canvas>  
  	<div id="debug"></div>
    
</body>
</html>
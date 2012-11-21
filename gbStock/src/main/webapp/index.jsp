<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String basePath = request.getContextPath();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>股票行情图案例</title>
<style type="text/css">
BODY,A,P,SPAN,TD {
	font-family: Arial, "宋体";
	font-size: 14px;
}

h1 {
	font-family: "微软雅黑", "黑体";
	font-size: 18px;
	margin: 10px 0 25px 10px;
	border-bottom: 1px solid #f0f0f0;
	padding: 10px 20px 10px 10px;
}

h2 {
	font-family: "微软雅黑", "黑体";
	font-size: 16px;
	margin: 10px 0 5px 2px;
	padding: 8px 0px 0px 2px;
}

pre {
	padding: 60px 0 0 30px;
	color: gray;
	font-size: .8em;
	line-height: 20px;
}
</style>
</head>
<body>
	<h1>
		<span style="float: right"></span> Html5 版股票行情图
	</h1>
	<p>
		<font color="red"><i>请使用支持html5的浏览器查看，ie6，7，8都不支持html5</i> </font>
	</p>
	<h2>
		<a name="K线图_滑块控制"></a>K线图 滑块控制<a href="#K线图_滑块控制" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
			<img src="<%=basePath%>/images/example/k-controller.JPG" />
		</a>
	</p>
	<h2>
		<a name="K线图_触摸控制"></a>K线图 触摸控制<a href="#K线图_触摸控制" class="section_anchor"></a>
	</h2>
	<p>
		<i>请使用ipad体验效果</i><br /> 
		<a href="#" title="点击查看">
			<img src="<%=basePath%>/images/example/k-touch.JPG" />
		</a>
	</p>

	<h2>
		<a name="大分时图"></a>大分时图<a href="#大分时图" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
			<img src="<%=basePath%>/images/example/big-min.JPG" />
		</a>
	</p>
	<h2>
		<a name="小分时图"></a>小分时图<a href="#小分时图" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
			<img src="<%=basePath%>/images/example/mini-min.JPG" />
		</a>
	</p>
	<h2>
		<a name="交易分析图"></a>交易分析图<a href="#交易分析图" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
			<img src="<%=basePath%>/images/example/dailyTradeAnalysis.JPG" />
		</a>
	</p>
	<h2>
		<a name="成交额分析图"></a>成交额分析图<a href="#成交额分析图" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
			<img src="<%=basePath%>/images/example/dealAmountAnalysis.JPG" />
		</a>
	</p>
	<h2>
		<a name="交易分析图"></a>交易分析图<a href="#交易分析图" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
			<img src="<%=basePath%>/images/example/moneyFlow.JPG" />
		</a>
	</p>
	<h2>
		<a name="饼图"></a>饼图<a href="#饼图" class="section_anchor"></a>
	</h2>
	<p>
		<a href="#" title="点击查看"> 
		<img src="<%=basePath%>/images/example/pie.JPG" /> </a>
	</p>
</body>
</html>
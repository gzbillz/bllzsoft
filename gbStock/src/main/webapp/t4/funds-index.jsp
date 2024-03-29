<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	String basePath = request.getContextPath();
%>
<!doctype html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>基金单位净值指数对比图</title>
		<style type="text/css">
        	.menu{
        		display:none
       		}
        	.menuItem{
        		border: 1px solid #666;
        		color: #666;display: block;font-size: 9pt;
        		font-family: Arial,宋体;margin: 2px;
        		text-decoration: none;line-height: 16px;text-align: center;
        		}
        	.focus{
        		background: #FFCC66;
       		}
    	</style>
    
		<script src="<%=basePath%>/libs/util.js"></script>
        <script src="<%=basePath%>/libs/loading.js"></script>
        <script src="<%=basePath%>/libs/ajax.js"></script>
        <script src="<%=basePath%>/libs/xmlparser.js"></script>
        <script src="<%=basePath%>/libs/absPainter.js"></script>
        <script src="<%=basePath%>/libs/crossLines.js"></script>
        <script src="<%=basePath%>/libs/chartEventHelper.js"></script>
        <script type="text/javascript">
	        /*
	         *	html5行情图库 
	        */
            function Tip(options) {
                this.options = options;
                this.canvas = options.canvas;
                this.canvas.tip = this;
            }

            Tip.prototype = {
                show: function (relativePoint, html) {
                    var dc = this.dataContext;
                    var painter = this.canvas.painter;
                    if (dc) {
                        if (dc.isNewQuote) painter.paintTitle();
                        else {
                            painter.paintTitle( dc.dataItem);
                        }
                    }
                },
                update: function (relativePoint, html) {
                    this.show(relativePoint, html);
                },
                hide: function () {
                    var painter = this.canvas.painter;
                    painter.paintTitle();
                }
            };

            function FundIndex(canvasId, options) {
                this.canvas = $id(canvasId);
                this.canPaint = (this.canvas.getContext);
                if (this.canPaint) {
                    this.ctx = this.canvas.getContext('2d');
                }
                extendObject(options, this);
            }

            FundIndex.prototype = {
                paintTitle: function (dataItem) {
                    var dc = this.dataContext;
                    dataItem = dataItem || dc.dataItems[dc.dataItems.length - 1];
                    var txt = '最新' + toMoney(dataItem.unitNetValue);
                    var ctx = this.ctx;
                    var options = this.titleOptions;
                    var region = options.region;
                    ctx.clearRect(region.x, region.y, region.width, region.height);
                    ctx.font = options.font;
                    ctx.fillStyle = this.normalColor;
                    var width = ctx.measureText(txt).width;
                    ctx.textAlign = 'left';
                    ctx.textBaseline = 'top';
                    ctx.fillText(txt, region.x, region.y);
                    var price = dataItem.unitNetValue;
                    var isRise = price > dc.firstValue;
                    var isEqual = price == dc.firstValue;
                    var isFall = price < dc.firstValue;
                    var diff = toMoney(price - dc.firstValue);
                    var txtRiseFall = (isRise ? '↑' : (isFall ? '↓' : '')) + diff + ('(') + toMoney(diff * 100 / dc.firstValue) + '%)';
                    ctx.fillStyle = isRise ? this.riseColor : (isFall ? this.fallColor : this.normalColor);
                    var riseFallWidth = ctx.measureText(txtRiseFall);
                    ctx.fillText(txtRiseFall, region.x + width + 2, region.y);


                    ctx.fillStyle = this.normalColor;
                    var txtTime = dataItem.date.substring(2);
                    var timeWidth = ctx.measureText(txtTime).width;
                    ctx.fillText(txtTime, region.width - timeWidth, region.y);
                },
                paintLegend: function () {
                    /*
                    legendOptions:
                    { region: {x:1.5,y:140,height:16,width:153}, fontColor:'black',font:'9pt 宋体' ,barHeight:6px,barWidth:12}
                    */
                    var options = this.legendOptions;
                    var ctx = this.ctx;
                    var region = options.region;
                    var x = region.x;
                    var y = region.y + (region.height - options.barHeight) / 2;
                    ctx.fillStyle = this.chartOptions.fundLineColor;
                    ctx.beginPath();
                    ctx.fillRect(x, y, options.barWidth, options.barHeight);

                    var fontX = x + options.barWidth + options.spaceBetweenBarAndText;
                    var fontY = region.y + region.height;
                    ctx.fillStyle = this.normalColor;
                    ctx.font = options.font;
                    ctx.textAlign = 'left';
                    ctx.textBaseline = 'bottom';
                    ctx.fillText('单位净值', fontX, fontY);

                    var txt = '上证指数';
                    var fontWidth = ctx.measureText(txt).width;

                    fontX = region.x + region.width - fontWidth;
                    ctx.fillText(txt, fontX, fontY);

                    x = region.x + region.width - (options.spaceBetweenBarAndText + options.barWidth + fontWidth);
                    ctx.fillStyle = this.chartOptions.indexLineColor;
                    ctx.beginPath();
                    ctx.fillRect(x, y, options.barWidth, options.barHeight);
                },
                paintChart: function () {
                    this.paintChartBackground();
                    var dc = this.dataContext;
                    var ctx = this.ctx;
                    var options = this.chartOptions;
                    var region = options.region;
                    ctx.beginPath();
                    ctx.strokeStyle = options.fundLineColor;
                    var count = dc.dataItems.length;
                    dc.dataItems.each(function (item, arr, i) {
                        var x = region.x + (region.width * i / (count - 1));
                        var y = region.y + (dc.maxUnitNetValue - item.unitNetValue) * region.height / (dc.maxUnitNetValue - dc.minUnitNetValue);
                        if (i == 0) {
                            ctx.moveTo(x, y);
                        } else {
                            ctx.lineTo(x, y);
                        }
                    });
                    ctx.stroke();

                    ctx.beginPath();
                    ctx.strokeStyle = options.indexLineColor;
                    var count = dc.dataItems.length;
                    dc.dataItems.each(function (item, arr, i) {
                        var x = region.x + (region.width * i / (count - 1));
                        var y = region.y + (dc.maxIndexValue - item.indexValue) * region.height / (dc.maxIndexValue - dc.minIndexValue);
                        if (i == 0) {
                            ctx.moveTo(x, y);
                        } else {
                            ctx.lineTo(x, y);
                        }
                    });
                    ctx.stroke();
                },
                fixLinePosition: function (val) {
                    val = Math.round(val * 10);
                    if (val % 10 > 5) val += (val % 10 + 5);
                    else val -= (val % 10 + 5)
                    return val / 10;
                },
                paintChartBackground: function () {
                    var options = this.chartOptions;
                    var region = options.region;

                    var ctx = this.ctx;
                    ctx.strokeStyle = options.borderColor;
                    ctx.lineWidth = 1;
                    ctx.beginPath();
                    ctx.rect(region.x, region.y, region.width, region.height);
                    ctx.stroke();

                    var dc = this.dataContext;
                    var referenceY = this.fixLinePosition(
                        region.y + (dc.maxUnitNetValue - dc.firstValue) * region.height / (dc.maxUnitNetValue - dc.minUnitNetValue));
                    ctx.strokeStyle = options.splitLine.referenceColor;
                    ctx.beginPath();
                    ctx.moveTo(region.x, referenceY);
                    ctx.lineTo(region.x + region.width, referenceY);
                    ctx.stroke();

                    var splitSpaceHeight = Math.ceil(region.height / options.splitLine.spaceCount);
                    ctx.strokeStyle = options.splitLine.color;
                    var i = 1;
                    do {
                        var lineY = this.fixLinePosition(referenceY - i * splitSpaceHeight);
                        if (lineY > region.y) {
                            ctx.beginPath();
                            ctx.moveTo(region.x, lineY);
                            ctx.lineTo(region.x + region.width, lineY);
                            ctx.stroke();
                            i++;
                            continue;
                        }
                        break;
                    } while (true);

                    var i = 1;
                    do {
                        var lineY = this.fixLinePosition(referenceY + i * splitSpaceHeight);
                        if (lineY < region.y + region.height) {
                            ctx.beginPath();
                            ctx.moveTo(region.x, lineY);
                            ctx.lineTo(region.x + region.width, lineY);
                            ctx.stroke();
                            i++;
                            continue;
                        }
                        break;
                    } while (true);
                },
                paintDate: function () {
                    var options = this.dateAxisOptions;
                    var dc = this.dataContext;
                    var firstDate = dc.dataItems[0].date.substring(2);
                    var lastDate = dc.dataItems[dc.dataItems.length - 1].date.substring(2);
                    var ctx = this.ctx;
                    var region = options.region;
                    var x = region.x;
                    var y = region.y;
                    ctx.textAlign = 'left';
                    ctx.textBaseline = 'bottom';
                    ctx.fillStyle = options.color;
                    ctx.font = options.font;
                    ctx.fillText(firstDate, x, y);
                    var lastDateWidth = ctx.measureText(lastDate).width;
                    x = region.x + region.width - lastDateWidth;
                    ctx.fillText(lastDate, x, y);
                },
                appendLinks: function () {
                    var linksDivId = this.canvas.id + '_links';
                    var o = $id(linksDivId);
                    var region = this.linksOptions.region;
                    var canvasPosition = getPageCoord(this.canvas);
                    o.style.left = (canvasPosition.x + region.x) + 'px';
                    o.style.top = (canvasPosition.y + region.y) + 'px';
                    o.style.width = region.width + 'px';
                    o.style.height = region.height + 'px';
                    o.style.display = 'block';
                    o.style.position = 'absolute';
                    o.style.zIndex = 1 + (this.canvas.style.zIndex || 2);
                },
                updateUrl: function (dataUrl, openUrl) {
                    this.dataUrl = dataUrl;
                    if (openUrl) this.openUrl = openUrl;
                    this.paint();
                },
                parseAjaxResponse: function (ajax) {
                    var me = this;
                    var xml = ajax.responseXML;
                    var parser = new XmlParser(xml);
                    var nodes = parser.getNodes('/DataSet/Data');
                    me.dataContext = {};
                    var dataItems = [];
                    var maxUnitNetValue, minUnitNetValue;
                    var maxIndexValue, minIndexValue;
                    nodes.each(function (item, arr, index) {
                        var data = {};
                        data.date = parser.getChildValue(item, 'fld_enddate');
                        data.unitNetValue = parseFloat(parser.getChildValue(item, 'fld_unitnetvalue'));
                        data.netValue = parseFloat(parser.getChildValue(item, 'fld_netvalue'));
                        data.indexValue = parseFloat(parser.getChildValue(item, 'fld_newprice'));
                        if (index == 0) {
                            maxUnitNetValue = data.unitNetValue;
                            minUnitNetValue = data.unitNetValue;
                            maxIndexValue = minIndexValue = data.indexValue;
                        } else {
                            maxUnitNetValue = Math.max(data.unitNetValue, maxUnitNetValue);
                            minUnitNetValue = Math.min(data.unitNetValue, minUnitNetValue);
                            maxIndexValue = Math.max(data.indexValue, maxIndexValue);
                            minIndexValue = Math.min(data.indexValue, minIndexValue);
                        }
                        dataItems.push(data);
                    });
                    dataItems.reverse();
                    me.dataContext.dataItems = dataItems;
                    me.dataContext.firstValue = dataItems[0].unitNetValue;
                    me.dataContext.firstIndexValue = dataItems[0].indexValue;

                    var fallRateNetValue = (dataItems[0].unitNetValue - minUnitNetValue) / dataItems[0].unitNetValue;
                    var fallRateIndexValue = (dataItems[0].indexValue - minIndexValue) / dataItems[0].indexValue;

                    if (fallRateIndexValue > fallRateNetValue) {
                        minUnitNetValue = dataItems[0].unitNetValue * (1 - fallRateIndexValue);
                    } else {
                        minIndexValue = dataItems[0].indexValue * (1 - fallRateNetValue);
                    }

                    var riseRateNetValue = (maxUnitNetValue - dataItems[0].unitNetValue) / dataItems[0].unitNetValue;
                    var riseRateIndexValue = (maxIndexValue - dataItems[0].indexValue) / dataItems[0].indexValue;
                    if (riseRateIndexValue > riseRateNetValue) {
                        maxUnitNetValue = dataItems[0].unitNetValue * (1 + riseRateIndexValue);
                    } else {
                        maxIndexValue = dataItems[0].indexValue * (1 + riseRateNetValue);
                    }
                   
                    me.dataContext.maxUnitNetValue = maxUnitNetValue;
                    me.dataContext.minUnitNetValue = minUnitNetValue;
                    me.dataContext.maxIndexValue = maxIndexValue;
                    me.dataContext.minIndexValue = minIndexValue;

                     /*
                    var changeRateNetValue = (maxUnitNetValue - minUnitNetValue) / maxUnitNetValue;
                    var changeRateIndexValue = (maxIndexValue - minIndexValue) / maxIndexValue;
                    if (changeRateIndexValue > changeRateNetValue) {
                    me.dataContext.minUnitNetValue = maxUnitNetValue - maxUnitNetValue * changeRateIndexValue;
                    } else {
                    me.dataContext.minIndexValue = maxIndexValue - maxIndexValue * changeRateNetValue;
                    }*/

                    me.dataContext.lastValue = dataItems[dataItems.length - 1].unitNetValue;
                },
                clear: function () {
                    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
                },
                addEvents: function () {
                    var me = this;
                    if (me.hasAddEvents) return;
                    var canvas = me.canvas;
                    var options = me.chartOptions;
                    var region = options.region;
                    function getY(x) {
                        var dc = me.dataContext;
                        var dataItems = dc.dataItems;
                        var count = dataItems.length;
                        var index = Math.ceil((x - region.x) / region.width * (count - 1));
                        var val;
                        var isNewQuote;
                        var dataItem;
                        if (index >= 0 && index < count) {
                            dataItem = dataItems[index];
                            val = dataItem.unitNetValue;
                            isNewQuote = false;
                        } else {
                            val = dc.lastValue;
                            isNewQuote = true;
                        }
                        if (canvas.tip) {
                            canvas.painter = me;
                            canvas.tip.dataContext = { index: index, dataItem: dataItem, isNewQuote: isNewQuote };
                        }
                        return region.y + (dc.maxUnitNetValue - val) * region.height / (dc.maxUnitNetValue - dc.minUnitNetValue);
                    }

                    function getX(x) {
                        var dc = me.dataContext;
                        var dataItems = dc.dataItems;
                        var count = dataItems.length;
                        var index = Math.ceil(x / region.width * (count - 1));
                       
                        if (index < 0) {
                        	index =0;
                        }
                        if (index >= count) index = count - 1;
                        return region.x + (region.width * index / (count - 1));
                    }

                    //添加鼠标事件
                    addCrossLinesAndTipEvents(canvas, {
                        getCrossPoint: function (ev) { var x = getX(ev.offsetX); var y = getY(ev.offsetX); /*setDebugMsg('crossPoint:x=' + x+',y='+y);*/return { x: x, y: y }; },
                        triggerEventRanges: { x: region.x, y: region.y, width: region.width - 1, height: region.height },
                        tipOptions: {
                            getTipHtml: function (ev) { return null; },
                            position: { x: false, y: false }
                        },
                        crossLineOptions: {
                            color: 'black'
                        },
                        onClick: function () {
                            window.open(me.openUrl);
                        }
                    });
                    me.hasAddEvents = true;
                },
                doPaint: function () {
                    var me = this;
                    me.clear();
                    me.paintTitle();
                    me.paintChart();
                    me.paintDate();
                    me.paintLegend();
                    me.appendLinks();
                    me.addEvents();
                },
                paint: function () {
                    if (!this.canPaint) return;
                    var me = this;
                    Ajax.get(me.dataUrl, function (ajax) {
                        me.parseAjaxResponse.call(me, ajax);
                        me.doPaint.call(me);
                    }, me.canvas.id, true);
                }
            };

        </script>
        <!--don't combine-->
        <script>
            var painter;

            addLoadEvent(
                function () {
                    if (!painter) {
                        painter = new FundIndex('canvasFund', {
                            riseColor: 'red', fallColor: 'green', normalColor: 'black',
                            dataUrl: 'openfundnetvalue.xml', openUrl: 'http://jingzhi.funds.hexun.com/160808.shtml',
                            titleOptions: { region: { x: 2, y: 2, width: 212, height: 16 }, font: '9pt 宋体' },
                            chartOptions: {
                                region: { x: 2.5, y: 18.5, width: 155, height: 90 },
                                borderColor: 'black',
                                font: '9pt 宋体', fontColor: 'gray',
                                splitLine: { spaceCount: 4, color: 'lightgray', referenceColor: 'red' },
                                fundLineColor: '#E2007E',
                                indexLineColor: '#2358A6'
                            },
                            dateAxisOptions: { region: { x: 1.5, y: 123, height: 16, width: 153 }, color: 'gray', font: '7pt Arial' },
                            legendOptions: { region: {x:1.5,y:129,height:12,width:153}, fontColor:'black',font:'9pt 宋体',barHeight:5,barWidth:16,spaceBetweenBarAndText:2 },
                            linksOptions:{region:{x:158,y:16,height:120,width:56}}
                        });
                    }
                    painter.paint();
                }
            );
        </script>
	</head>
<body>
	<h2 style="color:red;">基金单位净值指数对比图</h2>
    <canvas id="canvasFund" width="214" height="146"></canvas>
    <div id="canvasFund_links" class="menu">
        <a href="http://jingzhi.funds.hexun.com/jz/" target="_blank" class="menuItem">基金净值</a>
        <a href="http://paiming.funds.hexun.com/cc/zcgtj.htm" target="_blank" class="menuItem">重仓股</a>
        <a href="http://funds.hexun.com/2009/jjsxq/index.html" target="_blank" class="menuItem">筛选器</a>
        <a href="http://bbs.hexun.com/funds/board_30_all_1_d.aspx" target="_blank" class="menuItem">基金论坛</a>
        <a href="http://funds.hexun.com/money/" target="_blank" class="menuItem">基金课堂</a>
    </div>
</body>
</html>
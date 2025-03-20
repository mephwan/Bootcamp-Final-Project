// chart.js
document.addEventListener("DOMContentLoaded", function () {
  let chart = null;
  let currentSeries = null;
  let currentChartType = null;

  // 通用图表配置
  const baseChartConfig = {
    width: 1000,
    height: 600,
    layout: {
      background: { type: "solid", color: "#000000" },
      textColor: "#ffffff",
    },
    grid: {
      vertLines: { color: "#1f1f1f" },
      horzLines: { color: "#1f1f1f" },
    },
    priceScale: {
      borderColor: "#7b5353",
    },
    timeScale: {
      borderColor: "#7b5353",
      timeVisible: true,
      secondsVisible: false,
    },
  };

  // 时间格式化函数
  function timeFormatter(type) {
    return (time) => {
      const date = new Date(time * 1000);
      if (type === 'candle') {
        return `${date.getFullYear()}/${(date.getMonth() + 1).toString().padStart(2, '0')}/${date.getDate().toString().padStart(2, '0')}`;
      }
      return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
    };
  }

  function getSelectedSymbol() {
    const selected = document.querySelector('input[name="symbol"]:checked');
    console.log('当前选中的symbol:', selected ? selected.value : '未找到元素'); // 添加调试日志
    return selected.value;
  }
  

  // 销毁现有图表
  function destroyChart() {
    if (chart) {
      chart.remove();
      chart = null;
    }
  }

  // 加载K线图
  async function loadD1Chart() {
    currentChartType = 'd1';
    destroyChart();

    chart = LightweightCharts.createChart(document.getElementById('chart-container'), {
      ...baseChartConfig,
      timeScale: {
        ...baseChartConfig.timeScale,
        tickMarkFormatter: timeFormatter('candle')
      }
    });

    currentSeries = chart.addCandlestickSeries({
      upColor: "#26a69a",
      downColor: "#ef5350",
      borderUpColor: "#26a69a",
      borderDownColor: "#ef5350",
      wickUpColor: "#26a69a",
      wickDownColor: "#ef5350",
    });

    chart.applyOptions({
      timeScale: { barSpacing: 20 }
    });

    try {
      const params = new URLSearchParams({ 
        interval: "1d",
        symbol: getSelectedSymbol() // 确保这个方法能获取到选中的值
    });
      const response = await fetch(`/v1/chart/candle?${params}`);
      const data = await response.json();
      
      const stockData = data
        .filter(item => item.date && item.open && item.high && item.low && item.close)
        .map(item => ({
          time: Math.floor(Number(item.date)),
          open: item.open,
          high: item.high,
          low: item.low,
          close: item.close,
        }))
        .sort((a, b) => a.time - b.time);

      currentSeries.setData(stockData);
      chart.timeScale().setVisibleRange({
        from: stockData[0].time,
        to: stockData[stockData.length - 1].time
      });
    } catch (error) {
      console.error("Error loading candle chart:", error);
    }
  }

  async function loadW1Chart() {
    currentChartType = 'w1';
    destroyChart();

    chart = LightweightCharts.createChart(document.getElementById('chart-container'), {
      ...baseChartConfig,
      timeScale: {
        ...baseChartConfig.timeScale,
        tickMarkFormatter: timeFormatter('candle')
      }
    });

    currentSeries = chart.addCandlestickSeries({
      upColor: "#26a69a",
      downColor: "#ef5350",
      borderUpColor: "#26a69a",
      borderDownColor: "#ef5350",
      wickUpColor: "#26a69a",
      wickDownColor: "#ef5350",
    });

    chart.applyOptions({
      timeScale: { barSpacing: 20 }
    });

    try {
      const params = new URLSearchParams({ interval: "1w" , symbol: getSelectedSymbol()});
      const response = await fetch(`/v1/chart/candle?${params}`);
      const data = await response.json();
      
      const stockData = data
        .filter(item => item.date && item.open && item.high && item.low && item.close)
        .map(item => ({
          time: Math.floor(Number(item.date)),
          open: item.open,
          high: item.high,
          low: item.low,
          close: item.close,
        }))
        .sort((a, b) => a.time - b.time);

      currentSeries.setData(stockData);
      chart.timeScale().setVisibleRange({
        from: stockData[0].time,
        to: stockData[stockData.length - 1].time
      });
    } catch (error) {
      console.error("Error loading candle chart:", error);
    }
  }

  async function loadMNChart() {
    currentChartType = 'mn';
    destroyChart();

    chart = LightweightCharts.createChart(document.getElementById('chart-container'), {
      ...baseChartConfig,
      timeScale: {
        ...baseChartConfig.timeScale,
        tickMarkFormatter: timeFormatter('candle')
      }
    });

    currentSeries = chart.addCandlestickSeries({
      upColor: "#26a69a",
      downColor: "#ef5350",
      borderUpColor: "#26a69a",
      borderDownColor: "#ef5350",
      wickUpColor: "#26a69a",
      wickDownColor: "#ef5350",
    });

    chart.applyOptions({
      timeScale: { barSpacing: 20 }
    });

    try {
      const params = new URLSearchParams({ interval: "mn" , symbol: getSelectedSymbol()});
      const response = await fetch(`/v1/chart/candle?${params}`);
      const data = await response.json();
      
      const stockData = data
        .filter(item => item.date && item.open && item.high && item.low && item.close)
        .map(item => ({
          time: Math.floor(Number(item.date)),
          open: item.open,
          high: item.high,
          low: item.low,
          close: item.close,
        }))
        .sort((a, b) => a.time - b.time);

      currentSeries.setData(stockData);
      chart.timeScale().setVisibleRange({
        from: stockData[0].time,
        to: stockData[stockData.length - 1].time
      });
    } catch (error) {
      console.error("Error loading candle chart:", error);
    }
  }

  // 加载折线图
  async function loadM5Chart() {
    currentChartType = 'm5';
    destroyChart();

    chart = LightweightCharts.createChart(document.getElementById('chart-container'), {
      ...baseChartConfig,
      timeScale: {
        ...baseChartConfig.timeScale,
        tickMarkFormatter: timeFormatter('line')
      }
    });

    currentSeries = chart.addLineSeries({
      color: "#26a69a",
      lineWidth: 1.5,
    });

    chart.applyOptions({
      timeScale: { barSpacing: 20 }
    });

    try {
      const params = new URLSearchParams({ 
        interval: "5m",
        symbol: getSelectedSymbol() // 确保这个方法能获取到选中的值
    });
      const response = await fetch(`/v1/chart/line?${params}`);
      const data = await response.json();
      
      const stockData = data
        .filter(item => item.dateTime && item.close)
        .map(item => ({
          time: Math.floor(Number(item.dateTime)),
          value: item.close,
        }))
        .sort((a, b) => a.time - b.time);

      currentSeries.setData(stockData);
      chart.timeScale().setVisibleRange({
        from: stockData[0].time,
        to: stockData[stockData.length - 1].time
      });
    } catch (error) {
      console.error("Error loading line chart:", error);
    }
  }



  function autoRefreshChart() {
    if (currentChartType === 'd1') {
      loadD1Chart();
    } else if (currentChartType === 'm5') {
      loadM5Chart();
    } else if (currentChartType === 'w1') {
      loadW1Chart();
    } else if (currentChartType === 'mn') {
      loadMNChart();
    }
  }

  // 设置定时器（新增部分）
  setInterval(autoRefreshChart, 10000);

  // 绑定按钮事件
  document.getElementById('D1').addEventListener('click', loadD1Chart);
  document.getElementById('M5').addEventListener('click', loadM5Chart);
  document.getElementById('W1').addEventListener('click', loadW1Chart);
  document.getElementById('MN').addEventListener('click', loadMNChart);

  document.querySelectorAll('input[name="symbol"]').forEach(radio => {
    radio.addEventListener('change', () => {
      autoRefreshChart();
    });
  });
  // 默认加载K线图
  loadM5Chart();
});
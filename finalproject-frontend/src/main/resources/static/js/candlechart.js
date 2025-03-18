document.addEventListener("DOMContentLoaded", function () {
  const chart = LightweightCharts.createChart(
    document.getElementById("chart-container"),
    {
      width: 1000,
      height: 600,
      layout: {
        background: { type: "solid", color: "#000000" }, // Force black background
        textColor: "#ffffff", // White text for contrast
      },
      grid: {
        vertLines: { color: "#1f1f1f" }, // Dark grid
        horzLines: { color: "#1f1f1f" },
      },
      priceScale: {
        borderColor: "#7b5353",
      },
      timeScale: {
        borderColor: "#7b5353",
        timeVisible: true, // Enable time visibility
        secondsVisible: false,
        tickMarkFormatter: (time) => {
          const date = new Date(time * 1000); // Convert Unix timestamp to milliseconds
          const year = date.getFullYear();
          const month = (date.getMonth() + 1).toString().padStart(2, "0"); // Ensure 2-digit month
          const day = (date.getDate()).toString().padStart(2, "0");
          return `${year}/${month}/${day}`;
        },
      },
    }
  );

  const candleSeries = chart.addCandlestickSeries({
    upColor: "#26a69a", // Green for bullish candles
    downColor: "#ef5350", // Red for bearish candles
    borderUpColor: "#26a69a",
    borderDownColor: "#ef5350",
    wickUpColor: "#26a69a",
    wickDownColor: "#ef5350",
  });

  chart.applyOptions({
    timeScale: {
      barSpacing: 20, // Increase or decrease to adjust the width of candles
    },
  });

  function fetchData(interval) {
  const params = new URLSearchParams({interval});
  fetch(`/v1/chart/candle?${params.toString()}`)
    .then((response) => response.json())
    .then((data) => {
      const stockData = data
        .filter(
          (item) =>
            item.date &&
            item.open !== null &&
            item.high !== null &&
            item.low !== null &&
            item.close !== null
        )
        .map((item) => ({
          time: Math.floor(Number(item.date)),
          open: item.open,
          high: item.high,
          low: item.low,
          close: item.close,
        }))
        .sort((a, b) => a.time - b.time);
      // ! lightweight-chart.js cannot handle data without date order
      console.log(stockData);
      candleSeries.setData(stockData);

      chart.timeScale().setVisibleRange({
        from: stockData[0].time, // Start from the first data point
        to: stockData[stockData.length - 1].time, // End at the last data point
      });
    })
    .catch((error) => {
      console.error("Error fetching candlestick data:", error);
    });
  }

  fetchData("1d");

  document.querySelectorAll(".interval-btn").forEach((button) => {
    button.addEventListener("click", function () {
      const interval = this.dataset.interval; // ????? data-interval ?
      fetchData(interval); // ????
    });
  });
});

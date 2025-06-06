function DoChart(jsonData,XLabel,YLabel,chart){
    // 元素构建
    var canvas = document.createElement('canvas');
    var c = document.getElementById(chart);
    c.innerHTML = "";
    c.appendChild(canvas);

    // 数据执行
    if(jsonData.length == 0){
      // 准备横坐标和纵坐标数据
      labels = ['null','null','null']; // 横坐标数据
      values = [0,0,0]; // 纵坐标数据
    }else{
      // 准备横坐标和纵坐标数据
      labels = jsonData['x']
      values = jsonData['y']
    }

    // 创建 Chart.js 配置对象
    const config = {
      type: 'line',
      data: {
        labels: labels,
        datasets: [{
          label: 'Data',
          data: values,
          borderColor: 'rgb(75, 192, 192)',
          fill: false,
          lineTension: 0.4, // 曲线的曲率，值范围 0 到 1
        }]
      },
      options: {
        responsive: false,
        maintainAspectRatio: false, // 取消维持纵横比例
        scales: {
          x: {
            display: true,
            title: {
              display: true,
              text: XLabel
            }
          },
          y: {
            display: true,
            title: {
              display: true,
              text: YLabel
            }
          }
        }
      }
    };

    // 创建图表
    const ctx = canvas.getContext('2d');
    const myChart = new Chart(ctx, config);
}



// 创建图表实例
// fetch(baseUrl + '/backend/dayOrder',{method: 'GET'})
// .then(response => {
//   if (!response.ok) {throw new Error('Network response was not ok');}return response.json();})
// .then(data => {
//   if(data != undefined && data != null){if(data.code == 1){
//       console.log(data);
//       DoChart(data.data,"昨天时间(h)","订单流量","myChart1")
//     }else{alert(data.msg);}}})
// .catch(error => {console.error('There has been a problem with your fetch operation:', error);});


// 数据准备
var hostName = "http://" + (window.location.hostname == "localhost" ? "127.0.0.1" : window.location.hostname);
var baseUrl = hostName + ":" +  window.location.port;

// 方法内容
// 1. get请求
function getData(url,func){
    fetch(baseUrl+ url,{
        method: 'GET'
      }) // 替换成后端提供数据的端点
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {func(data);})
      .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
      });
}
// 2. post请求
function postData(url,dt,func){
    fetch(baseUrl+ url,{
        method: 'POST',
        body: dt
      }) // 替换成后端提供数据的端点
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {func(data);})
    .catch(error => {
        console.error('There has been a problem with your fetch operation:', error);
    });

}
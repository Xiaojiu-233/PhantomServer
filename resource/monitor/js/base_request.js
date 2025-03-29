// 数据准备
var hostName = "http://" + (window.location.hostname == "localhost" ? "127.0.0.1" : window.location.hostname);
var baseUrl = hostName + ":" +  window.location.port;
if(localStorage.getItem("k") === null || Number(localStorage.getItem("k")) < 1)
  localStorage.setItem("k","1");

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
// 3. post带Json的请求
function postJsonData(url,dt,func){
  fetch(baseUrl+ url,{
    method: 'POST',
    body: JSON.stringify(dt)
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

// 渲染参数表格内容
function renderArgTable(theadId,tbodyId,excludeElements,data){
  var thead = document.getElementById(theadId);
  var tbody = document.getElementById(tbodyId);
  for(var exclude of excludeElements)
    delete data[exclude];
  theadNames = ["参数名","参数值"];
  // 渲染表头
  var theadContent = `<tr>`;
  for(var i=0;i<theadNames.length;i++){
    theadContent += `<th>${theadNames[i]}</th>`;
  }
  theadContent += `<tr>`;
  thead.innerHTML = theadContent;
  // 渲染表身
  var replaceData = null;
  var tbodyContent = ``;
  for(var ob in data){
    var r = false;
    if(typeof data[ob] === 'string' && data[ob].indexOf('>') !== -1 && data[ob].indexOf('<') !== -1){
      replaceData = data[ob];
      r = true;
    }
    tbodyContent += `<tr><td>${ob}</td>`;
    tbodyContent += r ? `<td id="replace">${data[ob]}</td></tr>` : `<td>${data[ob]}</td></tr>`;
  }
  tbody.innerHTML = tbodyContent;
  if(replaceData){
    var replaced = document.getElementById("replace");
    replaced.innerText = replaceData;
  }
}

// 渲染表格内容
function renderTable(theadId,tbodyId,theadNames,data,styleMapping){
  var thead = document.getElementById(theadId);
  var tbody = document.getElementById(tbodyId);
  // 渲染表头
  var theadContent = `<tr>`;
  for(var i=0;i<theadNames.length;i++){
    theadContent += `<th>${theadNames[i]}</th>`;
  }
  theadContent += `<tr>`;
  thead.innerHTML = theadContent;
  // 渲染表身
  var tbodyContent = ``;
  for(var i=0;i<data.length;i++){
    tbodyContent += `<tr>`;
    for(var j=0;j<theadNames.length;j++){
      // 数据分析
      var item = data[i][theadNames[j]];
      var style = styleMapping[item];
      // 渲染
      tbodyContent += style !== null ? `<td style="${style}">${item}</td>` : `<td>${item}</td>`;
    }
    tbodyContent += `</tr>`;
  }
  tbody.innerHTML = tbodyContent;
}

// 渲染列表内容
// 渲染表格内容
function renderList(listId,data){
  var list = document.getElementById(listId);
  // 渲染表头
  var content = ``;
  if(data.length > 0){
    for(var i=0;i<data.length;i++){
      content += `<div class="unit">
                    ${data[i]}
                </div>`;
    }
  }else{
    content = `<div class="unit">数据空空如也...</div>`;
  }

  list.innerHTML = content;
}

// 渲染选择器
function renderSelector(selector,data){
  var select = document.getElementById(selector);
  // 渲染内容
  var content = ``;
  if(data !== null){
    for(var i in data){
      if(i == 0)continue;
      content += `<option value="${i}">${data[i]}</option>`
    }
  }
  else{
    content += `<option value="1">1</option>`
  }
  select.innerHTML = content;
}

// 获取k值
function getK(){
  return Number(localStorage.getItem("k"));
}

// 设置k值
function setK(k){
  localStorage.setItem("k",k);
}

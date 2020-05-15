function refreshGraph(graphName, type, url){
    var id=graphName.lastIndexOf('_')>=0?graphName.slice(0,graphName.lastIndexOf('_')):graphName;
    var max=undefined!=document.getElementById(id+"_max")?document.getElementById(id+"_max").value:"5";
    buildChart(url//graphs[graphName]
	    .replace("{surveyId}",surveyId)    
	    .replace("{max}",max)
        , graphName, type);
}

function buildChart(url, chartElementName, type){
  var xhr = new XMLHttpRequest();
  xhr.open("GET", env.server+url, true);
  xhr.send();
  xhr.onloadend = function () {
	var raw=setColors(xhr.responseText);
	//console.log("dataset="+raw);
    var json=JSON.parse(raw);
    
    resetCanvas(chartElementName);
  
	if (type=="horizontalBar" || type=="bar"){
	    charts[chartElementName+"Chart"]=new Chart(document.getElementById(chartElementName).getContext("2d"), {
                type: type, 
                data: json,
                options:{
                		responsive: true,
                		maintainAspectRatio: false,
                		legend: {
                			display: false
                		},
                		tooltips: {
                	  	callbacks: {}
                	  }
                }
            });
	    
	    
    }else if (type=="line"){
        charts[chartElementName+"Chart"]=new Chart(document.getElementById(chartElementName).getContext("2d"), {
                 type: type, 
                 data: json,
                 options: {
               	  maintainAspectRatio: false,
               	  "scales":{
               		  "xAxes":[{
               			  "ticks":{
               				  "beginAtZero":true
               				}
               			}]
        					},
        					legend: {
        						display:true
        					}
        				}
          });
      }
  }
}

function setColors(json){
	var o=JSON.parse(json);
	for (label in o["labels"]){
		if (o["datasets"].length<=0) continue; // bad data / no datasets situation
		if (undefined==o["datasets"][0]["backgroundColor"]) continue; //ie. line graphs
		if (o["labels"][label].includes("Discovery Session")){
			o["datasets"][0]["backgroundColor"][label]="rgba(204,0,0,0.6)";
			o["datasets"][0]["borderColor"][label]    ="rgba(204,0,0,0.7)";
		}else if (o["labels"][label].includes("Sales Kit")){
			o["datasets"][0]["backgroundColor"][label]="rgba(204,0,0,0.9)";
			o["datasets"][0]["borderColor"][label]    ="rgba(204,0,0,1.0)";
		}else{
			o["datasets"][0]["backgroundColor"][label]="rgba(204,0,0,0.3)";
			o["datasets"][0]["borderColor"][label]    ="rgba(204,0,0,0.4)";
		}
	}
	return JSON.stringify(o);
}

function resetCanvas(chartElementName){
	if (undefined!=charts[chartElementName+"Chart"]) charts[chartElementName+"Chart"].destroy();
}
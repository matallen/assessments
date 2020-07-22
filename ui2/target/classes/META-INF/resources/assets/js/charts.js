
var barOptions={
		responsive: true,
		maintainAspectRatio: false,
		legend: { display: false },
		scales:{
			y: {
				beginAtZero: true,
			},
			yAxes: [{
				ticks: {
					mirror: true,
					fontSize: 18,
					padding: -10,
					callback: function(value, index, values) {
                        //return '$' + value;
						
						var val=ctx.datasets[0].data[index];
						var total=0;
	                	for (var i=0;i<ctx.datasets[0].data.length;i++)
	                		total+=ctx.datasets[0].data[i];
						var label=ctx.labels[index];
						var percentage=((val/total)*100).toFixed(2);
	                    return label+" - "+val +"  ("+percentage+"%)";
                    }
				},
			}]
		}
}


function refreshGraph(graphName, type, url, processor){
    var id=graphName.lastIndexOf('_')>=0?graphName.slice(0,graphName.lastIndexOf('_')):graphName;
    var max=undefined!=document.getElementById(id+"_max")?document.getElementById(id+"_max").value:"5";
    buildChart(url//graphs[graphName]
	    .replace("{surveyId}",surveyId)    
	    .replace("{max}",max)
        , graphName, type, processor);
}

var ctx;
function buildChart(url, chartElementName, type, dataPreProcessCallback){
  var xhr = new XMLHttpRequest();
  xhr.open("GET", env.server+url, true);
  xhr.send();
  xhr.onloadend = function () {
	  
	var raw=setColors(type, xhr.responseText);

	//console.log(chartElementName+"/"+type+"\n"+(JSON.stringify(JSON.parse(raw),null,2)));
    var json=JSON.parse(raw);
    
    resetCanvas(chartElementName);
    
    if (undefined!=dataPreProcessCallback)
    	json=processors[dataPreProcessCallback](json);
    
    ctx=json;
    
    if (type=="bar"){
    	var options={
        		responsive: true,
        		maintainAspectRatio: false,
        		legend: { display: false },
        		tooltips: {
        			callbacks: {}
        		},
        		plugins: {
    	            datalabels: {
    	            	display: false
    	            }
        		}
    		};
    }else if (type=="horizontalBar"){
    	var options={
        		responsive: true,
        		maintainAspectRatio: false,
        		legend: { display: false },
        		tooltips: {
        			callbacks: {}
        		},
        		scales: {
        			yAxes: [{
        				ticks: {mirror: true},
        				scaleLabel: {
        					display: true
        				}
        			}],
        			xAxis: [{ticks: {display: "top"}}]
        		}
    		};
    	options={
    			responsive: true,
    			maintainAspectRatio: false,
    			legend: { display: false },
    			scales:{
    				y: {
    					beginAtZero: true,
    				},
    				yAxes: [{
        				ticks: {
        					mirror: true,
        					fontSize: 18,
        					padding: -10,
        					callback: function(value, index, values) {
                                //return '$' + value;
        						
        						var val=ctx.datasets[0].data[index];
        						var total=0;
        	                	for (var i=0;i<ctx.datasets[0].data.length;i++)
        	                		total+=ctx.datasets[0].data[i];
        						var label=ctx.labels[index];
        						var percentage=((val/total)*100).toFixed(2);
        	                    return label+" - "+val +"  ("+percentage+"%)";
                            }
        				},
    				}]
    			},
    			plugins: {
    	            datalabels: {
    	                display: true,
    	                color: "#444",
    	                font: {
    	                	size: "18",
    	                },
    	                formatter: function (value, context) {
    	                    //return value + '%';
    	                	
    	                	var label=context.chart.config.data.labels[context["dataIndex"]];
    	                	var total=0;
    	                	for (var i=0;i<context["dataset"]["data"].length;i++){
    	                		total+=context["dataset"]["data"][i];
    	                	}
    	                	var percentage=((value/total)*100).toFixed(2);
    	                    return label+" - "+context["dataset"]["data"][context["dataIndex"]] +"  ("+percentage+"%)";
    	                }
    	            }
    	        },
    	};
    }
    
    if (type=="horizontalBar" || type=="bar"){
	    charts[chartElementName+"Chart"]=new Chart(document.getElementById(chartElementName).getContext("2d"), {
                type: type, 
                data: json,
                options: options
            });
	    
	    
    }else if (type=="line"){
        charts[chartElementName+"Chart"]=new Chart(document.getElementById(chartElementName).getContext("2d"), {
                 type: type, 
                 data: json,
                 options: {
                	maintainAspectRatio: false,
	               	"scales":{
		               	"xAxes":[{
		               		"display": true,
		               		"ticks":{
		               			"beginAtZero":true
		               		}
		               	}],
	               		yAxes: [{
	               			display: true,
	               		}]
					},
					legend: {
						display:false
					},
	        		plugins: { datalabels: { display: false} }
				}
          });
	  }else if (type=="pie" || type=="doughnut"){
		  charts[chartElementName+"Chart"]=new Chart(document.getElementById(chartElementName).getContext("2d"), {
              type: 'pie', 
              data: json,
              options: {
            	  maintainAspectRatio: false,
 				legend: {
 					display:true
 				},
        		plugins: { datalabels: { display: false} }
     		  }
		  });
	  }
  }
}


//["rgba(204,0,0,{alpha})","rgba(204,0,0,{alpha})","rgba(204,0,0,{alpha})"];

var colorsAvailableMain=[
	 "#EE0000" // new Red Hat Red
	,"#4C4C4C" // Grey - main
	,"#004153" // Teal - main
	,"#3B0083" // Purple - main
	,"#00B9E4" // Light Blue - main
	,"#F0AB00" // Light Orange - main
	,"#EC7A08" // Orange - main
	,"#92D400" // Light Green - main
	,"#3F9C35" // Green - main
	,"#0088CE" // Blue - main
	
]
var colorsAvailableLight=[
	 "#C10000" // Red - light
	,"#535353" // Grey - light
	,"#BFD0D4" // Teal - light
	,"#CEBFE0" // Purple - light
	,"#BEEDF9" // Light Blue - light
	,"#FBEABC" // Light Orange - light
	,"#FBDEBF" // Orange - light
	,"#E4F5BC" // Light Green - light
	,"#9ECF99" // Green - light
	,"#BEE1F4" // Blue - light
	
]
var remainingColorsAvailable=[
	"890000", // Red - med
	"460000", // Red - dark
	"404040", // Grey - med
	"2D2D2D", // Grey - dark
	"80A1AA", // Teal - med
	"40717F", // Teal - dark
	"9E80C1", // Purple - med 1
	"6D40A2", // Purple - med 2
	"2D0062", // Purple - dark
	"7CDBF3", // Light Blue - med 1
	"35CAED", // Light Blue - med 2
	"005C73", // Light Blue - dark
	"F9D67A", // Light Orange - med 1
	"F5C12E", // Light Orange - med 2
	"B58100", // Light Orange - dark
	"F7BD7F", // Orange - med 1
	"F39D3C", // Orange - med 2
	"B35C00", // Orange - dark
	"C8EB79", // Light Green - med 1
	"ACE12E", // Light Green - med 2
	"6CA100", // Light Green - dark
	"9ECF99", // Green - med 1
	"6EC664", // Green - med 2
	"2D7623", // Green - dark
	"7DC3E8", // Blue - med 1
	"39A5DC", // Blue - med 2
	"00659C"  // Blue - dark
];

var colorsAvailableMain2=[
	
	 "rgba(204, 0, 0,     {alpha})" // Red Hat Red (c10000)
//	 "rgba(193, 0, 0,     {alpha})" // Red Hat Red (c10000)
	,"rgba(0,   65,  83,  {alpha})" // Teal - main
	,"rgba(59,  0,   131, {alpha})" // Purple - main
	,"rgba(0,   185, 228, {alpha})" // Light Blue - main
	,"rgba(240, 171, 0,   {alpha})" // Light Orange - main
	,"rgba(236, 122, 8,   {alpha})" // Orange - main
	,"rgba(146, 212, 0,   {alpha})" // Light Green - main
	,"rgba(63,  156, 53,  {alpha})" // Green - main
	,"rgba(0,   136, 206, {alpha})" // Blue - main
	,"rgba(76,  76,  76,  {alpha})" // Grey - main
	
	,"rgba(137, 0,   0,   {alpha})" // Red Hat Red - med
	,"rgba(128, 161, 170, {alpha})" // Teal - med
	,"rgba(158, 128, 193, {alpha})" // Purple - med
	,"rgba(124, 219, 243, {alpha})" // Light Blue - med
	,"rgba(249, 214, 122, {alpha})" // Light Orange - med
	,"rgba(247, 189, 127, {alpha})" // Orange - med
	,"rgba(200, 235, 121, {alpha})" // Light Green - med
	,"rgba(158, 207, 153, {alpha})" // Green - med
	,"rgba(125, 195, 232, {alpha})" // Blue - med
	
	
]
//var colorsAvailable=colorsAvailableMain2;//.concat(colorsAvailableLight);

var colorAffinity={};
function setColors(type, json){
	var colorsAvailable=colorsAvailableMain2.slice();
	var o=JSON.parse(json);
	
	if (type.toLowerCase().includes("pie")){
		for (j in o["datasets"]){
			for (i in o["labels"]){
				if (o["datasets"].length<=0) continue; // bad data / no datasets situation
				if (undefined==o["datasets"][j]["backgroundColor"]) continue; //ie. line graphs
				
				if (undefined==colorAffinity[o["labels"][i]["label"]]){
					colorAffinity[o["labels"][i]]=colorsAvailable.shift();
				}
				o["datasets"][j]["backgroundColor"].push(colorAffinity[o["labels"][i]].replace("{alpha}","0.7"));
			}
		}
	
	}else if (type.toLowerCase().includes("bar") || type.toLowerCase().includes("line")){
		var target=o["datasets"];
		for (i in target){
			if (undefined==target[i]["label"]){
				console.log("ERROR: Unable to find label for dataset to assign a color to");
				return;
			}
			if (undefined==colorAffinity[target[i]["label"]]){
				colorAffinity[target[i]["label"]]=colorsAvailable.shift();
			}
			
			for (var n=0;n<=target[i]["data"].length;n++){
				target[i]["backgroundColor"][n]=colorAffinity[target[i]["label"]].replace("{alpha}","0.3");
				target[i]["borderColor"][n]=colorAffinity[target[i]["label"]].replace("{alpha}","0.4");
			}
		}
	}
	
		
		
//		o["datasets"][0]["backgroundColor"][label]=colorAffinity[o["labels"][label]].replace("{alpha}","0.7");
//		if (type!="pie")
//			o["datasets"][0]["borderColor"][label]=colorAffinity[o["labels"][label]].replace("{alpha}","0.8");
		
		
//		if (o["labels"][label].includes("Discovery Session")){
//			o["datasets"][0]["backgroundColor"][label]="rgba(204,0,0,0.6)";
//			o["datasets"][0]["borderColor"][label]    ="rgba(204,0,0,0.7)";
//		}else if (o["labels"][label].includes("Sales Kit")){
//			o["datasets"][0]["backgroundColor"][label]="rgba(204,0,0,0.9)";
//			o["datasets"][0]["borderColor"][label]    ="rgba(204,0,0,1.0)";
//		}else{
//			o["datasets"][0]["backgroundColor"][label]="rgba(204,0,0,0.3)";
//			if (type!="pie")
//				o["datasets"][0]["borderColor"][label]    ="rgba(204,0,0,0.4)";
//		}
//	}
	return JSON.stringify(o);
}

function resetCanvas(chartElementName){
	if (undefined!=charts[chartElementName+"Chart"]) charts[chartElementName+"Chart"].destroy();
}
Utils = {
	
	getParameterByName: function(name, url) {
		if (!url) url = window.location.href;
		name = name.replace(/[\[\]]/g, "\\$&");
		var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
		    results = regex.exec(url);
		if (!results) return null;
		if (!results[2]) return '';
		return decodeURIComponent(results[2].replace(/\+/g, " "));
	},
	
	getBooleanParameterByName: function(name, url){
		var paramValue=Utils.getParameterByName(name);
		return undefined!=paramValue || "true"==paramValue;
	},
	
	findAncestor: function findAncestor (el, cls) {
		while ((el = el.parentElement) && !el.classList.contains(cls));
		return el;
	},
	isValidBase64: function(str){
		if (undefined == str) return false;
		if (str ==='' || str.trim() ==='')return false;
		if (!str.endsWith("=")) return false;
	   try {
	     return btoa(atob(str)) == str;
	   }catch(err){
	     return false;
	   }
	}

}

HtmlUtils = {
	asList: function asList(selector, attr){
		var list=[];
		$(selector).each(function(){
			list.push($(this).attr(attr));
		});
		return list;
	}
}

AdobeUtils = {
	sendAdobeEvent: function(evt){
		try{
			console.log("Adobe Event: '"+evt["event"]+"'"+ (evt["page"]?" - "+evt["page"]["detailedPageName"]+"("+evt["page"]["siteLanguage"]+")":""));
			window.appEventData = window.appEventData || [];
			appEventData.push(evt);
		}catch(err){}
	}
}

LocalStorage = {
		storageName:"RHAssessmentPlatform_State",
		saveFlag: function(key, value) {
			console.log("LocalStorage:: Saving flag (k="+key+",v="+value+")...");
			window.localStorage.setItem(LocalStorage.storageName+"_"+surveyId+"_"+key, value);
		},
		getFlag: function(key) {
			var result=window.localStorage.getItem(LocalStorage.storageName+"_"+surveyId+"_"+key);
			console.log("LocalStorage:: Get flag (k="+key+") = "+result);
			return result;
		},
		removeFlag: function(key) {
			console.log("LocalStorage:: Remove flag (k="+key+")");
			window.localStorage.removeItem(LocalStorage.storageName+"_"+surveyId+"_"+key);
		},
		saveState: function(survey) {
			console.log("LocalStorage:: Saving state... (page "+survey.currentPageNo+")");
			var toStore=JSON.stringify({ currentPageNo: survey.currentPageNo, data: survey.data });
		    //console.log("saveState: "+toStore);
			window.localStorage.setItem(LocalStorage.storageName+"_"+surveyId, toStore);
		},
		clearState: function(){
			console.log("LocalStorage:: Clearing state")
			window.localStorage.removeItem(LocalStorage.storageName+"_"+surveyId);
		},
		loadState: function(survey) {
			var storageSt = window.localStorage.getItem(LocalStorage.storageName+"_"+surveyId) || "";
			console.log("loadState: "+storageSt);
			var loaded=storageSt?JSON.parse(storageSt):{ currentPageNo: 0, data: {} };
			if (loaded.data) 
			    survey.data=loaded.data;
			if (loaded.currentPageNo){
				survey.currentPageNo=loaded.currentPageNo;
			}
			return loaded;
		},
		save: function(surveyId, toStore) {
			console.log("LocalStorage:: Saving...");
			//var toStore=JSON.stringify({ currentPageNo: pageNo, data: surveyData });
			window.localStorage.setItem(LocalStorage.storageName+"_"+surveyId, toStore);
		},
}

if (!String.prototype.format) {
  String.prototype.format = function() {
    var args = arguments;
    return this.replace(/{(\d+)}/g, function(match, number) { 
      return typeof args[number] != 'undefined'
        ? args[number]
        : match
      ;
    });
  };
}

/* Dynamically load a css file */
loadCSS = function(href) {
  var cssLink = $("<link>");
  $("head").append(cssLink); //IE hack: append before setting href
  cssLink.attr({
    rel:  "stylesheet",
    type: "text/css",
    href: href
  });
};

function setMenu(setCurrent){
	var surveyId=Utils.getParameterByName("id");
	$(".menu_needsId").each(function(index){
		var a=$(this).find("a");
		var name=a.text();
		if (setCurrent==a.text()){ // current tab
			$(this).addClass("current");
			a.attr("href", "#");
		}else{ // all other active tabs
			
			var linkAlreadyHasIdSet=""!=Utils.getParameterByName("id", a.attr("href"));
			if (!linkAlreadyHasIdSet && null!=surveyId)
				a.attr("href", a.attr("href")+surveyId);
			//a.text(a.text()+surveyId);
			//$(this).removeClass("menu_needsId");
		}
	});
	replaceText({"id":surveyId==null?"NEW":surveyId});
}
function replaceText(payload){
	for (key in payload){
		$("._"+key).text(payload[key]);
	}
}

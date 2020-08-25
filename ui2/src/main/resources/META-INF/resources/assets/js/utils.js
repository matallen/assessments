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
	
	findAncestor: function findAncestor (el, cls) {
		while ((el = el.parentElement) && !el.classList.contains(cls));
		return el;
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
		}
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

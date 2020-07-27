

var config=SURVEY_CONFIG;

// Page theme
if (undefined!=config.theme && ""!=config.theme)
	loadCSS("assets/themes/"+config.theme+"/css/style.css");


var defaultThemeColors = Survey
    .StylesManager
    .ThemeColors["default"];

defaultThemeColors["$main-color"] = "#a30000";
defaultThemeColors["$main-hover-color"] = "#820000";
defaultThemeColors["$text-color"] = "#4a4a4a";
defaultThemeColors["$header-color"] = "#ffffff";
defaultThemeColors["$header-background-color"] = "#cc0000";
defaultThemeColors["$body-container-background-color"] = "#f8f8f8";
defaultThemeColors["$error-color"]="#a30000";
//defaultThemeColors["$border-color"]="#cc0000";

Survey
    .StylesManager
    .applyTheme();

Survey
	.Serializer
	.addProperty("page", {
	    name: "navigationTitle:string",
	    isLocalizable: true
});
Survey
	.Serializer
	.addProperty("page", {
		name: "navigationDescription:string",
		isLocalizable: true
});

Survey.requiredText = "AA";

Survey.defaultStandardCss.navigation.complete = "sv_complete_btn sv_rh_complete_btn";
Survey.defaultStandardCss.navigation.prev =     "sv_prev_btn     sv_rh_prev_btn";
Survey.defaultStandardCss.navigation.next =     "sv_next_btn     sv_rh_next_btn";



Survey.ChoicesRestfull.onBeforeSendRequest = function(sender, options) {
        //options.request.setRequestHeader("Content-Type", "application/json");
        //options.request.setRequestHeader("Authorization", "Bearer "+jwtToken);
};

var json = SURVEY_CONTENT;

window.survey = new Survey.Model(json);


var timeInfo=[];


var geoInfo=undefined;
if (undefined==geoInfo){
  $.ajax({
    url: "http://ip-api.com/json?fields=continentCode,country,countryCode,region",
    type: 'GET',
    success: function(json){
	  geoInfo=json;
	  console.log("GeoInfo:: Identified country: " + json.country);
    },
    error: function(err){
      console.log("GeoInfo Failed: " + err);
    }});
}


//survey.showTimerPanelMode = 'page';
survey.startTimer();
survey.showTimerPanel = 'none'; //bottom
//survey.completeText = 'View Results';


survey
      .onAfterRenderPage
      .add(function(result, options){
      })

survey
	.onCurrentPageChanged
	.add(function(sender, options){
		var page=options.oldCurrentPage;
		var timeTaken=page.survey.koTimerInfoText();
		if (""==timeTaken) return;
		var expr= /.+spent (.+?) on this page and (.+?) in total./g;
		var match=expr.exec(timeTaken);
		timeInfo[page.name]=match[1];
		console.log("Metrics:: sending page message: page "+ page.name+" - "+timeInfo[page.name]);
    	
		LocalStorage.saveState(survey);
		
		Http.httpPost(env.server+"/api/surveys/"+surveyId+"/metrics/"+page.name+"/onPageChange?visitorId="+Cookie.get("rhae-visitorId"), buildPageChangePayload(page));
	});
		
survey
    .onComplete
    .add(function (result) {
    	var page=result.currentPageValue;
    	console.log("Metrics:: sending survey complete message");
    	
		var timeTaken=result.currentPageValue.survey.koTimerInfoText();
		if (""==timeTaken) return;
		var expr= /.+spent (.+?) on this page and (.+?) in total./g;
		var match=expr.exec(timeTaken);
		timeInfo[page.name]=match[1];
		console.log("Metrics:: sending page message: page "+ page.name+" - "+timeInfo[page.name]);

		//window.localStorage.removeItem("data");
		
		//window.localStorage.removeItem(storageName);
		clearInterval(timerId);
		//saveState(survey);
    	
		// TODO: Remove this double posting, but find a way to make the multi-depth object easier to parse on the java side
		
		//survey.data["language"]=languageCode;
		
		Http.httpPost(env.server+"/api/surveys/"+surveyId+"/metrics/"+page.name+"/onComplete?visitorId="+Cookie.get("rhae-visitorId"), buildPageChangePayload(page, false), function(response){
			if (response.status==200){
				// navigate to a results page
				
			}else{
				// Handle the error scenario
			}
		});
    	//Http.httpPost(env.server+"/api/surveys/"+surveyId+"/metrics/"+page.name+"?event=onComplete&cookie="+Cookie.get("rhae-jwt")+"&time="+timeInfo[page.name]+"&country="+geoInfo["countryCode"]+"region"+geoInfo["region"]);
		
		var data=survey.data;
		data["language"]=languageCode;
    	
    	Http.httpPost(env.server+"/api/surveys/"+surveyId+"/metrics/onResults?visitorId="+Cookie.get("rhae-visitorId"), data, function(response){
			if (response.status==200){
				// navigate to a results page
				
				console.log("Completed posting results to server");
				
				window.location.assign("/results.html?surveyId="+surveyId+"&visitorId="+visitorId);
				
			}else{
				// Handle the error scenario
			}
    	});
    	
    	
    });

function buildPageChangePayload(page, includeData){
	var payload={};
	payload["visitorId"]=Cookie.get("rhae-visitorId");
	payload["timeOnpage"]=timeInfo[page.name];
	payload["geo"]=geoInfo["continentCode"];
	payload["countryCode"]=geoInfo["countryCode"];
	payload["region"]=geoInfo["region"];
	if (includeData){
		payload["data"]=survey.data;
	}
//	payload["info"]={};
//	payload["info"]["visitorId"]=Cookie.get("rhae-jwt");
//	payload["info"]["timeOnpage"]=timeInfo[page.name];
//	payload["info"]["geo"]=geoInfo["continentCode"];
//	payload["info"]["countryCode"]=geoInfo["countryCode"];
//	payload["info"]["region"]=geoInfo["region"];
//	payload["data"]=survey.data;
	return payload;
}

//if (undefined!=window.localStorage.getItem("data"))
//	survey.data=JSON.parse(window.localStorage.getItem("data"));

$("#surveyElement").Survey({
    model: survey
});



// MAT - ADDING TOP NAV
var navTopEl = document.querySelector("#surveyNavigation");
navTopEl.className = "navigationContainer";
//var leftImg = document.createElement("img");
//leftImg.src = "/Content/Images/examples/covid/Left.svg";
//leftImg.style = "width: 16px; height: 16px";
//leftImg.className = "navigationProgressbarImage";
//navTopEl.appendChild(leftImg);
var textDiv = document.createElement("p");
textDiv.className = "textProgress"
textDiv.innerText = "PROGRESS";
navTopEl.appendChild(textDiv);
var navProgBarDiv = document.createElement("div");
navProgBarDiv.className = "navigationProgressbarDiv";
navTopEl.appendChild(navProgBarDiv);
var navProgBar = document.createElement("ul");
navProgBar.className = "navigationProgressbar";
navProgBarDiv.appendChild(navProgBar);
//leftImg.onclick = function () {
//    navProgBarDiv.scrollLeft -= 70;
//};
//var liEls = [];
//for (var i = 0; i < survey.PageCount; i++) {
//    var liEl = document.createElement("li");
//    if (survey.currentPageNo == i) {
//        liEl
//            .classList
//            .add("current");
//    }
//    //liEl.onclick = function (index) {
//    //    return function () {
//    //        if (survey['isCompleted']) 
//    //            return;
//    //        liEls[survey.currentPageNo]
//    //            .classList
//    //            .remove("current");
//    //        if (index < survey.currentPageNo) {
//    //            survey.currentPageNo = index;
//    //        } else if (index > survey.currentPageNo) {
//    //            var j = survey.currentPageNo;
//    //            for (; j < index; j++) {
//    //                if (survey.visiblePages[j].hasErrors(true, true)) 
//    //                    break;
//    //                if (!liEls[j].classList.contains("completed")) {
//    //                    liEls[j]
//    //                        .classList
//    //                        .add("completed");
//    //                }
//    //            }
//    //            survey.currentPageNo = j;
//    //        }
//    //        liEls[survey.currentPageNo]
//    //            .classList
//    //            .add("current");
//    //    };
//    //}(i);
//    var pageTitle = document.createElement("span");
//    if (!survey.pages[i].navigationTitle) {
//        pageTitle.innerText = survey.pages[i].name;
//    } else 
//        pageTitle.innerText = survey.pages[i].navigationTitle;
//    pageTitle.className = "pageTitle";
//    liEl.appendChild(pageTitle);
//    var br = document.createElement("br");
//    liEl.appendChild(br);
//    var pageDescription = document.createElement("span");
//    if (!!survey.pages[i].navigationDescription) {
//        pageDescription.innerText = survey.pages[i].navigationDescription;
//    }
//    pageDescription.className = "pageDescription";
//    liEl.appendChild(pageDescription);
//    liEls.push(liEl);
//    navProgBar.appendChild(liEl);
//}
//survey
//    .onCurrentPageChanged
//    .add(function (sender, options) {
//        var oldIndex = options.oldCurrentPage.visibleIndex;
//        var newIndex = options.newCurrentPage.visibleIndex;
//        if (undefined!=liEls[oldIndex])
//	        liEls[oldIndex]
//	            .classList
//	            .remove("current");
//        if (newIndex > oldIndex) {
//            for (var i = oldIndex; i < newIndex; i++) {
//                if (sender.visiblePages[i].hasErrors(true, true)) 
//                    break;
//                if (!liEls[i].classList.contains("completed")) {
//                    liEls[i]
//                        .classList
//                        .add("completed");
//                }
//            }
//        }
//        if (undefined!=liEls[newIndex])
//	        liEls[newIndex]
//	            .classList
//	            .add("current");
//    });




var navTitlesUniqueSet=[];
//var navTitleToIndex={};
//var indexToNavTitle={};

var liEls = {};
for (var i = 0; i < survey.PageCount; i++) {
    var liEl = document.createElement("li");
   if (survey.currentPageNo == i) {
       liEl
           .classList
           .add("current");
   }

    var pageTitle = document.createElement("div");
    pageTitle.innerText=!survey.pages[i].navigationTitle?
    		pageTitle.innerText = survey.pages[i].name:
    		pageTitle.innerText = survey.pages[i].navigationTitle;
    
    
    
    // logic to group question pages in progress panel
    if (navTitlesUniqueSet.includes(pageTitle.innerText)) continue;
    navTitlesUniqueSet.push(pageTitle.innerText);
    
//    navTitleToIndex[pageTitle]=i;
    
    
    pageTitle.className = "pageTitle";
	navProgBar.appendChild(pageTitle);
	navProgBar.appendChild(liEl);
    // var br = document.createElement("br");
    // liEl.appendChild(br);
    // var pageDescription = document.createElement("span");
    // if (!!survey.pages[i].navigationDescription) {
    //     pageDescription.innerText = survey.pages[i].navigationDescription;
    // }
    // pageDescription.className = "pageDescription";
    // liEl.appendChild(pageDescription);
    liEls[undefined!=survey.pages[i].navigationTitle?survey.pages[i].navigationTitle:survey.pages[i].name]=liEl;

}
survey
    .onCurrentPageChanged
    .add(function (sender, options) {
    	var oldIndex = options.oldCurrentPage.navigationTitle;
        var newIndex = options.newCurrentPage.navigationTitle;
        var oldIndexI = options.oldCurrentPage.visibleIndex;
        var newIndexI = options.newCurrentPage.visibleIndex;
        
        // Only transition if the navigation Title is different, so it hangs around if we want to collate them
        var oldIndex = options.oldCurrentPage.navigationTitle!=undefined?options.oldCurrentPage.navigationTitle:options.oldCurrentPage.name;
        var newIndex = options.newCurrentPage.navigationTitle!=undefined?options.newCurrentPage.navigationTitle:options.newCurrentPage.name;
        if (oldIndex==newIndex) return;
        
        
        if (undefined!=liEls[oldIndex])
	        liEls[oldIndex].classList.remove("current");
        // change li color once transitioned beyond it
        if (newIndexI > oldIndexI) {
            for (var i = oldIndexI; i < newIndexI; i++) {
                if (sender.visiblePages[i].hasErrors(true, true)) 
                    break;
                if (!liEls[sender.visiblePages[i].navigationTitle!=null?sender.visiblePages[i].navigationTitle:sender.visiblePages[i].name].classList.contains("completed")) {
                    liEls[sender.visiblePages[i].navigationTitle!=null?sender.visiblePages[i].navigationTitle:sender.visiblePages[i].name].classList.add("completed");
                }
            }
        }
        // highlight current
        if (undefined!=liEls[newIndex])
	        liEls[newIndex].classList.add("current");
    });
    
/*
var rightImg = document.createElement("img");
rightImg.src = "https://img.icons8.com/material/4ac144/256/user-male.png";
rightImg.style = "width: 16px; height: 16px";
rightImg.className = "navigationProgressbarImage";
rightImg.onclick = function () {
    navProgBarDiv.scrollLeft += 70;
};
navTopEl.appendChild(rightImg);

var updateScroller = setInterval(() => {
    if (navProgBarDiv.scrollWidth <= navProgBarDiv.offsetWidth) {
        leftImg
            .classList
            .add("hidden");
        rightImg
            .classList
            .add("hidden");
    } else {
        leftImg
            .classList
            .remove("hidden");
        rightImg
            .classList
            .remove("hidden");
    }
}, 100);
 * */

//$(".sv_progress_bar").addClass("progress-bar");

// /MAT - ADDING TOP NAV



// State saving feature (+ timed saving)
var timerId=0;
var saveIntervalInSeconds=20;


//LocalStorage = {
//		storageName:"RHAssessmentPlatform_State",
//		saveState: function(survey) {
//			console.log("LocalStorage:: Saving state... (page "+survey.currentPageNo+")");
//		    window.localStorage.setItem(LocalStorage.storageName, JSON.stringify({ currentPageNo: survey.currentPageNo, data: survey.data }));
//		},
//		clearState: function(){
//			console.log("LocalStorage:: Clearing state")
//			window.localStorage.removeItem(LocalStorage.storageName);
//		},
//		loadState: function(survey) {
//			var storageSt = window.localStorage.getItem(LocalStorage.storageName) || "";
//			var loaded=storageSt?JSON.parse(storageSt):{ currentPageNo: 1, data: {} };
//			if (loaded.data) 
//			    survey.data=loaded.data;
//			if (loaded.currentPageNo){
//				//console.log("set page to "+loaded.currentPageNo);
//				survey.currentPageNo=loaded.currentPageNo;
//			}
//		}
//}
//if (undefined!=Utils.getParameterByName("dev_reset")){
//	console.log("Clearing localstorage of previously answered questions");
//	LocalStorage.clearState();
//}

//save data every x seconds
timerId = window.setInterval(function () {
    LocalStorage.saveState(survey);
}, saveIntervalInSeconds*1000);

LocalStorage.loadState(survey);
// /State saving feature




//survey.showPreviewBeforeComplete = 'showAnsweredQuestions';
//survey.showCompletedPage=false;
//survey.navigateToUrl="/results.html?surveyId="+surveyId+"&visitorId="+visitorId;

survey.completedHtml=" ";

//survey.locale = languageCode;
survey.render();



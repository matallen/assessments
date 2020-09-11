
// ######################### REPLACING IMAGEPICKER VISUALS #######################
var imgpicker_template = `
  <fieldset data-bind="css: question.koCss().root">
      <legend data-bind="attr: { 'aria-label': question.locTitle.renderedHtml }"></legend>
	<div class="sv_imgsel_grid">
      <!-- ko foreach: { data: question.visibleChoices, as: 'item', afterRender: question.koAfterRender}  -->
      <div data-bind="css: question.getItemClass(item)">
          <label data-bind="css: question.koCss().label">
              <input style="display: none;" data-bind="attr: {type: question.multiSelect ? 'checkbox' : 'radio', name: question.name + '_' + question.id, value: item.value, id: ($index() == 0) ? question.inputId : '', 'aria-required': question.isRequired, 'aria-label': question.locTitle.renderedHtml}, checked: question.koValue, enable: !question.isReadOnly && item.isEnabled, css: question.koCss().itemControl"
              />
              <div class="sv_imgpicker_outer">
	              <div class="sv_imgpicker_inner">
	                  <!-- ko if: question.contentMode === "image" -->
	                  <img data-bind="css: question.koCss().image, attr: { src: $data.imageLink, width: question.imageWidth ? question.imageWidth + 'px' : undefined, height: question.imageHeight ? question.imageHeight + 'px' : undefined }, style: { objectFit: question.imageFit }"/>
	                  <!-- /ko -->
	                  <!-- ko if: question.contentMode === "video" -->
	                  <embed data-bind="css: question.koCss().image, attr: { src: $data.imageLink, width: question.imageWidth ? question.imageWidth + 'px' : undefined, height: question.imageHeight ? question.imageHeight + 'px' : undefined }, style: { objectFit: question.imageFit }"/>
	                  <!-- /ko -->
	                  <!-- ko if: question.showLabel -->
	                  <div class="sv_q_imgsel_title hdr" data-bind="text: text || value, attr: { title: text || value }, css: question.koCss().itemText"></div>
	                  <!-- /ko -->
	                  <!-- ko if: description -->
	                  <div class="sv_q_imgsel_description" data-bind="text: description, attr: { title: description }, css: question.koCss().itemText"></div>
	                  <!-- /ko -->
	              </div>     
              </div>
          </label>
      </div>
      <!-- /ko -->
	</div>
  </fieldset>
`;
new Survey
	.SurveyTemplateText()
	.replaceText(imgpicker_template, "question", "imagepicker");
//Survey
//	.Serializer
//	.addProperty("imagepicker", "description:string");
Survey
	.Serializer
	.addProperty("imageitemvalue", "description"); // adds a description to each imagepicker item/option
// ###############################################################################


// ##### REPLACING RADIOGROUP (https://github.com/surveyjs/survey-library/blob/master/src/knockout/templates/question-radiogroup.html) #############
 var radiogroup_template = `
 <fieldset data-bind="css: question.koCss().root">
     <legend data-bind="attr: { 'aria-label': question.locTitle.renderedHtml }"></legend>
     <!-- ko ifnot: question.hasColumns  -->
       <!-- ko foreach: { data: question.visibleChoices, as: 'item', afterRender: question.koAfterRender }  -->
           <!-- ko template: 'survey-radiogroup-item' -->
           <!-- /ko -->
       <!-- /ko -->
     <!-- /ko -->
     <!-- ko if: question.hasColumns  -->
       <!-- ko foreach: question.columns -->
       <div data-bind="css: question.getColumnClass()">
           <!-- ko foreach: { data: $data, as: 'item', afterRender: question.koAfterRender }  -->
               <!-- ko template: 'survey-radiogroup-item' -->
               <!-- /ko -->
           <!-- /ko -->
           </div>
       <!-- /ko -->
     <!-- /ko -->
     <!-- ko if: question.canShowClearButton -->
     <div>
         <input type="button" data-bind="click:question.clearValue, css: question.koCss().clearButton, value: question.clearButtonCaption"/>
     </div>
     <!-- /ko -->
 </fieldset>
`;
new Survey
.SurveyTemplateText()
.replaceText(radiogroup_template, "question", "radiogroup");


//var radiogroupitem_template = `
//  <div data-bind="css: question.getItemClass(item)">
//      <label data-bind="css: question.getLabelClass(item)">
//          <input type="radio" data-bind="attr: { name: question.name + '_' + question.id, id: question.inputId + '_' + question.getItemIndex(item), 'aria-required': question.isRequired, 'aria-label': item.locText.renderedHtml, role: 'radio', 'aria-invalid': question.errors.length > 0, 'aria-describedby': question.errors.length > 0 ? question.id + '_errors' : null}, checkedValue: item.value, checked: question.renderedValue, enable: !question.isReadOnly && item.isEnabled, css: question.koCss().itemControl"
//          />
//          <span data-bind="css: question.koCss().materialDecorator">
//            <svg data-bind="css:question.koCss().itemDecorator" viewBox="-12 -12 24 24">
//                <circle r="6" cx="0" cy="0">
//            </svg>
//          </span>
//          <span class="check"></span>
//          <span data-bind="visible: !item.hideCaption, css: question.getControlLabelClass(item), attr: { title: item.locText.koRenderedHtml }">
//              <!-- ko template: { name: 'survey-string', data: item.locText } -->
//              <!-- /ko -->
//          </span>
//      </label>
//      <!-- ko if: question.hasOther && (item.value == question.otherItem.value) -->
//      <div class="form-group" data-bind="template: { name: 'survey-comment', data: {'question': question, 'visible': question.isOtherSelected}}"></div>
//      <!-- /ko -->
//  </div>
//`;
//new Survey
//.SurveyTemplateText()
//.replaceText(radiogroup_template, "question", "radiogroup");
//new Survey
//.SurveyTemplateText()
//.replaceText(radiogroupitem_template, "radiogroup", "item");
// ##### END REPLACING RADIOGROUP ###############




var config=SURVEY_CONFIG;

// Page theme
if (undefined!=config.theme && ""!=config.theme)
	loadCSS("assets/themes/"+config.theme+"/css/style.css");

console.log("Config theme" +config.theme);
var defaultThemeColors = Survey
    .StylesManager
    .ThemeColors["default"];

defaultThemeColors["$main-color"] = "#ee0000";//"#a30000";
//defaultThemeColors["$main-hover-color"] = "#820000";
defaultThemeColors["$main-hover-color"] = "#D40000";

defaultThemeColors["$text-color"] = "#4a4a4a";
defaultThemeColors["$header-color"] = "#ffffff";
defaultThemeColors["$header-background-color"] = "#cc0000";
defaultThemeColors["$body-container-background-color"] = "#f8f8f8";
defaultThemeColors["$error-color"]="#ee0000";//"#a30000";
//defaultThemeColors["$border-color"]="#cc0000";

Survey
    .StylesManager
    .applyTheme("default");

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

Survey.defaultStandardCss.navigation.complete = "sv_rh_btn sv_rh_complete_btn rhbtn rhbtn-red";
Survey.defaultStandardCss.navigation.prev =     "sv_rh_btn sv_rh_prev_btn rhbtn rhbtn-secondary";
Survey.defaultStandardCss.navigation.next =     "sv_rh_btn sv_rh_next_btn rhbtn rhbtn-primary";

Survey.settings.webserviceEncodeParameters = true;

Survey.ChoicesRestfull.onBeforeSendRequest = function(sender, options) {
        //options.request.setRequestHeader("Content-Type", "application/json");
        //options.request.setRequestHeader("Authorization", "Bearer "+jwtToken);
        //sender.processedUrl=sender.processedUrl.replace("localhost","testing");
        
        // Do a replace on variables in the URL prior to executing
        options.request.open("GET", sender.processedUrl
        		.replace("$surveyId",surveyId)
        		.replace("$server", env.server)
        	);
        
};

var json = SURVEY_CONTENT;

window.survey = new Survey.Model(json);


var timeInfo=[];


var geoInfo=undefined;
if (undefined==geoInfo){
  $.ajax({
//    url: "http://ip-api.com/json?fields=continentCode,country,countryCode,region",
	url: env.server+"/api/geoInfo?url=http://ip-api.com/json&fields=continentCode,country,countryCode,region",
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
survey.completeText = 'View my results';

survey.onLoadChoicesFromServer.add(function(survey, options) {
    
	// CONSENT AGREEMENT LOGIC ONLY
	if (options.question.name=="_consentAgreement"){
		//console.log("options.length="+options.length);
		var payload=options.serverResult;
		var defaultValue=[];
		for(var x in payload){
			//console.log(payload[x]);
			if (payload[x].name!=undefined && payload[x].checked!=undefined && payload[x].checked)
				defaultValue.push(payload[x].name);
		}
		if (defaultValue.length>0)
			options.question.defaultValue=defaultValue;
	}
	// END OF CONSENT AGREEMENT LOGIC
	
  });

survey
      .onAfterRenderPage
      .add(function(result, options){
			// Change button text on specific pages (Start on page 1 &
			$(".sv_rh_next_btn").prop("value", "Next");
			
			// Change button text on page 1 to "Start"
//			if (survey.currentPageNo==0){
//				$(".sv_rh_next_btn").prop("value", "Start");
//			}
			// Change button text on last page to ???
//			if (survey.currentPageNo==survey.pages.length){
//				$(".sv_rh_next_btn").prop("value", "Start");
//			}
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
		
		Http.httpPost(env.server+"/api/surveys/"+surveyId+"/metrics/"+page.name+"/onPageChange?visitorId="+Cookie.get("rhae-visitorId"), buildPayload(page));
		
		
	});

survey
	.onUpdatePageCssClasses
	.add(function(sender, options){
		// set the css class to the page name allowing us to style some pages different from others
		options.cssClasses.page.root="sv_p_root sv_page_"+options.page.name.replace(/ /g,"_").toLowerCase();
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
		
		// Hide the survey Navigation pane
		$("#surveyNavigation").hide();

		//window.localStorage.removeItem("data");
		
		//window.localStorage.removeItem(storageName);
		//clearInterval(timerId);
		//saveState(survey);
    	
		survey.data["language"]=languageCode;
    	
    	Http.httpPost(env.server+"/api/surveys/"+surveyId+"/generateReport?pageId="+page.name+"&visitorId="+Cookie.get("rhae-visitorId"), buildPayload(page, true), function(response){
			if (response.status==200){
				// navigate to a results page
				
				console.log("Completed posting results to server");
				
				var resultId=response.responseText;
				window.location.assign("/results.html?surveyId="+surveyId+"&resultId="+resultId);
				
			}else{
				// Handle the error scenario
			}
    	});
    	
    });

function buildPayload(page, includeData){
	var payload={};
	payload["_page"]={};
	payload["_page"]["visitorId"]=Cookie.get("rhae-visitorId");
	payload["_page"]["timeOnpage"]=timeInfo[page.name];
	if (undefined!=geoInfo){
		payload["_page"]["geo"]=geoInfo["continentCode"];
		payload["_page"]["countryCode"]=geoInfo["countryCode"];
		payload["_page"]["region"]=geoInfo["region"];
	}else{
		console.log("Error: Unable to obtain geo information");
	}
	if (includeData){
		payload["_data"]=survey.data;
	}
	return payload;
}


//if (undefined!=window.localStorage.getItem("data"))
//	survey.data=JSON.parse(window.localStorage.getItem("data"));

$("#surveyElement").Survey({
    model: survey
});


// Top Nav
var navTopEl = document.querySelector("#surveyNavigation");
navTopEl.className = "navigationContainer";
var textDiv = document.createElement("p");
textDiv.className = "textProgress"
textDiv.innerText = "Progress";
navTopEl.appendChild(textDiv);
var navProgBarDiv = document.createElement("div");
navProgBarDiv.className = "navigationProgressbarDiv";
navTopEl.appendChild(navProgBarDiv);
var navProgBar = document.createElement("ul");
navProgBar.className = "navigationProgressbar";
navProgBarDiv.appendChild(navProgBar);


var navTitlesUniqueSet=[];
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
    
    pageTitle.className = "pageTitle";
    
    if (survey.pages[i].isVisible!=true)
    	pageTitle.classList.toggle("pageNotVisible");
    
	navProgBar.appendChild(pageTitle);
	navProgBar.appendChild(liEl);
	
	pageTitle.classList.add("_"+survey.pages[i].name.replace(/ /g,"_").toLowerCase());
    liEls[undefined!=survey.pages[i].navigationTitle?survey.pages[i].navigationTitle:survey.pages[i].name]=liEl;

}

survey
	.onValueChanged
	.add(function (sender, options) {
		//console.log("Answer changed - saving state");
		LocalStorage.saveState(survey);
});

survey
	.onPageVisibleChanged
	.add(function (sender, options) {
		console.log("onPageVisibleChanged");
		var pageName=options.page.name;
		var visible=options.visible;
		var navTitle=options.page.navigationTitle?options.page.navigationTitle:options.page.name;
		
		var pageTitle=$("._"+pageName.replace(/ /g,"_").toLowerCase()).get()[0];
		if (undefined==pageTitle) return;
		if (visible){
			liEls[navTitle].classList.remove("pageNotVisible");
			pageTitle.classList.remove("pageNotVisible");
		}else{
			liEls[navTitle].classList.add("pageNotVisible");
			pageTitle.classList.add("pageNotVisible");
		}
		
	});
survey
    .onCurrentPageChanged
    .add(function (sender, options) {
    	// This is a horrible hack, but I need to change a class on a parent html element allowing us to style pages separately, so this adds the page name as a classname to an ancestor div
    	var classList=document.getElementById('survey-wrapper').className;//.split(/\s+/);
    	if (undefined==document.getElementById('survey-wrapper').dataset["classList"])
    		document.getElementById('survey-wrapper').dataset["classList"]=classList;
    	document.getElementById('survey-wrapper').className=document.getElementById('survey-wrapper').dataset["classList"]+" sv_wrapper_"+options.newCurrentPage.name.replace(/ /g,"_").toLowerCase();
    	
    	
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


// /MAT - ADDING TOP NAV



// State saving feature (+ timed saving)
//var timerId=0;
var saveIntervalInSeconds=20;


//save data every x seconds
//timerId = window.setInterval(function () {
//    LocalStorage.saveState(survey);
//}, saveIntervalInSeconds*1000);

if (LocalStorage.getFlag("lastAssessmentCompleted")=="true"){ // This means the report page has been displayed so we can remove the prior answers
	LocalStorage.clearState(); // remove answers from localstorage. We cant do this on the results page just in case they want to retake the assessment
	LocalStorage.removeFlag("lastAssessmentCompleted");
}
LocalStorage.loadState(survey);
// /State saving feature



//survey.showPreviewBeforeComplete = 'showAnsweredQuestions';
//survey.showCompletedPage=false;
//survey.navigateToUrl="/results.html?surveyId="+surveyId+"&visitorId="+visitorId;


survey.completedHtml="<h2>Analysing responses and generating <br/>your report - please wait a moment</h2>";

//survey.locale = languageCode;
survey.render();


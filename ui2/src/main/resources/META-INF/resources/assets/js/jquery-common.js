//var server="http://localhost:8080";

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

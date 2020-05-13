//var server="http://localhost:8080";

function setMenu(setCurrent){
	$(".menu_needsId").each(function(index){
		var a=$(this).find("a");
		var name=a.text();
		if (setCurrent==a.text()){
			$(this).addClass("current");
			a.attr("href", "#");
		}else{
			
			var currentId=Utils.getParameterByName("href", a.attr("href"));
			if (null==currentId && null!=surveyId)
				a.attr("href", a.attr("href")+surveyId);
			//a.text(a.text()+surveyId);
			//$(this).removeClass("menu_needsId");
		}
	});
	replaceText({"id":surveyId});
}
function replaceText(payload){
	for (key in payload){
		$("._"+key).text(payload[key]);
	}
}

// This is the logic behind alert messages when content is updated or saved


function showSuccess(feedbackText, timeVisible){
	if (undefined==timeVisible) timeVisible=3000;
	$("#feedback-alert-text").text(feedbackText);
	$("#feedback-alert").fadeTo(timeVisible, 500).slideUp(500, function(){
	  $("#feedback-alert").slideUp(500);
	}); 
};
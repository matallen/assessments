// This is the logic behind alert messages when content is updated or saved

function showMessage(status, successText, errorText){
	return status?showSuccess(successText):showFailure("Error ("+status+"): "+errorText);
}

function showSuccess(feedbackText, timeVisible){
	if (undefined==timeVisible) timeVisible=3000;
	$("#feedback-alert-text").text(feedbackText);
	
	$("#feedback-alert").removeClass();
	$("#feedback-alert").addClass("alert alert-success");
	$("#feedback-alert").show();
	
	$("#feedback-alert").fadeTo(timeVisible, 500).slideUp(500, function(){
	  $("#feedback-alert").slideUp(500);
	}); 
};
function showFailure(feedbackText, timeVisible){
	if (undefined==timeVisible) timeVisible=3000;
	$("#feedback-alert-text").text(feedbackText);
	
	$("#feedback-alert").removeClass();
	$("#feedback-alert").addClass("alert alert-danger");
	$("#feedback-alert").show();

	$("#feedback-alert").fadeTo(timeVisible, 500).slideUp(500, function(){
	  $("#feedback-alert").slideUp(500);
	}); 
};


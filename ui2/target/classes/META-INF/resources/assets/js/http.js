Http = {
	send: function(action, uri, data, callback){
		var xhr = new XMLHttpRequest();
		xhr.open(action, uri, true);
		xhr.setRequestHeader('Authorization','Bearer '+ Http.getCookie("rhae-jwt"));
		if (data != undefined){
			xhr.setRequestHeader("Content-type", "application/json");
			xhr.send(JSON.stringify(data));
		}else
			xhr.send();
		xhr.onloadend = function () {
			console.log("http::send:: onloadend ... status = "+this.status);
			if (this.status == 200){
				console.log("http::send:: returned 200");
			}else if(xhr.status>=400){
			}
			
			if (undefined!=callback){
				callback(xhr, this.status);
			}
		};
	},
	httpPost: function(uri, data){
		return Http.send("POST", uri, data);
	},
	httpPost: function(uri, data, callback){
		return Http.send("POST", uri, data, callback);
	},
	httpDelete: function(uri, data){
		return Http.send("DELETE", uri, data);
	},
	httpDelete: function(uri, data, callback){
		return Http.send("DELETE", uri, data, callback);
	},
	httpGet: function(url, callback){
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, true);
		var jwt=Http.getCookie("rhae-jwt"); // debug
		//console.log("sending token: "+jwt); // debug
		xhr.setRequestHeader('Authorization','Bearer '+ Http.getCookie("rhae-jwt"));
		xhr.send();
		xhr.onloadend = function () {
			if (401==this.status)
				document.location.replace("/login.html?error=2");
			
			callback(this.status, xhr.responseText);
		};
	},
	httpGetObject: function(url, callback, onError){
		Http.httpGet(url, function(status, responseText){
			if (status==200){
				callback(status, responseText!="" && responseText!=undefined?JSON.parse(responseText):responseText);
			}else
				onError(status);
		});
		
		//var xhr = new XMLHttpRequest();
		//xhr.open("GET", url, true);
		//xhr.setRequestHeader('Authorization','Bearer '+ Http.getCookie("rhae-jwt"));
		//xhr.send();
		//xhr.onloadend = function () {
		//	if (this.status==200){
		//		callback(this.status, JSON.parse(xhr.responseText));
		//	}else
		//		onError(this.status);
		//};
	},
	getCookie: function(cname){
	  var name = cname + "=";
	  var decodedCookie = decodeURIComponent(document.cookie);
	  var ca = decodedCookie.split(';');
	  for(var i = 0; i <ca.length; i++) {
	    var c = ca[i];
	    while (c.charAt(0) == ' ') {
	      c = c.substring(1);
	    }
	    if (c.indexOf(name) == 0) {
	      return c.substring(name.length, c.length);
	    }
	  }
	  return "";
	}
}


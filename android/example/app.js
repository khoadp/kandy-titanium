// This is a test harness for your module
// You should do something interesting in this harness
// to test out the module and to provide instructions
// to users on how to use it by example.

var kandy = require('com.kandy');

kandy.setup({
	api_key: "DAK87b4d788686e4456b06de8d655ab16a7",
	api_secret: "DASb29ae2979e9140538b7bda90614be909"
});

/**
 * ACCESS
 */

kandy.createAccess().login({
	username: "user@domain.com",
	password: "password",
	success: function(s){
		alert("Login success!");
	},
	error: function(e){
		alert(e.message);
	}
});

kandy.createAccess().logout({
	success: function(s){
		alert("Logout success!");	
	},
	error: function(e){
		alert(e.message);
	}
});

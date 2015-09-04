function CallWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('call'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
		contentHeight : 'auto',
		scrollType : 'vertical'
	});
	self.add(container);

	// var button = Ti.UI.createButton({
	// height : 44,
	// width : 200,
	// title : 'call Demo',
	// top : 20
	// });
	// container.add(button);
	//
	// var localView = Kandy.createCallView({
	// width : 200,
	// height : 200,
	// top : 80,
	// left : 0
	// });
	// container.add(localView);
	// var remoteView = Kandy.createCallView({
	// width : 200,
	// height : 200,
	// top : 80,
	// left : 201
	// });
	// container.add(remoteView);

	// var callWidget = Kandy.createCallWidget({
	// callee: "demo@demo.com",
	// callText: "Call demo",
	// callType: "video",
	// callTypes: ['video', 'pstn', 'sip']
	// });

	// var call = Kandy.createCallService();
	// var callee = "user3@jupiter.kodeplus.net";
	// button.addEventListener('click', function(e) {
	// call.createVoipCall({
	// callee : callee,
	// startWithVideo : true,
	// });
	//
	// localView.setLocalVideoView(callee);
	// remoteView.setRemoteVideoView(callee);
	// });
	//

	var callWidget = Kandy.createCallWidget({
		top : 5,
		width : 200,
		height : 50,
		callee : "demo@demo.com",
		callText : "call demo",
		callType : "video"
	});

	callWidget.addEventListener('onIncomingCall', function(e) {
		Ti.API.info(e.callee.uri + ' is calling...');
	});

	self.setActivityForListener = function(a) {
		a.onPause = function() {
			callWidget.unregisterNotificationListener();
		};
		a.onResume = function() {
			callWidget.registerNotificationListener();
		};
	};

	container.add(callWidget);

	return self;
};

module.exports = CallWindow;

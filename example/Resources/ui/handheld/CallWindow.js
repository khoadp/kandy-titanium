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

	var button = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : 'call Demo',
		top : 20
	});
	container.add(button);

	var localView = Kandy.createCallView({
		width : 200,
		height : 200,
		top : 80,
		left : 0
	});
	container.add(localView);
	var remoteView = Kandy.createCallView({
		width : 200,
		height : 200,
		top : 80,
		left : 201
	});
	container.add(remoteView);

	var call = Kandy.createCallService();
	var callee = "demo@demo.com";
	button.addEventListener('click', function(e) {
		call.createVoipCall({
			callee : callee,
			startWithVideo : true,
		});

		localView.setLocalVideoView(callee);
		remoteView.setRemoteVideoView(callee);
	});

	self.setActivityForListener = function(a) {
		a.onPause = function() {
			call.unregisterNotificationListener();
		};
		a.onResume = function() {
			call.registerNotificationListener();
		};
	};

	return self;
};

module.exports = CallWindow;

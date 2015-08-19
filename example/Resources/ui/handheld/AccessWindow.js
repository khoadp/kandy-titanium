function AccessWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('access'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	var access = Kandy.createAccessService({
		width : 350,
		height : 500,
		callbacks : {
			onConnectionStateChanged : function(state) {
				if (state == Kandy.CONNECTION_STATE_CONNECTED) {
					var LoggedInApplicationTabGroup = require('ui/common/LoggedInApplicationTabGroup');
					new LoggedInApplicationTabGroup(Kandy).open();	
				}
			}
		}
	});

	self.setActivityForListener = function(a) {
		a.onPause = function() {
			access.unregisterNotificationListener();
		};
		a.onResume = function() {
			access.registerNotificationListener();
		};
	};
	container.add(access);

	return self;
};

module.exports = AccessWindow;

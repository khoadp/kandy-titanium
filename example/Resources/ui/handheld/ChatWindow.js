function ChatWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('chat'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);
	
	var chat = Kandy.createChatService({
		width : 350,
		height : 500,
		callbacks: {
			onChatReceived: function(message){
			Ti.API.info(message.type);
		}
		}
	});
	
	self.setActivityForListener = function(a) {
		a.onPause = function() {
			chat.unregisterNotificationListener();
		};
		a.onResume = function() {
			chat.registerNotificationListener();
		};
	};
	
	container.add(chat);

	return self;
};

module.exports = ChatWindow;

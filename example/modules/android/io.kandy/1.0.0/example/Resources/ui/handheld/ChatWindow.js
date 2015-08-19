function ChatWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('chat'),
		backgroundColor : 'white'
	});

	
	var chat = Kandy.createChatService({
		width : 350,
		height : 500,
		callbacks: {
			
		}
	});
	self.add(chat);

	return self;
};

module.exports = ChatWindow;

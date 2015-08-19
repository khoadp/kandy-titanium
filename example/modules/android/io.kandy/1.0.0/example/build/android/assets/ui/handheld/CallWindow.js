function CallWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('call'),
		backgroundColor : 'white'
	});

	var call = Kandy.createCallService({
		width : 350,
		height : 500,
		callbacks: {
			
		}
	});
	self.add(call);

	return self;
};

module.exports = CallWindow;

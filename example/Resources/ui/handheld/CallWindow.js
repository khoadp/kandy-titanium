function CallWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('call'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	var call = Kandy.createCallService({
		width : 350,
		height : 500,
		callbacks: {
			
		}
	});
	container.add(call);

	return self;
};

module.exports = CallWindow;

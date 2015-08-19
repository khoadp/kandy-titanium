function SettingsWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('settings'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	return self;
};

module.exports = SettingsWindow;

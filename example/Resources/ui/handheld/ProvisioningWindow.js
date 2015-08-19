function ProvisioningWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title: L('provisioning'),
		backgroundColor:'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	var provisioning = Kandy.createProvisioningService({
		width: 350,
		height: 500
	});
	container.add(provisioning);
	
	return self;
};

module.exports = ProvisioningWindow;

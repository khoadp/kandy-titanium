function ProvisioningWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title: L('provisioning'),
		backgroundColor:'white'
	});

	var provisioning = Kandy.createProvisioningService({
		width: 350,
		height: 500
	});

	self.add(provisioning);
	
	return self;
};

module.exports = ProvisioningWindow;

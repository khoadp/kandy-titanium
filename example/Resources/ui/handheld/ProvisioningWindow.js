function ProvisioningWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('provisioning'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
		contentHeight : 'auto',
		scrollType : 'vertical'
	});
	self.add(container);

	var request = Kandy.createProvisioningService({
		type : "request",
		top: 0,
		width : 350,
		height : 100
	});
	container.add(request);

	var validate = Kandy.createProvisioningService({
		type : "validate",
		top : 105,
		width : 350,
		height : 100
	});
	container.add(validate);

	var deactivate = Kandy.createProvisioningService({
		type : "deactivate",
		top : 210,
		width : 350,
		height : 100
	});
	container.add(deactivate);

	return self;
};

module.exports = ProvisioningWindow;

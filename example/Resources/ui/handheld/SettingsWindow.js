function SettingsWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('settings'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
		contentHeight : 'auto',
		scrollType : 'vertical'
	});
	self.add(container);

	var apiLabel = Ti.UI.createLabel({
		color : 'black',
		text : L('apiKeyLabel'),
		top : 5,
		left : 5,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	container.add(apiLabel);

	var apiEdit = Ti.UI.createTextField({
		borderStyle : Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
		color : '#336699',
		top : 5,
		left : 50,
		width : 300,
		height : 60
	});
	container.add(apiEdit);

	var secretLabel = Ti.UI.createLabel({
		color : 'black',
		text : L('secretKeyLabel'),
		top : 70,
		left : 5,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	container.add(secretLabel);

	var secretEdit = Ti.UI.createTextField({
		borderStyle : Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
		color : '#336699',
		top : 70,
		left : 50,
		width : 300,
		height : 60
	});
	container.add(secretEdit);

	var hostLabel = Ti.UI.createLabel({
		color : 'black',
		text : L('hostLabel'),
		top : 135,
		left : 5,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	container.add(hostLabel);

	var hostEdit = Ti.UI.createTextField({
		borderStyle : Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
		color : '#336699',
		top : 135,
		left : 50,
		width : 300,
		height : 60
	});
	container.add(hostEdit);

	var updateKeys = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('update'),
		top : 200
	});
	container.add(updateKeys);

	var reportsLabel = Ti.UI.createLabel({
		color : 'black',
		text : '',
		top : 300,
		left : 5,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	container.add(reportsLabel);

	reportsLabel.setText(Kandy.getReport());
	hostEdit.setValue(Kandy.getHostUrl());

	var s = Kandy.getSession();
	apiEdit.setValue(s.domain.apiKey);
	secretEdit.setValue(s.domain.apiSecret);

	updateKeys.addEventListener('click', function() {
		var api = apiEdit.value,
		    secret = secretEdit.value;
		if (api != '' && secret != '')
			Kandy.setKey(api, secret);
		else
			alert('Keys must not be empty');
		var url = hostEdit.value;
		if (url != '')
			Kandy.setHostUrl(url);
		else
			alert('Host url must not be empty');
	});

	return self;
};

module.exports = SettingsWindow;

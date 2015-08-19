function AddressBookWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('addressBook'),
		backgroundColor : 'white'
	});

	var btn1 = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('getDeviceContacts'),
		top : 20
	});
	self.add(btn1);

	var btn2 = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('getDomainDirectoryContacts'),
		top : 65
	});
	self.add(btn2);

	var txt = Ti.UI.createLabel({
		color : 'black',
		text : '',
		top : 150,
		left : 50,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	self.add(txt);

	var ab = Kandy.createAddressBookService();
	var eFn = function(e) {
		var toast = Ti.UI.createNotification({
			message : e,
			duration : Ti.UI.NOTIFICATION_DURATION_SHORT
		});
		toast.show();
	};

	btn1.addEventListener('click', function() {
		txt.setText('loading...');
		ab.getDeviceContacts({
			success : function(info) {
				txt.setText(JSON.stringify(info, null, 2));
			},
			error : eFn
		});
	});

	btn2.addEventListener('click', function() {
		txt.setText('loading...');
		ab.getDomainDirectoryContacts({
			success : function(info) {
				txt.setText(JSON.stringify(info, null, 2));
			},
			error : eFn
		});
	});

	return self;
};

module.exports = AddressBookWindow;

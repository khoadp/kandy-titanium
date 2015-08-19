function LocationWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('location'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	var btn1 = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('getCountryInfo'),
		top : 20
	});
	container.add(btn1);

	var btn2 = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('getCurrentLocation'),
		top : 65
	});
	container.add(btn2);

	var txt = Ti.UI.createLabel({
		color : 'black',
		text : '',
		top : 150,
		left : 50,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	container.add(txt);

	var location = Kandy.createLocationService();
	var eFn = function(e) {
		var toast = Ti.UI.createNotification({
			message : e,
			duration : Ti.UI.NOTIFICATION_DURATION_SHORT
		});
		toast.show();
	};

	btn1.addEventListener('click', function() {
		txt.setText('loading...');
		location.getCountryInfo({
			success : function(info) {
				txt.setText(JSON.stringify(info, null, 2));
			},
			error : eFn
		});
	});

	btn2.addEventListener('click', function() {
		txt.setText('loading...');
		location.getCurrentLocation({
			success : function(info) {
				txt.setText(JSON.stringify(info, null, 2));
			},
			error : eFn
		});
	});

	return self;
};

module.exports = LocationWindow;

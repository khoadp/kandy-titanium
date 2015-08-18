function PushWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('push'),
		backgroundColor : 'white'
	});

	var txt = Ti.UI.createLabel({
		color : 'black',
		text : L('pushServiceState'),
		top : 20,
		textAlign : Ti.UI.TEXT_ALIGNMENT_CENTER,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});

	self.add(txt);

	var btn1 = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('enablePush'),
		top : 45
	});
	self.add(btn1);

	var btn2 = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('disablePush'),
		top : 90
	});
	self.add(btn2);

	var push = Kandy.createPushService();
	var eFn = function(e) {
		var toast = Ti.UI.createNotification({
			message : e,
			duration : Ti.UI.NOTIFICATION_DURATION_SHORT
		});
		toast.show();
	};

	btn1.addEventListener('click', function() {
		push.enablePushNotification({
			success : function() {
				txt.setText(L('pushServiceEnabled'));
			},
			error : eFn
		});
	});

	btn2.addEventListener('click', function() {
		push.disablePushNotification({
			success : function() {
				txt.setText(L('pushServiceDisabled'));
			},
			error : eFn
		});
	});

	return self;
};

module.exports = PushWindow;

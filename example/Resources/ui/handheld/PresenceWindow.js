function PresenceWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('presence'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	var txtEdit = Ti.UI.createTextField({
		borderStyle : Ti.UI.INPUT_BORDERSTYLE_ROUNDED,
		color : '#336699',
		hintText : L('userListHint'),
		top : 20,
		width : 300,
		height : 60
	});
	container.add(txtEdit);

	var btn = Ti.UI.createButton({
		height : 44,
		width : 200,
		title : L('watch'),
		top : 90
	});
	container.add(btn);

	var txtPresences = Ti.UI.createLabel({
		color : 'black',
		text : '',
		top : 150,
		left : 50,
		textAlign : Ti.UI.TEXT_ALIGNMENT_LEFT,
		width : Ti.UI.SIZE,
		height : Ti.UI.SIZE
	});
	container.add(txtPresences);

	var presence = Kandy.createPresenceService();
	var eFn = function(e) {
		var toast = Ti.UI.createNotification({
			message : e,
			duration : Ti.UI.NOTIFICATION_DURATION_SHORT
		});
		toast.show();
	};

	btn.addEventListener('click', function() {
		txtPresences.setText('loading...');
		var list = txtEdit.value;
		if (list != '') {
			presence.startWatch({
				list : list.split(','),
				success : function(info) {
					txtPresences.setText(JSON.stringify(info, null, 2));
				},
				error : eFn
			});
		}
	});

	return self;
};

module.exports = PresenceWindow;

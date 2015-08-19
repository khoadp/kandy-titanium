function GroupWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('group'),
		backgroundColor : 'white'
	});

	var container = Titanium.UI.createScrollView({
    	contentHeight:'auto',
    	scrollType: 'vertical'
	});
	self.add(container);

	var group = Kandy.createGroupService({
		width : 350,
		height : 500,
		callbacks: {
			
		}
	});
	container.add(group);

	return self;
};

module.exports = GroupWindow;

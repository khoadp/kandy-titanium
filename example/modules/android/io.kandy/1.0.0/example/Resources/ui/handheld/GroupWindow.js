function GroupWindow(Kandy) {
	var self = Ti.UI.createWindow({
		title : L('group'),
		backgroundColor : 'white'
	});

	var group = Kandy.createGroupService({
		width : 350,
		height : 500,
		callbacks: {
			
		}
	});
	self.add(group);

	return self;
};

module.exports = GroupWindow;

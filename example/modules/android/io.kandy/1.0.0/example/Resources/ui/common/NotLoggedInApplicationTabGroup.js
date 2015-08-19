function NotLoggedInApplicationTabGroup(Kandy) {
	//create module instance
	var self = Ti.UI.createTabGroup();

	var AccessWindow = require('ui/handheld/AccessWindow'),
	    ProvisioningWindow = require('ui/handheld/ProvisioningWindow');

	//create app tabs
	var win1 = new AccessWindow(Kandy),
	    win2 = new ProvisioningWindow(Kandy);

	var tab1 = Ti.UI.createTab({
		title : L('access'),
		icon : '/images/KS_nav_ui.png',
		window : win1
	});
	win1.containingTab = tab1;

	var tab2 = Ti.UI.createTab({
		title : L('provisioning'),
		icon : '/images/KS_nav_views.png',
		window : win2
	});
	win2.containingTab = tab2;

	self.addTab(tab1);
	self.addTab(tab2);
	
	var activity = self.activity;
	win1.setActivityForListener(activity);
	
	activity.onCreateOptionsMenu = function(e) {
		var menu = e.menu;
		var menuItem = menu.add({
			title : L('settings')
		});
		menuItem.addEventListener('click', function(e) {
			//webView.goBack();
		});
	};

	return self;
};

module.exports = NotLoggedInApplicationTabGroup;

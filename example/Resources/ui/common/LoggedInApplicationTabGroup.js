function LoggedInApplicationTabGroup(Kandy) {
	//create module instance
	var self = Ti.UI.createTabGroup();

	var SettingsWindow = require('ui/handheld/SettingsWindow'),
	    CallWindow = require('ui/handheld/CallWindow'),
	    ChatWindow = require('ui/handheld/ChatWindow'),
	    GroupWindow = require('ui/handheld/GroupWindow'),
	    PresenceWindow = require('ui/handheld/PresenceWindow'),
	    LocationWindow = require('ui/handheld/LocationWindow'),
	    PushWindow = require('ui/handheld/PushWindow'),
	    AddressBookWindow = require('ui/handheld/AddressBookWindow');

	//create app tabs

	var win0 = new SettingsWindow(Kandy),
	    win1 = new CallWindow(Kandy),
	    win2 = new ChatWindow(Kandy),
	    win3 = new GroupWindow(Kandy),
	    win4 = new PresenceWindow(Kandy),
	    win5 = new LocationWindow(Kandy),
	    win6 = new PushWindow(Kandy),
	    win7 = new AddressBookWindow(Kandy);

	var tab1 = Ti.UI.createTab({
		title : L('call'),
		icon : '/images/KS_nav_ui.png',
		window : win1
	});
	win1.containingTab = tab1;

	var tab2 = Ti.UI.createTab({
		title : L('chat'),
		icon : '/images/KS_nav_views.png',
		window : win2
	});
	win2.containingTab = tab2;

	var tab3 = Ti.UI.createTab({
		title : L('group'),
		icon : '/images/KS_nav_ui.png',
		window : win3
	});
	win3.containingTab = tab3;

	var tab4 = Ti.UI.createTab({
		title : L('presence'),
		icon : '/images/KS_nav_views.png',
		window : win4
	});
	win4.containingTab = tab4;

	var tab5 = Ti.UI.createTab({
		title : L('location'),
		icon : '/images/KS_nav_ui.png',
		window : win5
	});
	win5.containingTab = tab5;

	var tab6 = Ti.UI.createTab({
		title : L('push'),
		icon : '/images/KS_nav_views.png',
		window : win6
	});
	win6.containingTab = tab6;

	var tab7 = Ti.UI.createTab({
		title : L('addressBook'),
		icon : '/images/KS_nav_ui.png',
		window : win7
	});
	win7.containingTab = tab7;

	self.addTab(tab1);
	self.addTab(tab2);
	self.addTab(tab3);
	self.addTab(tab4);
	self.addTab(tab5);
	self.addTab(tab6);
	self.addTab(tab7);

	var activity = self.activity;
	
	win2.setActivityForListener(activity);
	win1.setActivityForListener(activity);

	activity.onCreateOptionsMenu = function(e) {
		var menu = e.menu;
		var menuItem = menu.add({
			title : L('settings')
		});
		menuItem.addEventListener('click', function(e) {
			// open settings window
			win0.open();
		});
	};

	return self;
};

module.exports = LoggedInApplicationTabGroup;

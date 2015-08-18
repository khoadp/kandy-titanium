function ApplicationTabGroup(Window) {
	//create module instance
	var self = Ti.UI.createTabGroup();

	//create app tabs
	var win1 = new Window(L('login')),
		win2 = new Window(L('provisioning'));

	var tab1 = Ti.UI.createTab({
		title: L('home'),
		icon: '/images/KS_nav_ui.png',
		window: win1
	});
	win1.containingTab = tab1;

	var tab2 = Ti.UI.createTab({
		title: L('settings'),
		icon: '/images/KS_nav_views.png',
		window: win2
	});
	win2.containingTab = tab2;

	self.addTab(tab1);
	self.addTab(tab2);

	var FORWARD = 1, BACK = 2;
    var activity = self.activity;
    activity.onCreateOptionsMenu = function(e) {
        var menu = e.menu;
        var menuItem = menu.add({
            title : L('back'),
            itemId : BACK
        });
        menuItem.addEventListener('click', function(e) {
            //webView.goBack();
        });
        menuItem = menu.add({
            title : L('forward'),
            itemId : FORWARD
        });
        menuItem.addEventListener('click', function(e) {
            //webView.goForward();
        });
    };

	return self;
};

module.exports = ApplicationTabGroup;

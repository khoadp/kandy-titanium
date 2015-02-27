package com.kandy;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyAddressBookServiceNotificationListener;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsListener;
import com.genband.kandy.api.services.addressbook.KandyEmailContactRecord;
import com.genband.kandy.api.services.addressbook.KandyPhoneContactRecord;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class AddressBookServiceProxy extends KrollProxy {

	private KandyAddressBookServiceNotificationListener _addressBookServiceNotificationListener = null;

	public AddressBookServiceProxy() {
		super();
	}

	@Kroll.method
	public void registerNotificationListener(
			final HashMap<String, KrollFunction> callbacks) {
		if (_addressBookServiceNotificationListener != null) {
			unregisterNotificationListener();
		}

		_addressBookServiceNotificationListener = new KandyAddressBookServiceNotificationListener() {

			public void onDeviceAddressBookChanged() {
				Utils.checkAndSendResult(getKrollObject(),
						callbacks.get("onDeviceAddressBookChanged"), null);
			}
		};

		Kandy.getServices()
				.getAddressBookService()
				.registerNotificationListener(
						_addressBookServiceNotificationListener);
	}

	@Kroll.method
	public void unregisterNotificationListener() {
		if (_addressBookServiceNotificationListener != null) {
			Kandy.getServices()
					.getAddressBookService()
					.unregisterNotificationListener(
							_addressBookServiceNotificationListener);
			_addressBookServiceNotificationListener = null;
		}
	}

	@Kroll.method
	public void getDeviceContacts(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		int[] filtersOrdinal = (int[]) args.get("filters");
		KandyDeviceContactsFilter[] filters = new KandyDeviceContactsFilter[filtersOrdinal.length];
		for (int i = 0; i < filtersOrdinal.length; ++i) {
			filters[i] = KandyDeviceContactsFilter.values()[filtersOrdinal[i]];
		}

		Kandy.getServices().getAddressBookService()
				.getDeviceContacts(filters, new KandyDeviceContactsListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded(List<IKandyContact> contacts) {
						JSONObject data = new JSONObject();
						// TODO: check this!
						try {
							data.put("size", contacts.size());

							JSONArray contactsArray = new JSONArray();
							for (IKandyContact contact : contacts) {
								JSONObject c = new JSONObject();

								// Get display name
								c.put("displayName", contact.getDisplayName());

								// Get emails
								JSONArray emails = new JSONArray();
								for (KandyEmailContactRecord email : contact
										.getEmails()) {
									JSONObject e = new JSONObject();
									e.put("address", email.getAddress());
									e.put("type", email.getType().name());
									emails.put(e);
								}
								c.put("emails", emails);

								// Get number phones
								JSONArray phones = new JSONArray();
								for (KandyPhoneContactRecord phone : contact
										.getNumbers()) {
									JSONObject p = new JSONObject();
									p.put("number", phone.getNumber());
									p.put("type", phone.getType().name());
									phones.put(p);
								}
								c.put("phones", phones);

								contactsArray.put(c);

							}

							data.put("contacts", contactsArray);

						} catch (JSONException e) {
							e.printStackTrace();
						}

						Utils.sendSuccessResult(getKrollObject(), success, data);
					}
				});
	}
}

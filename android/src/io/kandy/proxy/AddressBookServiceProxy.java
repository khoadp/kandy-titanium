package io.kandy.proxy;

import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.*;
import com.genband.kandy.api.services.common.KandyResponseListener;
import io.kandy.KandyModule;
import io.kandy.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Address book service
 * 
 * @author kodeplusdev
 * 
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class AddressBookServiceProxy extends KrollProxy {

	private static final String LCAT = AddressBookServiceProxy.class
			.getSimpleName();

	public AddressBookServiceProxy() {
		super();
	}

	@Kroll.method
	public void getDeviceContacts(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		List<KandyDeviceContactsFilter> filters = new ArrayList<KandyDeviceContactsFilter>();

		int[] filterNames = (int[]) args.get("filters");

		if (filterNames != null) {
			KandyDeviceContactsFilter[] kandyDeviceContactsFilters = KandyDeviceContactsFilter
					.values();
			for (int name : filterNames) {
				try {
					filters.add(kandyDeviceContactsFilters[name]);
				} catch (Exception e) {
				}
			}
		}

		if (filters.size() == 0)
			filters.add(KandyDeviceContactsFilter.ALL);

		Kandy.getServices()
				.getAddressBookService()
				.getDeviceContacts(
						filters.toArray(new KandyDeviceContactsFilter[filters
								.size()]),
						new KandyContactsListenerCallback(success, error));
	}

	@Kroll.method
	public void getDomainDirectoryContacts(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices()
				.getAddressBookService()
				.getDomainDirectoryContacts(
						new KandyContactsListenerCallback(success, error));
	}

	@Kroll.method
	public void getFilteredDomainDirectoryContacts(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String searchString = args.getString("search");
		if (searchString == null)
			searchString = "";

		KandyDomainContactFilter filter;
		try {
			filter = KandyDomainContactFilter.values()[args.getInt("filter")];
		} catch (Exception e) {
			filter = KandyDomainContactFilter.ALL;
		}

		Kandy.getServices()
				.getAddressBookService()
				.getFilteredDomainDirectoryContacts(filter, false,
						searchString,
						new KandyContactsListenerCallback(success, error));
	}

	@Kroll.method
	public void getPersonalAddressBook(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices()
				.getAddressBookService()
				.getPersonalAddressBook(
						new KandyContactsListenerCallback(success, error));
	}

	@Kroll.method
	public void addContactToPersonalAddressBook(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		KrollDict contact = args.getKrollDict("contact");
		KandyContactParams contactParams = new KandyContactParams();
		contactParams.initFromJson(new JSONObject(contact));

		Kandy.getServices()
				.getAddressBookService()
				.addContactToPersonalAddressBook(contactParams,
						new KandyContactListener() {

							@Override
							public void onRequestFailed(int code, String err) {
								Log.d(LCAT,
										"KandyContactListener->onRequestFailed() was invoked: "
												+ String.valueOf(code) + " - "
												+ err);
								KandyUtils.sendFailResult(getKrollObject(),
										error, code, err);
							}

							@Override
							public void onRequestSucceded(IKandyContact contact) {
								Log.d(LCAT,
										"KandyContactListener->onRequestSucceded() was invoked: "
												+ contact.getDisplayName());
								KandyUtils.sendSuccessResult(getKrollObject(),
										success, getContactDetails(contact));
							}
						});
	}

	@Kroll.method
	public void removePersonalAddressBookContact(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String contact = args.getString("contact");

		Kandy.getServices()
				.getAddressBookService()
				.removePersonalAddressBookContact(contact,
						new KandyResponseListener() {

							@Override
							public void onRequestFailed(int code, String err) {
								Log.d(LCAT,
										"KandyResponseListener->onRequestFailed() was invoked: "
												+ String.valueOf(code) + " - "
												+ err);
								KandyUtils.sendFailResult(getKrollObject(),
										error, code, err);
							}

							@Override
							public void onRequestSucceded() {
								Log.d(LCAT,
										"KandyResponseListener->onRequestSucceded() was invoked.");
								KandyUtils.sendSuccessResult(getKrollObject(),
										success);
							}
						});

	}

	private KrollDict getContactDetails(IKandyContact contact) {
		KrollDict obj = new KrollDict();

		obj.put("displayName", contact.getDisplayName());
		if (contact.getServerIdentifier() != null)
			obj.put("serverIdentifier", contact.getServerIdentifier().getUri());
		obj.put("emails", getEmailsFromContact(contact));
		obj.put("phones", getPhonesFromContact(contact));

		return obj;
	}

	private JSONArray getPhonesFromContact(IKandyContact contact) {
		JSONArray phones = new JSONArray();

		if (contact.getNumbers() != null) {
			for (KandyPhoneContactRecord phone : contact.getNumbers()) {
				KrollDict p = new KrollDict();
				p.put("number", phone.getNumber());
				p.put("type", phone.getType().name());
				phones.put(p);
			}
		}

		return phones;
	}

	private JSONArray getEmailsFromContact(IKandyContact contact) {
		JSONArray emails = new JSONArray();

		if (contact.getEmails() != null) {
			for (KandyEmailContactRecord email : contact.getEmails()) {
				KrollDict e = new KrollDict();
				e.put("address", email.getAddress());
				e.put("type", email.getType().name());
				emails.put(e);
			}
		}

		return emails;
	}

	private class KandyContactsListenerCallback extends KandyContactsListener {

		KrollFunction success, error;

		public KandyContactsListenerCallback(KrollFunction success,
				KrollFunction error) {
			this.success = success;
			this.error = error;
		}

		@Override
		public void onRequestFailed(int code, String err) {
			Log.d(LCAT,
					"KandyContactsListener->onRequestFailed() was invoked: "
							+ String.valueOf(code) + " - " + err);
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}

		@Override
		public void onRequestSucceded(List<IKandyContact> contacts) {
			Log.d(LCAT,
					"KandyContactsListener->onRequestSucceded() was invoked: "
							+ contacts.size());

			JSONArray results = new JSONArray();

			for (IKandyContact contact : contacts) {
				results.put(getContactDetails(contact));
			}

			KandyUtils.sendSuccessResult(getKrollObject(), success, results);
		}
	}
}

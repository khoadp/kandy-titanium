package com.kandy;

import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.IKandyContact;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsListener;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class AddressBookServiceProxy extends KrollProxy {

	public AddressBookServiceProxy() {
		super();
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
						HashMap data = new HashMap();
						Utils.sendSuccessResult(getKrollObject(), success, data);
					}
				});
	}
}

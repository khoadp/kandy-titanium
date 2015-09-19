# Titanium Kandy Module

## Description

[**Kandy**](http://www.kandy.io/) is a full-service cloud platform that enables real-time communications for business applications. **io.kandy** is a [Titanium module](http://www.appcelerator.com/) that makes it easy to use Kandy API with Titanium project.

**Kandy** homepage: [kandy.io](http://www.kandy.io/)

## Accessing the kandy Module

To access this module from JavaScript, you would do the following:

    var Kandy = require("io.kandy");

The Kandy variable is a reference to the Module object.

## Reference

### Kandy.constants

**Status codes:** `STATUS_SUCCESS`, `STATUS_ERROR`

**Kandy device contact filters:** `DEVICE_CONTACT_FILTER_ALL`, `DEVICE_CONTACT_FILTER_HAS_EMAIL_ADDRESS`, `DEVICE_CONTACT_FILTER_HAS_PHONE_NUMBER`, `DEVICE_CONTACT_FILTER_IS_FAVORITE`

**Kandy domain contact filters:** `DOMAIN_CONTACT_FILTER_ALL`, `DOMAIN_CONTACT_FILTER_FIRST_AND_LAST_NAME`, `DOMAIN_CONTACT_FILTER_USER_ID`, `DOMAIN_CONTACT_FILTER_PHONE`

**Kandy record types:** `RECORD_TYPE_CONTACT`, `RECORD_TYPE_GROUP`

**Kandy thumbnail sizes:** `THUMBNAIL_SIZE_SMALL`, `THUMBNAIL_SIZE_MEDIUM`, `THUMBNAIL_SIZE_LARGE`

**Kandy camera info constants:** `CAMERA_INFO_FACING_BACK`, `CAMERA_INFO_FACING_FRONT`, `CAMERA_INFO_UNKNOWN`

**Kandy connection states:** `CONNECTION_STATE_UNKNOWN`, `CONNECTION_STATE_DISCONNECTED`, `CONNECTION_STATE_CONNECTED`, `CONNECTION_STATE_DISCONNECTING`, `CONNECTION_STATE_CONNECTING`, `CONNECTION_STATE_FAILED`

**Kandy call states:** `CALL_STATE_INITIAL`, `CALL_STATE_DIALING`, `CALL_STATE_SESSION_PROGRESS`, `CALL_STATE_RINGING`, `CALL_STATE_ANSWERING`, `CALL_STATE_TALKING`, `CALL_STATE_TERMINATED`, `CALL_STATE_ON_DOUBLE_HOLD`, `CALL_STATE_REMOTELY_HELD`, `CALL_STATE_ON_HOLD`

### Kandy.functions

##### setKey(String apiKey, String apiSecret)
Set keys for KandyAPI.

##### setKandyChatSettings(Json options)
Configuration Kandy chat service.
- `download_path_preference`: The default download path.
- `media_size_picker_preference`: The default media upload size.
- `download_policy_preference`: The default download network.
- `auto_download_thumbnail_size_preference`: The default download thumbnail size.

##### setHostUrl(String url)
Set Kandy host url.

##### getHostUrl(): String
Get current Kandy host url.

##### getReport(): String
Get Kandy report.

##### getSession(): Json
Get current session.
- `domain.apiKey`: The api key.
- `domain.apiSecret`: The secret key.
- `domain.name`: The Kandy domain name.
- `user.id`: The user id.
- `user.name`: The username.
- `user.deviceId`: The user device id.
- `user.password`: The user password.

### Kandy.proxies

#### Provisioning

```js
    // Ex:
    var provisioning = Kandy.createProvisioningService({
        twoLetterISOCountryCode: 'US',
        type: 'request' // 'validate', 'deactivate'
    });
    win1.add(provisioning);
```

##### requestCode(Json args)
Request code for verification and registration.
- `phoneNumber`: The phone number.
- `countryCode`: The country code.
- `success`: The success callback function.
- `error`: The error callback function.

##### validate(Json args)
Validation of the signed up phone number send received code to the server.
- `phoneNumber`: The phone number.
- `countryCode`: The country code.
- `otp`: The opt code.
- `success`: The success callback function.
- `error`: The error callback function.

##### deactivate(KrollDict callbacks)
Signing off the registered account(phone number) from a Kandy.
- `success`: The success callback function.
- `error`: The error callback function.

#### Access

```js
    // Ex:
    var access = Kandy.createAcessService({
        // add lifecycle events for listeners
        lifecycleContainer: win1 // or manually call (un)registerNotificationListener
    });
    win1.add(access);
```

**Listeners:** `onConnectionStateChanged`, `onInvalidUser`, `onSDKNotSupported`, `onSessionExpired`, `onSocketConnected`, `onSocketConnecting`, `onSocketDisconnected`, `onSocketFailedWithError`

##### registerNotificationListener()
Register kandy access notification listener.

##### unregisterNotificationListener()
Unregister kandy access notification listener.

#####  login(KrollDict args)
Register/login the user on the server with credentials received from admin or userAccessToken.
- `username`: The username.
- `password`: The user's password.
- `token`: The token to login (prioritized).
- `success`: The success callback function.
- `error`: The error callback function.

##### logout(KrollDict args)
Unregisters user from the server.
- `success`: The success callback function.
- `error`: The error callback function.

##### isLoggedIn(): Boolean
Check the user is logged in or not.

#### Call

```js
    // Ex:
    // only call proxy (no TiUiView)
    var call = Kandy.createCallService({
        // add lifecycle events for listeners
        lifecycleContainer: win1 // or manually call (un)registerNotificationListener
    });
    
    // widget example
    var callWidget = Kandy.createCallWidget({
        callee: "demo@demo.com",
        callText: "call demo",
        callType: "video",
        callTypes: ["video", "pstn"],
        widgets: ["outgoing", "incomming"],
        // add lifecycle events for listeners
        lifecycleContainer: win1 // or manually call (un)registerNotificationListener
    });
    win1.add(callWidget);
    
    // video view example
    var videoView = Kandy.createCallView(/*{local: "demo@demo.com"}*/);
    videoView.setLocalVideoView("demo@demo.com");
    win1.add(videoView);
```

**Listeners:** `onCallStateChanged`, `onGSMCallConnected`, `onGSMCallDisconnected`, `onGSMCallIncoming`, `onIncomingCall`, `onMissedCall`, `onVideoStateChanged`, `onWaitingVoiceMailCall`

##### registerNotificationListener()
Register kandy access notification listener.

##### unregisterNotificationListener()
Unregister kandy access notification listener.

###### CallServiceProxy

##### createVoipCall(KrollDict args)
#####  createPSTNCall(KrollDict args)
##### createSIPTrunkCall(KrollDict args)
Do a call to entered phone number.
- `callee`: The callee phone number.
- `startWithVideo`: Make call with video enabled (voip only).
- `success`: The success callback function.
- `error`: The error callback function.

##### accept(KrollDict args)
Accept incoming call.
- `callee`: The callee who is accepted.
- `startWithVideo`: Accept call with video enabled.
- `success`: The success callback function.
- `error`: The error callback function.

##### ignore(KrollDict args)
Ignoring the incoming call -  the caller wont know about ignore, call will continue on his side.

##### reject(KrollDict args)
Reject the incoming call.

##### hangup(KrollDict args)
Hangup a active call.

##### mute(KrollDict args)
Mute current active call.

##### unmute(KrollDict args)
Unmute a active call.

##### hold(KrollDict args)
Hold a active call.

##### unhold(KrollDict args)
Unhold a active call.

##### switchVideoOn(KrollDict args)
Turn video sharing on for a active video call.

##### switchVideoOff(KrollDict args)
Turn video sharing off for a active video call.

##### switchCameraFront(KrollDict args)
Switch to front camera for a video sharing.

##### switchCameraBack(KrollDict args)
Switch to back camera for a video sharing.

#### switchCamera(KrollDict args)
Switch to front or back camera for video sharing.
- `callee`: The callee who is accepted.
- `camera`: The camera info.
- `success`: The success callback function.
- `error`: The error callback function.

##### switchSpeakerOn(KrollDict args)
Switch speaker on for current active calls.

##### isInCall(): Boolean
Check if there have a active call or not.

##### isInGSMCall(): Boolean
Check if there have a active GSM call or not.

###### Call View

##### setLocalVideoView(String callee)
Set this view to a active call as a local view.

##### setRemoteVideoView(String callee)
Set this view to a active call as a remote view.

#### Chat

```js
    // Ex:
    var chat = Kandy.createChatService({
        // add lifecycle events for listeners
        lifecycleContainer: win1 // or manually call (un)registerNotificationListener
    });
    win1.add(chat);
```

**Listeners:** `onChatDelivered`, `onChatMediaAutoDownloadFailed`, `onChatMediaAutoDownloadProgress`, `onChatMediaAutoDownloadSucceded`, `onChatReceived`

##### registerNotificationListener()
Register kandy access notification listener.

##### unregisterNotificationListener()
Unregister kandy access notification listener.

##### sendSMS(KrollDict args)
Send a sms message to recipient.
- `destination`: The recipient.
- `title`: The title of the message.
- `message`: The message content.
- `success`: The success callback function.
- `error`: The error callback function.

##### sendChat(KrollDict args)
Send a text chat message.
- `destination`: The recipient.
- `type`: The type of the message (Kandy record types: CONTACT, GROUP).
- `message`: The message content.
- `success`: The success callback function.
- `error`: The error callback function.

#####  pickAudio()
#####  pickVideo()
#####  pickImage()
#####  pickContact()
#####  pickFile()
Pick a file to send.

##### sendAudio(KrollDict args)
##### sendVideo(KrollDict args)
##### sendImage(KrollDict args)
##### sendContact(KrollDict args)
##### sendFile(KrollDict args)
Send a file to the recipient.
- `destination`: The recipient.
- `type`: The type of the message (Kandy record types: CONTACT, GROUP).
- `caption`: The message caption.
- `uri`: The uri of the file.
- `success`: The success callback function.
- `error`: The error callback function.

#####  sendCurrentLocation(KrollDict args)
Send the current location to recipient.
- `destination`: The recipient.
- `type`: The type of the message (Kandy record types: CONTACT, GROUP).
- `caption`: The message caption.
- `success`: The success callback function.
- `error`: The error callback function.

#####  sendLocation(KrollDict args) 
Send a location to the recipient.
- `destination`: The recipient.
- `type`: The type of the message (Kandy record types: CONTACT, GROUP).
- `caption`: The message caption.
- `location`: The location info (json).
- `success`: The success callback function.
- `error`: The error callback function.

##### cancelMediaTransfer(KrollDict args)
Cancel the current media transfer.
- `uuid`: The uuid of the message.
- `success`: The success callback function.
- `error`: The error callback function.

##### downloadMedia(KrollDict args)
Download media file from server.

##### downloadMediaThumbnail(KrollDict args)
Download thumbnail media file from server.

##### markAsReceived(KrollDict args)
Make a messgae as received.

##### pullEvents(KrollDict args)
Pull pending messages.
- `success`: The success callback function.
- `error`: The error callback function.

#### Group

```js
    // Ex:
    var group = Kandy.createGroupService({
        // add lifecycle events for listeners
        lifecycleContainer: win1 // or manually call (un)registerNotificationListener
    });
    win1.add(group);
```

**listeners:** `onGroupDestroyed`, `onGroupUpdated`, `onParticipantJoined`, `onParticipantKicked`, `onParticipantKicked`, and included chat listeners.

##### registerNotificationListener()
Register kandy access notification listener.

##### unregisterNotificationListener()
Unregister kandy access notification listener.

##### createGroup(KrollDict args)
Create a new group.
- `name`: The name of the group.
- `success`: The success callback function.
- `error`: The error callback function.

##### getMyGroups(KrollDict args)
Get the groups list.
- `success`: The success callback function.
- `error`: The error callback function.

##### getGroupById(KrollDict args)
Get the group detail by id.
- `id`: The id of the group.
- `success`: The success callback function.
- `error`: The error callback function.

##### updateGroupName(KrollDict args)
Update group name.
- `id`: The id of the group.
- `name`: The name of the group.
- `success`: The success callback function.
- `error`: The error callback function.

##### updateGroupImage(KrollDict args)
Update group image
- `id`: The id of the group.
- `uri`: The uri of the image.
- `success`: The success callback function.
- `error`: The error callback function.

##### removeGroupImage(KrollDict args)
Remove group image.
- `id`: The id of the group.
- `success`: The success callback function.
- `error`: The error callback function.

##### downloadGroupImage(KrollDict args)
Download group image.
- `id`: The id of the group.
- `success`: The success callback function.
- `error`: The error callback function.

##### downloadGroupImageThumbnail(KrollDict args)
Download thumbnail group image.
- `id`: The id of the group.
- `thumbnailSize`: The thumbnail size.
- `success`: The success callback function.
- `error`: The error callback function.

##### muteGroup(KrollDict args)
Mute notifications on a group.

##### unmuteGroup(KrollDict args)
Unmute notifications on a group.

##### destroyGroup(KrollDict args)
Destroy a group.

##### leaveGroup(KrollDict args)
Leave a group.
- `id`: The id of the group.
- `success`: The success callback function.
- `error`: The error callback function.

##### addParticipants(KrollDict args)
Add participants to a group.

##### muteParticipants(KrollDict args)
Mute participants of a group.

##### unmuteParticipants(KrollDict args)
Unmute participants of a group.

##### removeParticipants(KrollDict args)
Remove participants out of a group.
- `id`: The id of the group.
- `participants`: The participants list.
- `success`: The success callback function.
- `error`: The error callback function.

#### Presence

##### startWatch(KrollDict args)
Start watch status of the users.
- `list`: The user list.
- `success`: The success callback function.
- `error`: The error callback function.

#### Location

##### getCountryInfo(KrollDict args)
Get the country info.
- `success`: The success callback function.
- `error`: The error callback function.

##### getCurrentLocation(KrollDict args)
Get current location info
- `success`: The success callback function.
- `error`: The error callback function.

#### Address book

##### getDeviceContacts(KrollDict args) 
Get local contacts from user device.
- `filters`: The contacts filters.
- `success`: The success callback function.
- `error`: The error callback function.

##### getDomainDirectoryContacts(KrollDict args) 
Get domain contacts.
- `success`: The success callback function.
- `error`: The error callback function.

##### getFilteredDomainDirectoryContacts(KrollDict args)
Get filtered domain contacts.
- `filters`: The contacts filters.
- `search`: The search query.
- `success`: The success callback function.
- `error`: The error callback function.

##### getPersonalAddressBook(KrollDict args)
Get personal address book.
- `success`: The success callback function.
- `error`: The error callback function.

##### addContactToPersonalAddressBook(KrollDict args) 
Add a contact to personal address book.
- `contact`: The contact info.
- `success`: The success callback function.
- `error`: The error callback function.

##### removePersonalAddressBookContact(KrollDict args)
Remove a contact from personal address book.
- `contact`: The contact info.
- `success`: The success callback function.
- `error`: The error callback function.

#### Cloud storage

#####  uploadMedia(KrollDict args)
Upload a file to cloud storage.
- `uri`: The uri of the file.
- `success`: The success callback function.
- `error`: The error callback function.

##### downloadMedia(KrollDict args)
##### downloadMediaThumbnail(KrollDict args)
##### cancelMediaTransfer(KrollDict args) 
Download media from cloud storage.
- `uuid`: The UUID of the file.
- `filename`: The file saved name.
- `success`: The success callback function.
- `error`: The error callback function.

#### Push

##### enablePushNotification(KrollDict args)
Enable push notification.
- `success`: The success callback function.
- `error`: The error callback function.

##### disablePushNotification(KrollDict args)
Diable push notification.
- `success`: The success callback function.
- `error`: The error callback function.

## Author

## License

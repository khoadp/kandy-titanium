# Titanium Kandy Module

[**Kandy**](http://www.kandy.io/) is a full-service cloud platform that enables real-time communications for business applications. **io.kandy** is a [Titanium module](http://www.appcelerator.com/) that makes it easy to use Kandy API with Titanium project.

**Kandy** homepage: [kandy.io](http://www.kandy.io/)

## Supported Platforms
- Android
- IOS (coming soon)

## Installation
View the [Using Titanium Modules](http://docs.appcelerator.com/titanium/latest/#!/guide/Using_Titanium_Modules) document for instructions on getting started with using this module in your application.

Edit the modules section of your tiapp.xml file to include this module:
```xml
  <modules>
    <module platform="android">io.kandy</module>
  </modules>
```

## Getting Started
To access this module from JavaScript, you would do the following (recommended):

```js
	var Kandy = require('io.kandy');
```

The "Kandy" variable is now a reference to the Module object.

Then, you have to initialize `KandyAPI` to use in the `values/kandy_config.xml` of the module
```xml
  <resources>
    <string name="kandy_api_key">Your api key here.</string>
    <string name="kandy_api_secret">Your secret key here.</string>
  </resources>
```
or at run-time
```js
  Kandy.setKey("your api key here", "your secret key here");
```

For usage see the code in [the example folder](example/).   
Documentation can be found [here](documentation/).

## Contributors

## Legal
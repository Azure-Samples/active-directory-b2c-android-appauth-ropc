---
services: active-directory-b2c
platforms: android
author: parakhj
---

# Integrate Azure AD B2C natively into an Android application using ROPC

**This sample demonstrates how to use Azure AD B2C using a 3rd party library called AppAuth. It has only been tested for compatibility in basic scenarios with Azure AD B2C. Issues and feature requests should be directed to the library's open-source project.**

This sample is a quickstart to help you get started with Azure AD B2C on Android using a 3rd party library called AppAuth. The focus of this sample is to show you how you can *natively build a sign-in experience* within your app while using Azure AD B2C. **This sample uses an authorization flow called Resource Owner Password Credential (ROPC).** Unless an absolute requirement, we do not recommend using this flow. You should use the authorization code flow, which is demonstrated in this [sample](https://github.com/Azure-Samples/active-directory-b2c-android-native-appauth).


This sample was adapted from the [original Android AppAuth sample](https://github.com/openid/AppAuth-Android). For more details on how the sample and the library work, please look at the original sample.

## Steps to Run


Follow the instructions below to configure the sample for your Azure AD B2C configuration. To use Azure AD B2C, you'll first need to create an Azure AD B2C tenant, register your application, and create a "Resource owner policy".

* To create an Azure AD B2C tenant, checkout [these steps](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-get-started).

* To register your app, checkout [these steps](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-app-registration).  Make sure the "Native Client" switch is turned to "Yes". You will need to supply a Redirect URL with a custom scheme in order for your Android application to capture the callback. To avoid a collision with another application, we recommend using an unique scheme. The example redirect URI in this sample is: `com.onmicrosoft.fabrikamb2c.exampleapp://oauth/redirect`. We recommend replacing fabrikamb2c with your tenant name, and exampleapp with the name of your application.

* Create a Resource Owner Policy.

* Clone the code

### Setting up the Android App

1. In Android Studio, click on "File"->"New"->"Import Project" and select the cloned folder. You will likely get a few errors and need to install some additional tools in Android Studio. Follow the prompts and let Android Studio update the local data.

2. Inside `/app/res/raw/auth_config.json`, replace the following fields:

   * `client_id`: This is your Application ID, which can be found in the Azure Portal (under Application settings).
   * `redirect_uri`: This is your redirect URI, which can be found in the Azure Portal (under Application settings).
   * `authorization_scope`: This is the value for the scope parameter that will be passed in the request.
   * `discovery_uri`: This is the metadata URL for the resource owner policy.

3. Inside `/app/build.gradle`, replace the value for `appAuthRedirectScheme`. This should correspond to the scheme of the `redirect_uri`.

4. Inside `app/java/net/openid/appauthdemo/TokenActivity.java`, update the values for the `username` and `password` in the params object.

5. Go ahead and try the app.  When you click authorize, it will attempt to sign in using ROPC. Upon completing the login process, you should see the types of tokens acquired. As you will notice, the app did not open the browser at all.


## Questions & Issues

Please file any questions or problems with the sample as a github issue. You can also post on [StackOverflow](https://stackoverflow.com/questions/tagged/azure-ad-b2c) with the tag `azure-ad-b2c`.

This sample was built and tested with the Android Virtual Device Manager on versions 25 using Android Studio 3.0.0.

## Acknowledgements

This sample was adapted from the [Android AppAuth sample](https://github.com/openid/AppAuth-Android).


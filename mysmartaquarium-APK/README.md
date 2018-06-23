## My Smart Aquarium-APK (Android)

This project aims to manage an aquarium remotely via Android app and Esp32 chip.
It use AWS IoT APIs to publish to and subscribe from MQTT topics. Authentication of the connection is done with Amazon Cognito. Once a connection to the AWS IoT platform has been established, the application presents a simple UI to publish and subscribe over MQTT.

It is created for:

  - Learning mobile computing
  - Learning to work with Arduino, particularly ESP32 chip
  - Developing a project for UniParthenope (in specific for mobile computing exam)
  
## Requirements

* AndroidStudio or Eclipse
* Android API 10 or greater

## How to use

* For login (Amazon Cognito):

1. Create Your user pool on the Cognito console
   - Go to https://console.aws.amazon.com/cognito/ , the console contains step-by-step instructions to create the pool.
   - Click on "_Manage your User Pools_" to open Your User Pools browser.
   - Click on "_Create a User Pool_" to open "_Create a user pool_" page, here you can start creating a user pool.
   - In "_Create a user pool_" page, give your pool a name and select "_Review default_" - this will create a user pool with default settings.
   - Click on "_Create pool_" to create the new user pool.
   - After creating a new pool, navigate to "_Apps_" page (select "_Apps_" from the navigation options on the left hand side).
   - Click "_Add an app_" and give a name to the app, e.g. "My Android App".
   - Click "_Create app_" to generate the app client id.
   - Get the App client id and App client secret, if the secret was generated. To see the App client secret click on "_Show Details_".
   - Get the "_Pool Id_" from the "_Pool details_" page.

2. Download and import the MySmartAquarium project into your Android Studio
   - From the Welcome screen, click on "_Import project_".
   - Browse to the MySmartAquarium directory and click OK.
   - Accept requests to add Gradle to the project.
   - If the SDK reports missing Android SDK packages (such as Build Tools or the Android API package), import AWS SDK.
      
3. Modify the app to run it on your user pool.
   - Open the file __AppHelper.java__ from the project files.
   - Locate these four variables and add your pool details: 
      * __userPoolId__ set this to your pool id.
      * __clientId__ set this to your app client id.
      * __clientSecret__ set this to your app client secret associated with the app client id. If your app client id does not have an associated client secret, set this variable to null, i.e. _clientSecret_ = _null_.
      * __cognitoRegion__ set this to AWS Cognito Your User Pools region.




* Managing AWS IoT (publish and subscribe topic):

	This sample will create a certificate and key, save it in the local java key store and upload the certificate to the AWS IoT platform.  To upload the certifiate, it requires a Cognito Identity with access to AWS IoT to upload the device certificate. Use Amazon Cognito to create a new identity pool (or you can reuse an identity pool that you previously created):
	*  In the [Amazon Cognito Console](https://console.aws.amazon.com/cognito/), press the `Manage Federated Identities` button and on the resulting page press the `Create new identity pool` button.
	*  Give your identity pool a name and ensure that `Enable access to unauthenticated identities` under the `Unauthenticated identities` section is checked.  This allows the sample application to assume the unauthenticated role associated with this identity pool.  Press the `Create Pool` button to create your identity pool.


	As part of creating the identity pool, Cognito will setup two roles in [Identity and Access Management (IAM)](https://console.aws.amazon.com/iam/home#roles).  These will be named something similar to: `Cognito_<<PoolName>>Auth_Role` and `Cognito_<<PoolName>>Unauth_Role`.  You can view them by pressing the `View Details` button on the console.  Now press the `Allow` button to create the roles.

  *Note the `Identity pool ID` value that shows up in red in the "Getting started with Amazon Cognito" page. It should look similar to: `us-east-1:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx". Also, note the region that is being used.
 
 Next,  we will attach a policy to the unauthenticated role to setup permissions to access the required AWS IoT APIs.  This is done by first creating the IAM Policy shown below in the [IAM Console](https://console.aws.amazon.com/iam/home#roles) and then attaching it to the unauthenticated role.  In the IAM console, Search for the pool name that you created  and click on the link for the unauth role.  Click on the "Add inline policy" button and add the following policy using the JSON tab. Click on "Review Policy", give the policy a descriptive name and then click on "Create Policy".  This policy allows the sample app to create a new certificate (including private key) and attach a policy to the certificate.

        ```
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": [
                "iot:AttachPrincipalPolicy",
                "iot:CreateKeysAndCertificate"
              ],
              "Resource": [
                "*"
              ]
            }
          ]
        }
        ```

        **Note**: to keep this example simple it makes use of unauthenticated users in the identity pool.  This can be used for getting started and prototypes but unauthenticated users should typically only be given read-only permissions if used in production applications.  More information on Cognito identity pools can be found [here](http://aws.amazon.com/cognito/), information on AWS IAM roles and policies can be found [here](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_manage.html), and information on AWS IoT policies can be found [here](http://docs.aws.amazon.com/iot/latest/developerguide/authorization.html).

	1. The configuration we have setup up to this point will enable the Sample App to connect to the AWS IoT platform using Cognito and upload certificates and policies.  Next, we will need to create a policy, that we will attach to the Device Certificate that will authorize the certificate to connect to the the AWS IoT message broker and peform publish, subscribe and receive operations. To create the policy in AWS IoT,
    *  Navigate to the [AWS IoT Console](https://console.aws.amazon.com/iot/home) and press the `Get Started` button.  On the resulting page click on `Secure` on the side panel and the click on `Policies`.
    * Click on `Create`
    * Give the policy a name.  Note this name as you will use it in the application when making the attach policy API call.
    * Click on `Advanced Mode` and replace the default policy with the following text and then click the `Create` button.

        ```
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": "iot:Connect",
              "Resource": "*"
            },
            {
              "Effect": "Allow",
              "Action": [
                "iot:Publish",
                "iot:Subscribe",
                "iot:Receive"
              ],
              "Resource": "*"
            }
          ]
        }
        ```
    

**Note**: To keep things simple, This policy allows access to all the topics under your AWS IoT account. This can be used for getting started and prototypes. In product, you should scope this policy down to specific topics, specify them explicitly as ARNs in the resource section: `"Resource": "arn:aws:iot:<REGION>:<ACCOUNT ID>:topic/<<mytopic/mysubtopic>>"`.

	Open the AndroidPubSub project.

	Open `UserActivity.java` and update the following constants:

    ```
    CUSTOMER_SPECIFIC_ENDPOINT = "<CHANGE_ME>";
    ```
    The customer specific endpoint can be found on the IoT console settings page. 

    ```
    COGNITO_POOL_ID = "<CHANGE_ME>";
    MY_REGION = Regions.US_EAST_1;
    ```
    This would be the name of the Cognito pool ID and the Region that you noted down previously. 

    ```
    AWS_IOT_POLICY_NAME = "CHANGE_ME";
    ```
    This would be the name of the AWS IoT policy that you created previously. 

    ```
    KEYSTORE_NAME = "iot_keystore";
    KEYSTORE_PASSWORD = "password";
    CERTIFICATE_ID = "default";
    ```
    For these parameters, the default values will work for this application.  The keystore name is the name used when writing the keystore file to the application's file directory.  The password is the password given to protect the keystore when written.  Certificate ID is the alias in the keystore for the certificate and private key entry.  

   **Note**: If you end up creating a keystore off of the device you will need to update this to match the alias given when importing the certificate into the keystore.

	Build and run the app.

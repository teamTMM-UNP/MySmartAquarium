(function(){function r(e,n,t){function o(i,f){if(!n[i]){if(!e[i]){var c="function"==typeof require&&require;if(!f&&c)return c(i,!0);if(u)return u(i,!0);var a=new Error("Cannot find module '"+i+"'");throw a.code="MODULE_NOT_FOUND",a}var p=n[i]={exports:{}};e[i][0].call(p.exports,function(r){var n=e[i][1][r];return o(n||r)},p,p.exports,r,e,n,t)}return n[i].exports}for(var u="function"==typeof require&&require,i=0;i<t.length;i++)o(t[i]);return o}return r})()({1:[function(require,module,exports){
/*
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * NOTE: You must set the following string constants prior to running this
 * example application.
 */
var awsConfiguration = {
   poolId: 'us-west-2:67723020-9d50-4cf3-b77b-9b44606d8615',
   //poolId: 'us-west-2:85590f6d-69ad-4def-9ba1-1ec70a5bb457', // 'YourCognitoIdentityPoolId'
   host: 'a3hi6rxv4kn75d.iot.us-west-2.amazonaws.com', // 'YourAwsIoTEndpoint', e.g. 'prefix.iot.us-east-1.amazonaws.com'
   region: 'us-west-2' // 'YourAwsRegion', e.g. 'us-east-1'
};
module.exports = awsConfiguration;


},{}],2:[function(require,module,exports){
/*
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

//
// Instantiate the AWS SDK and configuration objects.  The AWS SDK for 
// JavaScript (aws-sdk) is used for Cognito Identity/Authentication, and 
// the AWS IoT SDK for JavaScript (aws-iot-device-sdk) is used for the
// WebSocket connection to AWS IoT and device shadow APIs.
// 
var AWS = require('aws-sdk');
var AWSIoTData = require('aws-iot-device-sdk');
var AWSConfiguration = require('./aws-configuration.js');

console.log('Loaded AWS SDK for JavaScript and AWS IoT SDK for Node.js');


//
// Create a client id to use when connecting to AWS IoT.
//
var clientId = 'mqtt-explorer-' + (Math.floor((Math.random() * 100000) + 1));

//
// Initialize our configuration.
//
AWS.config.region = AWSConfiguration.region;

AWS.config.credentials = new AWS.CognitoIdentityCredentials({
   IdentityPoolId: AWSConfiguration.poolId
});

//
// Create the AWS IoT device object.  Note that the credentials must be 
// initialized with empty strings; when we successfully authenticate to
// the Cognito Identity Pool, the credentials will be dynamically updated.
//
const mqttClient = AWSIoTData.device({
   //
   // Set the AWS region we will operate in.
   //
   region: AWS.config.region,
   //
   ////Set the AWS IoT Host Endpoint
   host:AWSConfiguration.host,
   //
   // Use the clientId created earlier.
   //
   clientId: clientId,
   //
   // Connect via secure WebSocket
   //
   protocol: 'wss',
   //
   // Set the maximum reconnect time to 8 seconds; this is a browser application
   // so we don't want to leave the user waiting too long for reconnection after
   // re-connecting to the network/re-opening their laptop/etc...
   //
   maximumReconnectTimeMs: 8000,
   //
   // Enable console debugging information (optional)
   //
   debug: true,
   //
   // IMPORTANT: the AWS access key ID, secret key, and sesion token must be 
   // initialized with empty strings.
   //
   accessKeyId: '',
   secretKey: '',
   sessionToken: ''
});

//
// Attempt to authenticate to the Cognito Identity Pool.  Note that this
// example only supports use of a pool which allows unauthenticated 
// identities.
//
var cognitoIdentity = new AWS.CognitoIdentity();
AWS.config.credentials.get(function(err, data) {
   if (!err) {
      console.log('retrieved identity: ' + AWS.config.credentials.identityId);
      var params = {
         IdentityId: AWS.config.credentials.identityId
      };
      cognitoIdentity.getCredentialsForIdentity(params, function(err, data) {
         if (!err) {
            //
            // Update our latest AWS credentials; the MQTT client will use these
            // during its next reconnect attempt.
            //
            mqttClient.updateWebSocketCredentials(data.Credentials.AccessKeyId,
               data.Credentials.SecretKey,
               data.Credentials.SessionToken);
         } else {
            console.log('error retrieving credentials: ' + err);
            alert('error retrieving credentials: ' + err);
         }
      });
   } else {
      console.log('error retrieving identity:' + err);
      alert('error retrieving identity: ' + err);
   }
});

//
// Connect handler; update div visibility and fetch latest shadow documents.
// Subscribe to lifecycle events on the first connect event.
//
window.mqttClientConnectHandler = function() {
   console.log('connect');
   document.getElementById("container").style.visibility = 'visible';

   var topic = $('#test').text();
   console.log('ID: ' + topic);

   //
   // Subscribe to our current topic.
   //
   mqttClient.subscribe(topic);
};

//
// Reconnect handler; update div visibility.
//
window.mqttClientReconnectHandler = function() {
   console.log('reconnect');
   document.getElementById("container").style.visibility = 'hidden';
};

//
// Utility function to determine if a value has been defined.
//
window.isUndefined = function(value) {
   return typeof value === 'undefined' || typeof value === null;
};

var term;
var light;
var opt;
var back;
//
// Message handler for lifecycle events; create/destroy divs as clients
// connect/disconnect.
//
window.mqttClientMessageHandler = function(topic, payload) 
{
   console.log('message: ' + topic + ':' + payload.toString());
   var data = JSON.parse(payload);
   document.getElementById('Temperature').innerHTML = 'Room temperature: ' + data.Temperature;
   document.getElementById('Humidity').innerHTML = 'Room humidity: ' + data.Humidity;
   document.getElementById('Light').innerHTML = 'Spotlight: ' + data.Light;
   document.getElementById('Temp_mean').innerHTML = 'Water Temperature: ' + data["Water Temperature Mean"];
   document.getElementById('Temp_set').innerHTML = 'Optimal Temperature: ' + data.Temp_set;
   term = data.Term;
   light = data.Light;
   opt = data.Opt;
   back = data.Led;
};



//
// Handle the UI to update the topic we're publishing on
//
window.updatePublishTopic = function() {};


window.Light = function()
{

	if(light == 0)
	{
		mqttClient.publish('ITEM','PRESA2_ON');
		light = 1;
	}
	else if(light == 1)
	{
		mqttClient.publish('ITEM','PRESA2_OFF');
		light = 0;
	}
}

window.Term = function()
{

	if(term == 0)
	{
		mqttClient.publish('ITEM','PRESA1_ON');
		term = 1;
	}
	else if(term == 1)
	{
		mqttClient.publish('ITEM','PRESA1_OFF');
		term = 0;
	}
}

window.Back = function()
{

	if(back == 0)
	{
		mqttClient.publish('ITEM','LED_ON');
		back = 1;
	}
	else if(back == 1)
	{
		mqttClient.publish('ITEM','LED_OFF');
		back = 0;
	}
}

window.Opt = function()
{
	if(opt == 0)
	{
		mqttClient.publish('ITEM','PRESA3_ON');
		opt = 1;
	}
	else if(opt == 1)
	{
		mqttClient.publish('ITEM','PRESA3_OFF');
		opt = 0;
	}
}


window.Food = function()
{
	mqttClient.publish('ITEM','FEED');
}

//
// Install connect/reconnect event handlers.
//
mqttClient.on('connect', window.mqttClientConnectHandler);
mqttClient.on('reconnect', window.mqttClientReconnectHandler);
mqttClient.on('message', window.mqttClientMessageHandler);

},{"./aws-configuration.js":1,"aws-iot-device-sdk":"aws-iot-device-sdk","aws-sdk":"aws-sdk"}]},{},[2]);

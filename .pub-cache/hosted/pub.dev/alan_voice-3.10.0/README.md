# Alan voice assistant SDK for Flutter

This plugin allows you to add voice to your app. Create an in-app voice assistant to enable human-like conversations and provide a personalized voice experience for every user.

<img src="https://storage.googleapis.com/alan-public-images/github/phone-ads.gif" height="325px" align="right"/>

## Alan is a Voice AI Platform

Alan is a conversational voice AI platform that lets you create an intelligent voice assistant for your app. It offers all necessary tools to design, embed and host your voice solutions:

#### Alan Studio

A powerful web-based IDE where you can write, test and debug dialog scenarios for your voice assistant or chatbot.

#### Alan Client SDKs

Alan's lightweight SDKs to quickly embed a voice assistant to your app.

#### Alan Cloud

Alan's AI-backend powered by the industry’s best Automatic Speech Recognition (ASR), Natural Language Understanding (NLU) and Speech Synthesis. The Alan Cloud provisions and handles the infrastructure required to maintain your voice deployments and perform all the voice processing tasks.

To get more details on how Alan works, see <a href="https://alan.app/platform" target="_blank">Alan Platform</a>.

## Why Alan?

* **No or minimum changes to your UI**: To voice enable your app, you only need to get the Alan Client SDK and drop it to your app.
* **Serverless environment**: No need to plan for, deploy and maintain any infrastructure or speech components - the Alan Platform does the bulk of the work.
* **On-the-fly updates**: All changes to the dialogs become available immediately.
* **Voice flow testing and analytics**: Alan Studio provides advanced tools for testing your dialog flows and getting the analytics data on users' interactions, all in the same console.

## How to start

To create a voice assistant for a Flutter app:

1. [Sign up for Alan Studio](https://studio.alan.app/register) to build and test voice scripts.
2. Use this plugin to embed a voice assistant to your app:

 a. In the `pubspec.yaml` file, add the Alan voice dependency:

 ```yaml
 //pubspec.yaml file
 ...
 dependencies:
  flutter:
   sdk: flutter
  ...
  alan_voice: 3.x.xx 
 ```

 b. Add the `alan_voice` package dependency:

 ```dart
 import 'package:alan_voice/alan_voice.dart';
 ```

 c. Add the Alan button to your main widget:

 ```dart
 AlanVoice.addButton(
        "8e0b083e795c924d64635bba9c3571f42e956eca572e1d8b807a3e2338fdd0dc/stage",
        buttonAlign: AlanVoice.BUTTON_ALIGN_LEFT);
 ```

 d. Replace the key above with the Alan SDK key of your project in Alan Studio.

That's it. Now run your app and tap the Alan button!

## Examples

### Beginner

[alan_simple_app](https://github.com/alan-ai/alan-sdk-flutter/tree/master/examples/alan_simple_app): an example demonstrating how to integrate Alan with a Flutter app.

### Advanced

[Flutter Shrine](https://github.com/alan-ai/alan-sdk-flutter/tree/master/examples/ShrineApp): a shopping app with a voice interface synchronized with the app visual elements.


## Want to learn more?

For more information, see [Alan documentation](https://alan.app/docs/usage/getting-started).
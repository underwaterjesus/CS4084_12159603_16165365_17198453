# UPDATE 11 Aug 2020:
### The Discover Limerick app has been graded as part of the CS4084 Mobile Application Development module.
### The Google Cloud services connected with the app have been closed, and therefore cloning this repo will not result in a working application and the Google Cloud keys are no longer valid.
### We are making this repo public for those interested in the project or who wish to reuse the code. If the repo is cloned it will need to be reconnected to a new Google Cloud account and new keys will need to be used in place of those in our code. The database would also have to be rebuilt or the corresponding code edited.

# Discover Limerick
Discover Limerick is an application designed for locals and visitors.<br>
It aims to simplify the process of discovering things to see and do around Limerick City and the surrounding area.<br>
To make it easy to find what you're looking for, the app splits these activities into seven handy categories:<br>
* Amusements
* Arts & Culture
* Bars
* Family
* Nightlife
* Outdoors
* Sports

When you select an activity/location, you are presented with its name, address, EirCode(if applicable) and a brief description. For the visually impaired, we provide Text-to-Speech(TTS) functionality. You can view reviews and images of the location that have been left by other users. If you choose to create an account and log in, you can upload your own reviews and images. All of your uploads, across each location, can be viewed together on one screen, to save you looking in multiple places. The location can be viewed on Google Maps via our "View on Map" button.
<br><br>
If you allow the app access to your location, it will display the distance you currently are from any activity. Our app does require internet access to work. If you are having issues, the app will assist you with your network connectivity problems. All our data is stored on a Google Firebase NoSQL database, and files are stored on Google Firestore. The Firebase project can be found [here](https://console.firebase.google.com/u/0/project/discover-limerick/overview "Firebase Project"). Access for authorised users only.
<br><br>
## Getting Started
* What do I need to do, to get the app running on my device?

  If you have access to the apk, you can transfer it to your device and select install. You may have to change your settings to allow third party apps. You can also run the app on your device in debug mode. Open the apk in Android studio and conect your mobile device to your computer. Select your device in Android Virtual Device Manager(AVD) and click the "Run app" button or press Shift + F10. You can also follow the steps in the next bullet point, but swapping the selected device in the AVD to your connected mobile device.
* How do I run the app from source code in Android Studio?

  The app can be opened from GitHub directly in Android Studio. When opening a new project, select "Git", under the "Check out project from Version Control" menu. You can also download the files in a compressed folder. You can extract the files and open the project in Android Studio. If you have the apk, you can also open this in Android Studio. Once in Android Studio, click the "Run app", or press Shift + F10.<br>
* External Project Links

  [Discover Limerick Firebase Project](https://console.firebase.google.com/u/0/project/discover-limerick/overview "Firebase Project")<br>
  [Discover Limerick Google Maps Project](https://console.cloud.google.com/google/maps-apis/new?project=discover-limerick "Google Maps Project")
<br><br>
## More information

* [Technical Design](design.md "Technical Design")
* [Navigation Structure](structure.md "NavigationSstructure")

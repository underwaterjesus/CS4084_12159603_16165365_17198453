# Technical Design of the Discover Limerick App
The main technologies used in the Discover Limerick app are:
* Advanced UI elements such as fragments
* Google Maps API
* Google Firebase Database
* Google Firebase Firestore
* Google Firebase Authentication
* Google Text-to-Speech(TTS) Engine
* LocationProvider
* Connectivity Manager
## Technical Design Choices

## Lessons Learned
The most important lesson we learned during the development of the Discover Limerick app was the importance of the design phase. It is important to understand properly how the various components and APIs work before any coding begins. This allows for better planning of how to integrate the various elements of the program, particularly those that operate asynchronously. If we were to restart this project we would put extra effort into the design phase, as we believe this would result in fewer iterations of the development cycle and improve the performance of or app. Many issues were solved dynamically, on the fly, that would likely have been avoided with better planning. As mentioned previously, a main area of difficulty were asynchronous operations, and as our app heavily involved interaction with a database, these issues appeared quite often. If starting the app again, we would also research much more thoroughly the APIs available to us. We admit we likely rushed into development very hastily and missed opportunities to leverage existing frameworks. The main example of this is that we did not make use of the Google Places API. This is a powerful tool, but we are still proud of what we created without it and believe it is still a quite useful application.

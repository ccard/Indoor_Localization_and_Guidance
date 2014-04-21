Indoor Localization and Guidance
================================
Chris Card (ccard)

#Description
This repository contains the mobel program for my thesis on *"Indoor Markerless Localization and Guidance for the Mobile
 Environment"*.  It uses the open source libraries for *OpenCV* and *Android SDK*

#Setup and Installation
##Required Libraries
 - [Android SDK](https://developer.android.com/sdk/installing/index.html): Free and opensource Android development tools.
 - OpenCV libraries for Android: This is the Android and java versions of Opencv and contains most functionality of OpenCV.  I recomend downloading it from NVIDA's Developer site and its called [Tegra Android Development Pack](https://developer.nvidia.com/tegra-resources) for using Opencv with android. It also contains the Android SDK's and several other helpful tools that you can choose to install if you wish. There is also another open source Opencv Library for java and android that can be [downloaded](http://sourceforge.net/projects/opencvlibrary/files/opencv-android/2.4.4/) but i recomend the one from NVIDA.
 - IDE: you can use [eclispe](https://www.eclipse.org/) to develope this application but I am using and recomend
 [Intellij](http://www.jetbrains.com/idea/).

##Getting your Environment Setup
- Android Environment (for Intellij)
 - To use the opencv library: first goto *Project Structure* (*File*->*Project Structure*).  Then goto *Modules* and
  click the *+* button, and goto where the android sdk was saved. Then goto the *Libraries* tab and click the *+* button
  and point the path to the android opencv *\sdk\src* folder.  Whan asked what modules depend on this library select
  the android sdk module.  Then in the *Modules* tab and make the opencv sdk module a dependency of you primary project.
 - To set up google play services import the google-play-services_lib as a module.  then in the *Library* tab  import
  the google-play-services.jar that you copied to your local directory.  When asked what module depends on this library
  select the google-play-services_lib.  In the *Module* tab select the primary project and make google-play-services_lib
  a dependency.  under the google-play-services_lib module goto the *Paths* tab and select
  'Use module compile output path'. To set up the Authentication for using google drive (this is for uploading logs)
  follow the [tutorial](https://developers.google.com/drive/android/get-started). When setting up the Google developer
  console ensure that the *Consent screen* tab under *APIs & auth* tab for your project is properly filled out
  (other wise your device won't connect to google drive)
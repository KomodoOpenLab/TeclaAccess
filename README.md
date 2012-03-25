Tecla Access
============

Tecla is a set of open software and hardware tools that facilitate switch access to electronic devices
for people with mobility impairments. This repo contains the source code for the
[Tecla Access App for Android](https://play.google.com/store/apps/details?id=ca.idi.tekla).

Getting Involved
----------------
There are many ways to contribute to the [Tecla Access App for Android](https://play.google.com/store/apps/details?id=ca.idi.tekla). Here are some ideas:

### Code

In order to start contributing code to the Tecla Access project, follow the steps below:

1. Fork this repo. For detailed instructions visit [http://help.github.com/fork-a-repo/](http://help.github.com/fork-a-repo/)
2. Download and install [Eclipse](http://www.eclipse.org/)
3. Download and install the [Android SDK](http://developer.android.com/sdk/index.html)
4. Create a new Android project in Eclipse named *Tecla Access* (target Android 2.0)
5. Import the *Tecla Access* project's source from the *source* directory in your local repo (you may need to let Eclipse copy the source to the workspace)
6. Create a new Android **library** project in Eclipse named *Tecla Access SDK* (target Android 2.0)
7. Import the *Tecla Access SDK* project's source from the *sdk* directory in your local repo (you may need to let Eclipse copy the source to the workspace)
8. Go to the properties of your *Tecla Access* project and add the project *Tecla Access SDK* as a library
9. You should now be able to compile the project
10. Hack away! but please make sure you follow [this branching model] (http://nvie.com/posts/a-successful-git-branching-model/). That means, make your pull requests against the **develop** branch, not the **master** branch.

### Review

Another very useful way to contribute to the Tecla Access project is to identify any bugs or issues that may still be lurking in the [Tecla Access App for Android](https://play.google.com/store/apps/details?id=ca.idi.tekla). You can also submit your requests for features that you think the [Tecla Access App for Android](https://play.google.com/store/apps/details?id=ca.idi.tekla) is still missing. To get started follow the steps below:

1. [Sign up for a fee github account](https://github.com/signup/free) if you don't already have one
2. [Download the Tecla Access App for Android](https://play.google.com/store/apps/details?id=ca.idi.tekla) from the Google Play Store.
2. Use Tecla Access as your main input method on your Android device
3. Report issues and submit feature requests to your heart's content via a the [Tecla Access issues page](https://github.com/jorgesilva/TeclaAccess/issues) 

### Translate

Translating Tecla Access to your mother tongue (or voting on completed translations) is the easiest way to contribute to the project. To start translating follow the steps below:

1. [Sign up for a free crowdin.net account](http://crowdin.net/join) if you don't already have one.
2. Visit the [Tecla Access translation page](http://crowdin.net/project/tecla-access) and check if the language you want to work on is already there.
3. If the language you want to work on is not shown on the [Tecla translation page](http://crowdin.net/project/tecla-access), [let us know](http://komodoopenlab.com/about_us/contact/) so we can add it.
4. If your language has not been completed, take a moment to contribute some translations, otherwise feel free to vote or make alternative suggestions to the strings already translated.

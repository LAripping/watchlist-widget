<img src="app/src/main/res/mipmap-hdpi/ic_launcher.png" align="right"/>

# Watchlist Widget

> An android app that beatifully displays your IMDB watchlist on a home screen widget! 

This app aims to provide a home screen widget for your IMDB watchlist. Something that the official IMDB app never supported and probably never will! In a few easy steps, you can have a scrollable widget to pick your next movie from, with posters, ratings and all. 

"Watchlist Widget" is a simplistic Android application, heavily inspired by "[TV Time](https://www.tvtime.com/)" (formerly "TV Show Time"). Behind the scenes, it uses:

* [OMDB API](http://www.omdbapi.com/) for fetching posters. Read [below](#usage-instructions) how to get your free API key and specify it in the app 
* [Gllide](https://github.com/bumptech/glide) image loading and caching library
* [Expandable-fab](https://github.com/nambicompany/expendable-fab) library for a pumped up  Floating Action Button (FAB)
* [OkHttp](https://square.github.io/okhttp/) library for fetching IMDB lists 





## Features

:white_check_mark: ​Importing IMDB Watchlist from downloaded CSV file

:white_check_mark: ​Pointing to a *public* IMDB Watchlist to track

:white_check_mark: Placing fancy, scrollable widgets on your home screen (pics below)

:white_check_mark: Clicking titles to get to the IMDB page

:white_check_mark: Pull down main app screen to manually refresh widget from tracked IMDB list 

| List Widget                 | Grid Widget                    | Stack Widget                   |
| --------------------------- | ------------------------------ | ------------------------------ |
| ![](listwidget-cropped.jpg) | :hourglass: ***Coming soon!*** | :hourglass: ***Coming soon!*** |





### Disclaimer :warning:

This is a free time hobby, I'm a hacker, not a dev and while I'd like to see people enjoy the app and I'd probably fix a bug or two,  take this repo as it is.

:x: The **code is probably shit** through the eyes of a proper dev

:x: **No tests in sight**, nor unit nor regression or anything. Too bored

:x: I've **only tried it in a couple of devices**, and since it works, it works :P



## Usage Instructions

1. Download the latest APK from the [Releases](https://github.com/laripping/watchlist-widget/releases) tab 

2. "Allow untrusted sources" and install the app from your download folder

3. :warning: Remember to re-disable installing from untrusted sources!  

4. Launch the app and populate your watchlist using one of the following ways:

   * From an IMDB list - *Just make sure your list is public!* 

     > ...official IMDB guidance on the relevant [FAQ](https://help.imdb.com/article/imdb/track-movies-tv/watchlist-faq/G9PA556494DM8YBA#)

     The app (and widget) will always show the latest version of specified list 

   * From a pre-exported IMDB list, downloaded as a CSV file to your device

5. You like posters don't you? Register for a free OMDB API key [here](http://www.omdbapi.com/apikey.aspx) and specify it in the app's settings

   > ...otherwise I'd need to do naughty stuff with the private IMDB API

6. Place a widget on your screen and start scrolling! :popcorn::popcorn:















## Roadmap

- [ ] Last Setting, update Readme, first release! (signed APK on Github releases)

- [ ] Styling
  - [ ] display a collage/tiling of titles' posters, below the Status ("Empty"/"N titles"), fading towards the FAB 

    OR

  - [ ] Checklist. Titles "Let's get started!" / "Almost there!" / "All set and ready!"

    > Subtitle shown if task not completed

    - [ ] X Titles added (check DB count) - Click the button below to add some movies
    - [ ] Tracking list "prefs.name" (check prefs.url) - Point to an IMDB list to track as your watchilst
    - [ ] Widget Placed - Long press your home screen to place a watchlist widget
    - [ ] Key for Posters specified (check prefs) - Generate your OMDB API key and provide it in Settings

- [ ] **Spinners and Loading TODOs**
  
  - [ ] That broken refresh animation
  
- [ ] TODOs and Error handling 
  - [ ] What happens to posters when API key reaches max?
  - [ ] Test case: multiple widgets?
  
- [ ] Security fixes
  - [ ] Encrypted Prefs to protect the user's watchlist (and OMDB key)
  - [ ] Provider permission
  - [ ] Permission (?) to launch `ConfigActivity`
  
- [ ] Implement different  [Widget types](https://developer.android.com/guide/topics/appwidgets/collections):
  - [x] List
  - [ ] Grid
  - [ ] Stack
  - [ ] Flipper

- [ ] Larger display on larger screens (higher dpi?)

- [ ] Widget customisation?

- [ ] 
  End long titles in ...


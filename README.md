<img src="app/src/main/res/mipmap-hdpi/ic_launcher.png" align="right"/>

# Watchlist Widget

> An android app that beatifully displays a user's watchlist and has a home screen widget for it too!> 

In every Android's I've ever owned, I would always make room in one of the home screens for movies, as a go-to place for when I want to chill and enjoy a film, populating it with all the relevant apps and widgets. What's always annoyingly missing is a cool widget to list my options and remind of good movies I've noted. Of course, like million other users, I use my **IMDB** watchlist for this purpose... but the IMDB App doesn't offer a Widget to display any list.

This app aims to bridge that gap. Simplistic, and heavily inspired by "[TV Time](https://www.tvtime.com/)" (formerly "TV Show Time") - another excellent app (*which as of recently supports tracking Movies as well!*). It uses:

* [OMDB API](http://www.omdbapi.com/) for fetching posters, see [Compiling](#Compiling) for instructions on how to get your free API key and bundle within the app 
* [Gllide](https://github.com/bumptech/glide) image loading and caching library



### Disclaimer :warning:

This is a free time hobby, I'm not a dev - I'm a security consultant and while I'd like to see people using my app and maybe take a glimpse of the support lifecycle, take this repo as it is:

:white_check_mark: Through the eyes of a proper dev, the **code is probably shit**

:white_check_mark: Of course **it doesn't include tests**, nor unit nor regression no nothing

:white_check_mark: It will by no means support many devices, hell, I've **only tried it in a Xiaomi Redmi 5 Plus** 

 





## Features

- [x] Importing IMDB Watchlist from downloaded CSV file

- [ ] **Coming Soon!** :hourglass: Pointing to a *public* IMDB Watchlist to track

- [x] Placing fancy, scrollable widgets on your home screen (pics below!)

- [ ] Clicking titles to get to the IMDB page

  | List Widget                 | Grid Widget | Stack Widget |
  | --------------------------- | ----------- | ------------ |
  | ![](listwidget-cropped.jpg) |             |              |

  



## Compiling

1. Clone the project and load into Android Studio

2. Register for a free OMDB API key [here](http://www.omdbapi.com/apikey.aspx)  - otherwise I'd need to do naughty stuff with the private IMDB API 

3. Place it in a file in the project root as below, named `apikey.properties`

   ```
   OMDB_KEY="XXXXXXXXX"
   ```

4. Build and run as usual 



## Roadmap

#### Static Import

- [x] Single-screen App that allows CSV import (button #1 - action ), 
  - [x] saves it (`Provider`), backed by `SQLiteDatabase`
  - [x] lists a counter on main screen to validate 
    - [ ] on Main Screen display a collage/tiling of titles' posters, fading towards the Status ("Empty"/"N titles")
- [x] ...and exposes  a bare `ListView`  widget 
  - [x] (ideally) with fetched title icons if space / when resized
- [ ] (button #2 - 3dots) Widget settings (`SettingsActivity` ?)
  - [x] Clear all data
- [x] Styling

#### Dynamic Tracking

- [ ] FAB Expand action like LastPass?
  * original button will become just ":heavy_plus_sign:"
  * expanded options are " :mag:" for static import and ":globe_with_meridians:" for URL 
  * [ ] maybe with disclaimer that list needs to be made "public" and pointer to IMDB FAQs
  * [ ] URL prompt -> loader -> loaded! 
- [ ] Widget-initiated-flow with `ConfigActivity`
- [ ] TODOs and Error handling 
- [ ] Implement different  [Widget types](https://developer.android.com/guide/topics/appwidgets/collections):
  - [x] List
  - [ ] Grid
  - [ ] Stack
  - [ ] Flipper
# Watchlist Widget

> An android app that beatifully displays a user's watchlist and has a home screen widget for it too!

In every Android smartphone I get, always make room in one of my home screens for the movies, as a go-to place for when I want to chill and enjoy a film with all the apps needed to do so. What is always missing is a cool widget to list my options and remind of the good movies I have stashed and provide me the links for their IMDB entries in my watchlist.

There was never an app for that since IMDB does not (officially) provide an API so I decided to make one.


## The Facts

* Some very handy third party APIs exist which could be usefull:
	* [OMDB API](https://www.omdbapi.com/)
	* [Another one](http://imdbapi.poromenos.org/) less sleek, but thumbs up for another Greek! 
	
* IMDB Watchlists can be exported by hitting this link: (when logged-in)
	http://www.imdb.com/list/export?list_id=ls075069559&author_id=ur54621898&ref_=wl_exp
  
* To appear logged-in you only need the ```id``` cookie:

```json
	{
	    "domain": ".imdb.com",
	    "expirationDate": 1548699599.487852,
	    "hostOnly": false,
	    "httpOnly": false,
	    "name": "id",
	    "path": "/",
	    "sameSite": "no_restriction",
	    "secure": false,
	    "session": false,
	    "storeId": "0",
	    "value": "....",
	    "id": 3
	}
```


## The Plan

1. In a neat, single-screen App, the user will be prompted for **login** to IMDb.com 
> That's the missing part: *How do we log the user in?* 
2. The **cookie** will be extracted to send with every following request
3. The above URL will be hit, to get the **watchlist** in .csv 
4. Parsing will be made to get desired info, even using the **APIs** listed above for resources not available in the CSV file (e.g. the Poster)
5. Appear them nicely in polished, IMDb-styled, good looking **widget**
6. Browse the movie you want and relax!


## Ideas

* Material design
* The app logo will consist of a small, golden list with the IMDb logo on top. 
* App Colors: Gold, Black, White* List / Grid Display in the Widget
* Scrolling widget
* Color each row/block with the color extracted from the poster (like google results/imdb stripe)
* Option for ```watchlist.csv``` import, right from the main Screen
* Reverse engineer the IMDb app for a way to perform the login


## TODOs

* Mention the Icon generator in the 'About' page, see License.





## Roadmap

- [x] Single-screen App that allows CSV import (button #1 - action ), 
  - [x] saves it (`Provider`), backed by `SQLiteDatabase`
  - [x] lists a counter on main screen to validate 
    - [ ] (ideally) list titles in tiles with `CursorLoader`
- [ ] ...and exposes  a bare widget 
- [ ] (button #2 - 3dots) Widget settings -> `SettingsActivity`
  - [ ] Clear all data
- [ ] (button #3) Step-by-Step flow (initiated by same #1 action button - expanding 2nd option like LP) that 
  1. Spawns webview, loads IMDB
  2. prompts user to login
  3. prompts user to download their watchlist
  4. Spawn file explorer & select CSV 
  5. import - "Voila! Now pick your widget" 
- [ ] Widget-initiated-flow
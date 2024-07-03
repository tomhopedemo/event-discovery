**Skedge**

Parses, analyzes, filters and cleans scraped web data, to output a curated list of events for each configured city. 

The approximately DateMatcher, TimeMatcher and DateTimeMatcher classes contain the various parsing mechanisms for all date, time and datetime configurations that are encountered in the UK. 

The ClassParser is the most commonly invoked mechanism to parse website data. 

There is a merging algorithm which de-duplicates the events promoted by multiple websites.

Additional natural language filtering and cleaning is applied, to normalize the event titles displayed to the user

The HTML classes compile the events information to a set of static webpages, generated for each area/date. 

An API server can be spun up also to publish the events data via a REST API.  

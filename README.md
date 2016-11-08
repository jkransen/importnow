# ImportNow

Import now, think later. A system that allows importing data files, saving the contents, 
and adding metadata during and after import.

For now, only CSV files are supported. The first line is expected to contain headers. 
The headers are converted to URIs and used as predicates when the records are
saved to a triple store. 

After uploading, a mapping screen is shown to allow customization of the mapping from 
header name to predicate URI. Here, it will also be possible to skip columns, or add
additional metadata like [uniquely identifying values](https://www.w3.org/wiki/InverseFunctionalProperty).

After importing, the records can be listed, as well as the properties.

Optionally, for triple store, the application can connect to a running instance of GraphDB. 
In this case, GraphDB's front end also allows browsing and querying the data.

# Development environment

Install the [Lightbend Activator](https://www.lightbend.com/activator/download) and 
add the bin/ to your $PATH for convenience.

Run the Activator UI

    activator ui

A browser tab will be opened. 

Press `Code view & Open in IDE` to generate IDE project files for Eclipse or IntelliJ IDEA.

Press the Run button to run the application.

# Configuration

In `conf/application.conf`, the entries on the bottom are application specific, and 
start with "importnow".

# Remote repository (Optional)

Download and install [GraphDB Free](http://info.ontotext.com/graphdb-free-ontotext) from Ontotext. 
Connect to is through your browser, and create a new repo, e.g. "importnow".

Set the same name in the `conf/application.conf` file under `importnow.remoterepo.repo`.

Check that `importnow.remoterepo.url` is correct and leads to your GraphDB instance.

Set `importnow.isLocalRepo` to false

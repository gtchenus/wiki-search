#CS6200 Project-Wiki Search
Code author
-----------
Guanting Chen, 04/27/2021

##Compile and Run
To compile and run, make sure a JDK version of 1.8 and above has been installed in your environment and can be accessed by shell. Make sure that we have the Wiki article XML dump file to index under the `./src/` folder. Create a folder of `./src/intermediate` and `./src/index`. Download the page rank file from https://data.world/falcon/wikipedia-pagerank-20160804 and store it under `./src/pageranks.csv`. Fix the XML dump path from your environment in the source code.

* Open the command line interface and navigate to the code dircetory `cd ./src`
* Use javac `javac Main.java` and `javac Query.java` to compile the program 
* Use `java Main` to run the index construction.
* Use `java Query` to start the Wiki-search Web interface and prepare to search
* While successfully running the program, follow instructions on the command line to open `localhost:9000` on your browser and input a query.
* Once a query has been submitted, the top-30 relevant Wiki pages with anchored titles be presented on the web interface together with performance metrics. 


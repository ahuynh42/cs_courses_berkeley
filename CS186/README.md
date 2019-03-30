# CS186 Introduction to Database Systems
In this course I learned about buffers, joins, query optimization, record locking, logging, and recovery. This course also taught SQL at an introductory level.
## _project_main
This course had a database system implementation as a term project. This was split into four parts. Because all the files are shared between parts, I have copied the implemented files for each part in separate folders below. They can all be found in /_project_main/src/main/java/edu/berkeley/cs186/database/table/.
## project1-File-Management
This project was implementing fixed-length records and numerous auxiliary functions. It concluded with finishing an iterator subclass that iterates over all valid records in a page.
## project2-B-Plus-Trees
This project was implementing B-plus tree functionality. This included key insertion and node splitting. It also concluded with finishing an iterator subclass that iterates over keys in a B-plus tree. This iterator was able to also do equality lookups, bounded range lookups, and full index scans.
## project3-Query-Operators
This project was implementing page nested loop join, block nested loop join, grace hash join, and sort merge join operators. There was also an index scan operator capable of performing an equals, less than, less than equals, greater than, and greater than equals on a given index.
## project4-Query-Optimization
This project was optimizing table queries. I estimated IO costs for query operators. I then optimized single table access selections followed by optimizing join selections.
## tests
This is a test suite consisting of tests that I have written for all four projects. These can be found in /_project_main/src/test/java/edu/berkeley/cs186/database/.
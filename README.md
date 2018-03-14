### How to run?

In IntelliJ
* Edit configuration
* in Command Line Args add 5005 and run server
* in Command Line Args add 5006 and run server
* Run client
* Client only supports 5005 and 5006, hard coded. Will change later.

Now able to write from any server and calling main server from that server. 

Reading code is also done. Need to implement propagation code though using multicast and notify client on write completion.
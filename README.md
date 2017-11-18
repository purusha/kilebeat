# kilebeat
[filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-overview.html) in java using [AKKA](http://akka.io)

For the first release with support only two connector 
- generic http POST
- kafka 

We also support stop and resume of endpoint connector (losing all messages in the period when server connector's was down).
Before considering a failed connection, up to 3 tests are performed (it will become a configuration). 

Example configuration and usage:
```
exports = [
    {
        path = "/Users/power/Tmp/a"
        send-if-match = "^\\d.*"			#OPTIONAL 		
        http {
            url = "http://localhost:55555/log"
        }
    }
    {
        path = "/Users/power/Tmp/*.log"
        send-if-not-match = ".*[1-9].*"		#OPTIONAL
        http {
            url = "http://localhost:55555/test"
        }
    }
    {
        path = "/Users/power/Tmp/q"
        send-if-match = "^\\d.*"			#OPTIONAL        
        kafka {
            host = "localhost:44444"
            queue = "test"
        }
    }
]
```

An example of json sent to the connector is
```
{"line":"test 123","ts":1511043044203,"path":"/Users/power/Tmp/9cd29f449df8192c2d0de449e1e7583f"}
```

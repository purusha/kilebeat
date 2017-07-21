# kilebeat
[filebeat](https://www.elastic.co/guide/en/beats/filebeat/current/filebeat-overview.html) in java using [AKKA](http://akka.io)

For the first release with support only two connector 
- generic http (JSON over POST)
- kafka 

We also support stop and resume of endpoint connector (losing all messages in the period when server connector's was down)

Example configuration and usage:
```
exports = [
    {
        path = "/Users/power/Tmp/a"
        http {
            url = "http://localhost:55555/log"
        }
    }
    {
        path = "/Users/power/Tmp/*.log"
        http {
            url = "http://localhost:55555/test"
        }
    }	
]
```

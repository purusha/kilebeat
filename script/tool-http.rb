require 'webrick'

class Echo < WEBrick::HTTPServlet::AbstractServlet
  def do_GET(request, response)
    puts request
    response.status = 200
  end
  
  def do_POST(request, response)
    puts request
    response.status = 200
  end
end

class ToolHttp
  def initialize
  	@http_t = nil
  	@server = nil
  end
  
  def start  
	@http_t = Thread.new {
		@server = WEBrick::HTTPServer.new(:Port => 55555)
		@server.mount "/", Echo  	
		@server.start    	
	}  
  end
  
  def stop
  	@server.shutdown
  	sleep 3
  	
  	Thread.kill(@http_t)
  	sleep 2

  	@http_t = nil
  	@server = nil  	
  end
end

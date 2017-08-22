require 'webrick'

class Always200 < WEBrick::HTTPServlet::AbstractServlet
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
		@server.mount "/", Always200  	
		@server.start    	
	}  
  end
  
  def stop
  	if !@server.nil?
  	  	@server.shutdown
  		sleep 3  	
  	end 
  	
  	if !@http_t.nil?
	  	Thread.kill(@http_t)
	  	sleep 2
  	end

	@server = nil
  	@http_t = nil  	  	
  end
end

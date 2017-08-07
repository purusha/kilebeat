require 'securerandom'
require 'erb'

print 'Enter number of files: [10]'
files = gets.chomp
if files.to_s.empty?
	files = 9
end

print 'Enter root of files: [/Users/power/Tmp/]'
root = gets.chomp
if root.to_s.empty?
	root = "/Users/power/Tmp/"
end

Dir.foreach(root) do |file|
	if ((file.to_s != ".") and (file.to_s != ".."))
		File.delete("#{root}/#{file}")
	end
end

for number in 0..files
	file_name = SecureRandom.hex
	
	File.open(root + file_name, 'w') do |f|
		f.print file_name
	end	
end

print 'Enter path where to write configuration file: [/Users/power/Dev/github/kilebeat/confs/]'
conf_file = gets.chomp
if conf_file.to_s.empty?
	conf_file = "/Users/power/Dev/github/kilebeat/confs/"
end
conf_file += SecureRandom.hex
puts 'New configuration file will be write in ' + conf_file 

File.open(conf_file, 'w') do |f|
	@elements = Dir.foreach(root).reject {|file| file.to_s == "." or file.to_s == ".."} 
	f.print ERB.new(File.open("template.erb", 'r').read).result(binding)
end	

File.delete(File.readlink("/Users/power/Dev/github/kilebeat/kilebeat.conf"))
File.unlink("/Users/power/Dev/github/kilebeat/kilebeat.conf")
File.symlink(conf_file, "/Users/power/Dev/github/kilebeat/kilebeat.conf")

#REPL start Here
loop do
	print '$> '
	input = gets.chomp
	command, *params = input.split /\s/
	
  	case command
  	when /\Ahelp\z/i
    	#puts Application::Console.help_text
    	puts "help"
	when /\Aopen\z/i
    	#Application::Task.open params.first
    	puts "open"
  	when /\Ado\z/i
    	#Application::Action.perform *params
    	puts "do on #{params}"
  	else 
  		puts 'Invalid command'
  	end
end

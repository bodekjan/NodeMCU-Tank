wifi.setmode(wifi.STATION)
wifi.sta.config("519","hunningtu519")
print(wifi.sta.getip())
spd = 256
function pvmSpeed(speed)
    pwm.setup(pvmA, 100, spd*speed-1)
    pwm.start(pvmA) 
    pwm.setup(pvmB, 100, spd*speed-1) 
    pwm.start(pvmB) 
end
function gpInit()
    in1  = 3
    in2  = 4
    pvmA = 5
    in3  = 6
    in4  = 7
    pvmB = 8
    gpio.mode(in1, gpio.OUTPUT)  
    gpio.mode(in2, gpio.OUTPUT)  
    gpio.mode(in3, gpio.OUTPUT)  
    gpio.mode(in4, gpio.OUTPUT)
end
function stop()
    gpio.write(in1, gpio.LOW);  
    gpio.write(in2, gpio.LOW);  
    gpio.write(in3, gpio.LOW);  
    gpio.write(in4, gpio.LOW);
end
function moveDown(speed)
    gpio.write(in1, gpio.HIGH);  
    gpio.write(in2, gpio.LOW);  
    gpio.write(in3, gpio.HIGH);  
    gpio.write(in4, gpio.LOW); 
    pvmSpeed(speed)
end
function moveUp(speed)
    gpio.write(in1, gpio.LOW);  
    gpio.write(in2, gpio.HIGH);  
    gpio.write(in3, gpio.LOW);  
    gpio.write(in4, gpio.HIGH); 
    pvmSpeed(speed)
end
function moveLeft(speed)
    gpio.write(in1, gpio.LOW);  
    gpio.write(in2, gpio.HIGH);  
    gpio.write(in3, gpio.HIGH);  
    gpio.write(in4, gpio.LOW); 
    pvmSpeed(speed)
end
function moveRight(speed)
   gpio.write(in1, gpio.HIGH);  
   gpio.write(in2, gpio.LOW);  
   gpio.write(in3, gpio.LOW);  
   gpio.write(in4, gpio.HIGH); 
   pvmSpeed(speed)
end
gpInit()
stop()
srv = net.createServer(net.TCP) 
srv:listen(80,function(conn) 
    conn:on("receive", function(conn,request)
    local _, _, method, path, vars = string.find(request, "([A-Z]+) (.+)?(.+) HTTP");  
        local _GET = {}
        if (vars ~= nil)then  
            for k, v in string.gmatch(vars, "(%w+)=(%w+)&*") do  
                _GET[k] = v  
            end  
        end
        if(_GET.action == "moveup")then  
              moveUp(_GET.speed) 
        elseif(_GET.action == "movedown")then  
              moveDown(_GET.speed)
        elseif(_GET.action == "stop")then  
              stop()
        elseif(_GET.action == "right")then  
              moveRight(_GET.speed) 
        elseif(_GET.action == "left")then  
              moveLeft(_GET.speed)
        end  
    conn:close();
    end) 
end)
      
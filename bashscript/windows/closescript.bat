REM lsof -ti:7090 | xargs kill & lsof -ti:4210 | xargs kill
FOR /T /F  "tokens=5 delims= " %%P IN ('netstat -a -n -o ^| findstr :7090.*LISTENING') DO TaskKill.exe /PID %%P

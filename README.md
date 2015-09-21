# planet-wars

Eclipse project located at ./PlanetWars
Generate the executable in ./tools/mybot.jar
Navigate to ./tools
Run on terminal as -

java -jar tools/PlayGame.jar maps/map7.txt 1000 1000 log.txt "java -jar ./example_bots/RageBot.jar" "java -jar ./mybot.jar" | java -jar tools/ShowGame.jar | python visualiser/visualize-locally.py

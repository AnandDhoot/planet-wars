
#!/bin/bash
TIME_LIMIT=1000
TURN_LIMIT=200
PLAY_GAME=tools/PlayGame-1.2.jar
SHOW_GAME=tools/ShowGame.jar

mkdir -p .play/build
rm .play/build/*.class 2>/dev/null


        f="maps/map$1.txt"
        path=.play/game/$(echo $f | cut -c 6- | cut -d '.' -f 1)
        mkdir -p $path
        rm $path/* 2>/dev/null
        enemy_bot="java -jar example_bots/${2:-DualBot}.jar"
        my_bot="java -jar mybot.jar"
        java -jar $PLAY_GAME $f $TIME_LIMIT $TURN_LIMIT $path/log.txt "$my_bot" "$enemy_bot" >$path/stdout.log 2>$path/stderr.log
        cat $path/stdout.log | python visualiser/visualize_localy.py
        echo "$f: $(grep Wins $path/stderr.log) ($(grep Turn $path/stderr.log | tail -n 1))"
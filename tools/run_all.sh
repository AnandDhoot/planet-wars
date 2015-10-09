
#!/bin/bash
TIME_LIMIT=1000
TURN_LIMIT=102
PLAY_GAME=tools/PlayGame.jar
SHOW_GAME=tools/ShowGame.jar

mkdir -p .play/build
rm .play/build/*.class 2>/dev/null

avg=0
lost=0
    for f in maps/*; do
        path=.play/game/$(echo $f | cut -c 6- | cut -d '.' -f 1)
        mkdir -p $path
        rm $path/* 2>/dev/null
        enemy_bot="java -jar example_bots/${2:-DualBot}.jar"
        my_bot="java -jar mybot.jar"
        java -jar $PLAY_GAME $f $TIME_LIMIT $TURN_LIMIT $path/log.txt "$my_bot" "$enemy_bot" >$path/stdout.log 2>$path/stderr.log
        echo "$f: $(grep Wins $path/stderr.log) ($(grep Turn $path/stderr.log | tail -n 1))"

        if [ "$(grep Wins $path/stderr.log | cut -d \  -f 2)" -eq 2 ]; then
        	lost=$((lost+1))
        else
        	avg=$(( avg + $(grep Turn $path/stderr.log | tail -n 1 |cut -d \  -f 2) ))
    	fi
    done
    avg=$(( avg/(100-lost) ))
    echo "Average Winning Turns: $avg "
    echo "Number of losses: $lost"

    

f="maps/map$1.txt"
path=.play/game/$(echo $f | cut -c 6- | cut -d '.' -f 1)
echo "$f: $(grep Wins $path/stderr.log) ($(grep Turn $path/stderr.log | tail -n 1))"
python visualiser/visualize_localy.py <$path/stdout.log &
export PATH=$PATH:/usr/local/node/bin

# arg1=start, arg2=end, format: %s.%N  
function getTiming() {  
    start=$1  
    end=$2  
     
    start_s=$(echo $start | cut -d '.' -f 1)  
    start_ns=$(echo $start | cut -d '.' -f 2)  
    end_s=$(echo $end | cut -d '.' -f 1)  
    end_ns=$(echo $end | cut -d '.' -f 2)  
  
    time=$(( ( 10#$end_s - 10#$start_s ) * 1000 + ( 10#$end_ns / 1000000 - 10#$start_ns / 1000000 ) ))  
  
    echo "$time ms"  
}  

# arg1=message
function printTime() {
    message=$1  

    echo "$message: `date '+%Y-%m-%d %H:%M:%S'`"
}


echo 'build start -------------------------- '
start=$(date +%s.%N)
printTime "start"

[ -e $TMP_MOD ]||mkdir -p $TMP_MOD
[ -e webtmp ] && rm -fr webtmp
mkdir webtmp
cd webtmp

echo 'copy file -------------------------- '
cp -r ../../itone-web/src/main/websrc ./
cp -r ../../itone-web/src/main/webapp ./
cd ./websrc

printTime "copy"

echo 'node -v'
node -v
echo 'npm -v'
npm -v
echo 'npm config list'
npm config list
npm run check-nodesass
echo ''
echo 'npm install -------------------------- '
npm install || exit 5

printTime "install"

echo 'build -------------------------- '
npm run release-test || exit 5

end=$(date +%s.%N)

echo 'build end -------------------------- '
printTime "end"

getTiming $start $end
echo ''
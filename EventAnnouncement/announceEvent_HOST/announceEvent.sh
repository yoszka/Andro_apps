api_key=AIzaSyA3DeMKrdtb6gG4kYdnbHbm-JUvm1ouikg
#registration_id=APA91bFBxhdHTzCAC7F8vemyEYrXHV1n_MRor810XkFyrJ2BDbHwLGXqqcctcKqCBq2LJX3i8hIW2ieGQWriYhJ5psrNbnCwaos0635q8tNIgBDvBsmX7UeFwcMJe3hZGFVe_WKLbu9wTdDocNz_crJqKrbsHcYYUgVgr1teJ6kPnOBpJYKTyWQ

# ID 15
registration_id=APA91bGBQj9lhoOKrWIXUIo1gwrLCXz6zqf7RWaJmVQHg6ga8fhmiXHp2vTXOXPYHdEU-s_qBkiNHCP14Er-iewZ9ezXJHfyLt176wJFjciykKVMGwSFBqYrXAfNZIIeBQzEamYPmzIBZpLmysNmOjwrtXHzqVYtO3YzLB0VKBwg3eLN6i4oo48

token=location
message=
location_lat=51.1151300
location_lon=16.9506200
location_acc=100.0
location_provider=network
location_timestamp=1363388511000

#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "{\"registration_ids\":[\"$registration_id\"]}"

#{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"$message\",\"location_lat\":\"$location_lat\",\"location_lon\":\"$location_lon\",\"location_acc\":\"$location_acc\",\"location_provider\":\"$location_provider\",\"location_timestamp\":\"$location_timestamp\"}}

#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"$message\",\"location_lat\":\"$location_lat\",\"location_lon\":\"$location_lon\",\"location_acc\":\"$location_acc\",\"location_provider\":\"$location_provider\",\"location_timestamp\":\"$location_timestamp\"}}"

#echo "{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"$message\",\"location_lat\":\"$location_lat\",\"location_lon\":\"$location_lon\",\"location_acc\":\"$location_acc\",\"location_provider\":\"$location_provider\",\"location_timestamp\":\"$location_timestamp\"}}"





#curl -s  http://pinnote.zz.mu/list_all.php | grep -Po '"id":"15","gcm_regid":"(.*)' | grep -Po 'gcm_regid":"(.*)' | grep -Po '[^:"]{20,}'
registration_id=`curl -s  http://pinnote.zz.mu/list_all.php | grep -Po '"id":"'$1'","gcm_regid":"(.*)' | grep -Po 'gcm_regid":"(.*)' | grep -Po '[^:"]{20,}' | head -1`
#aaaaa='"id":"'$1'","gcm_regid":"(.*)'
#echo $aaaaa
echo $registration_id

# to check if LocationReceiver works
#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"location_lat\":\"$location_lat\",\"location_lon\":\"$location_lon\",\"location_acc\":\"$location_acc\",\"location_provider\":\"$location_provider\",\"location_timestamp\":\"$location_timestamp\"}}"
#exit

token=event
#ls -la | tail -c 4000 > some_file.txt
message='Hello World\n===========\n'
message=`ls -la | tail -c 4000`
message=`cat some_file.txt`
#message=`tail -n 4 some_file.txt`
# added print no output `-silent -output`
#curl -silent -output --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"`cat -v some_file.txt`\"}}"
#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"`echo \"$(<some_file.txt)\"`\"}}" 
#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"$message\"}}" 


#zmienna="{\"registration_ids\":[\"$registration_id\"],\"data\":{\"token\":\"$token\",\"message\":\"`echo -ne \"$message\"`\"}}"

#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  -d "`echo -ne \"$zmienna\"`" 
#curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  --data-binary "$(<tmp_file.txt)" 
curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send   --data-binary  @tmp_file.txt
# curl --header "Authorization: key=$api_key" --header Content-Type:"application/json" https://android.googleapis.com/gcm/send  --data-binary $"`echo -ne \"$zmienna\"`" 

#echo -ne "$zmienna" 
#echo -ne "$message"

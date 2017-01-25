:: THe ^ is used solely to make this file more readable, it means continue reading from next line.
java -jar 2IID0-HW5-EMM-A-B-Testing.jar ^
-dataset-file data/speed_dating_altered.arff ^
-y-target like ^
-x-targets attractive_partner,sincere_partner,intelligence_partner,funny_partner,ambition_partner,shared_interests_partner ^
-d 3 ^
-w 20 ^
-set-length 20 ^
-null-is-zero ^
-blacklist decision,decision_o ^
-min-group-size 200 ^
-output-file result_1.csv

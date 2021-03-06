#!/bin/bash
i=1
echo ""
while [ ${i} -le 7 ]
do
	echo "checking peer_100${i} ..."
	res=$(cmp peer_100${i}/five.dat peer_1000/five.dat)
	ret=$?

	if [ ${ret} -ne 0 ]
	then
		echo "$(tput setaf 1)FAILED$(tput sgr0)"
		echo ${res}
	else
		if [ -n ${res} ]
		then
			echo "$(tput setaf 2)PASSED$(tput sgr0)"
		else
			echo "$(tput setaf 1)FAILED$(tput sgr0)"
			echo ${res}
		fi
	fi
	i=$(( $i + 1 ))
done
